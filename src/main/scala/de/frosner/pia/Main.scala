package de.frosner.pia

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpHeader, HttpResponse, StatusCodes, headers, Uri}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.twitter.util.LruMap
import de.frosner.pia.Observations.Observation
import org.rosuda.REngine.{RList, REXP, REXPDouble}

object Main extends App {

  type Result = Double

  private implicit val system = ActorSystem("pia")
  private implicit val materializer = ActorMaterializer()

  private var predictions = new LruMap[UUID, Option[Result]](Options.cacheCapacity)

  val rMaster = system.actorOf(RMaster.props(
    concurrencyFactor = Options.concurrencyFactor,
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
          rMaster ! dataFrame
          HttpResponse(
            status = StatusCodes.Created,
            headers = List(headers.Location(Uri(s"$interface:$port/$predictionsEndpoint/${uuid.toString}")))
          )
        }
      }
    }
  }

  val binding = Http().bindAndHandle(route, interface, port)(materializer)

  println(s"Pia online at http://$interface:$port/...")
  Console.readLine()

  import system.dispatcher
  binding.flatMap(_.unbind()).onComplete(_ => system.shutdown())

}
