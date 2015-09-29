package de.frosner.pia

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpHeader, HttpResponse, StatusCodes, headers, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.twitter.util.LruMap
import de.frosner.pia.Observations.Observation
import org.rosuda.REngine.{RList, REXP, REXPDouble}
import scala.util.{Try, Failure, Success}

object Main extends App {

  type Result = Double

  private implicit val system = ActorSystem("pia")
  private implicit val materializer = ActorMaterializer()(system)

  private val predictions = new ConcurrentHashMap[UUID, Option[Try[Result]]]()

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
    post {
      entity(observationUnmarshaller) { observation =>
        complete {
          val data = new REXPDouble(observation.getDoubleFeature).asInstanceOf[REXP]
          val dataFrame = REXP.createDataFrame(new RList(Array(data), Array("doubleFeature")))
          val uuid = UUID.randomUUID()
          predictions.put(uuid, None)
          (rMaster ? dataFrame)(Timeout(1000)).onSuccess {
            case result: Try[Result] => predictions.replace(uuid, Some(result))
          }(system.dispatcher)
          println(predictions)
          HttpResponse(
            status = StatusCodes.Created,
            headers = List(headers.Location(Uri(s"$interface:$port/$predictionsEndpoint/${uuid.toString}")))
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
