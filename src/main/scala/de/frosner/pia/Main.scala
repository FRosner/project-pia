package de.frosner.pia

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.javadsl.server.values.PathMatchers
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.twitter.util.LruMap
import de.frosner.pia.Observations.Observation
import org.rosuda.REngine.{RList, REXP, REXPDouble}
import spray.json.{JsNumber, JsString, JsArray}
import scala.util.{Try, Failure, Success}
import scala.collection.JavaConversions.mapAsScalaConcurrentMap

object Main extends App {

  type Result = Double

  private implicit val system = ActorSystem("pia")
  private implicit val materializer = ActorMaterializer()(system)

  private val predictions = mapAsScalaConcurrentMap(new ConcurrentHashMap[UUID, Option[Try[Result]]]())

  val rMaster = system.actorOf(RMaster.props(
    concurrencyFactor = Options.concurrentRConnections,
    rInterface = Options.rServerInterface,
    rPort = Options.rServerPort,
    initScript = Options.initScript,
    predictScript = Options.predictScript
  ))

  val observationUnmarshaller = FromByteArrayRequestUnmarshaller{ bytes => Observation.parseFrom(bytes) }

  val interface = "localhost"
  val port = 8080

  private val predictionsEndpoint = "predictions"
  val route = path(predictionsEndpoint) {
    get {
      complete {
        JsArray(predictions.keys.map(key => JsString(key.toString)).toVector)
      }
    } ~ post {
      entity(observationUnmarshaller) { observation =>
        complete {
          val data = new REXPDouble(observation.getDoubleFeature).asInstanceOf[REXP]
          val dataFrame = REXP.createDataFrame(new RList(Array(data), Array("doubleFeature")))
          val uuid = UUID.randomUUID()
          predictions.put(uuid, None)
          (rMaster ? dataFrame)(Timeout(1000)).onSuccess {
            case result: Try[Result] => predictions.replace(uuid, Some(result))
          }(system.dispatcher)
          HttpResponse(
            status = StatusCodes.Created,
            headers = List(headers.Location(Uri(s"$interface:$port/$predictionsEndpoint/${uuid.toString}")))
          )
        }
      }
    }
  } ~ path(predictionsEndpoint / JavaUUID) { predictionId =>
    get {
      complete {
        predictions.get(predictionId) match {
          case None => HttpResponse(status = StatusCodes.NotFound)
          case Some(None) => HttpResponse(status = StatusCodes.NoContent)
          case Some(Some(Failure(_))) => HttpResponse(status = StatusCodes.InternalServerError)
          case Some(Some(Success(result))) => HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity.apply(ContentTypes.`application/json`, result.toString)
          )
        }
      }
    }
  }


  val routeHandler = {
    import system.dispatcher // some weird implicits need to get imported that I don't understand
    Route.asyncHandler(route)
  }

  val binding = Http(system).bindAndHandleAsync(
    handler = routeHandler,
    interface = interface,
    port = port,
    parallelism = Options.concurrentHttpConnections
  )(materializer)

  println(s"Pia online at http://$interface:$port/...")
  Console.readLine()

  binding.flatMap(_.unbind())(system.dispatcher).onComplete(_ => system.shutdown())(system.dispatcher)

}
