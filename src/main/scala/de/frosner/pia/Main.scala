package de.frosner.pia

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import de.frosner.pia.Observations.Observation
import org.rosuda.REngine.{RList, REXP, REXPDouble}
import scala.concurrent.duration._

import scala.concurrent.Await
import scala.io.Source
import scala.util.{Failure, Success, Try}

object Main extends App {

  private val DEFAULT_TIMEOUT = 5000
  private val DEFAULT_CONCURRENCY_FACTOR = 1

  private val timeout = {
    val timeoutTime = Try {
      val parsedTimeout = System.getProperty("pia.timeout", DEFAULT_TIMEOUT.toString).toInt
      require(parsedTimeout > 0)
      parsedTimeout
    }.getOrElse {
      println(s"Invalid timeout format: Falling back to default")
      DEFAULT_TIMEOUT
    }
    Timeout(timeoutTime milliseconds)
  }

  private val concurrencyFactor = Try {
    val parsedFactor = System.getProperty("pia.concurrencyFactor", DEFAULT_CONCURRENCY_FACTOR.toString).toInt
    require(parsedFactor > 0)
    parsedFactor
  }.getOrElse {
    println(s"Invalid concurrency factor format: Falling back to default")
    DEFAULT_CONCURRENCY_FACTOR
  }

  private val rServerInterface = Option(System.getProperty("pia.rServerInterface"))

  private val rServerPort = {
    val maybePort = Option(System.getProperty("pia.rServerPort"))
    maybePort.flatMap(port => Try(Some(port.toInt)).getOrElse{
      println(s"Invalid R server port format: Falling back to default")
      None
    })
  }

  private val initScript = {
    val location = Option(System.getProperty("pia.script.init")).getOrElse("init.R")
    Source.fromFile(location).mkString
  }

  private val predictScript = {
    val location = Option(System.getProperty("pia.script.predict")).getOrElse("predict.R")
    Source.fromFile(location).mkString
  }

  private implicit val system = ActorSystem("pia")
  private implicit val materializer = ActorMaterializer()

  val rMaster = system.actorOf(RMaster.props(concurrencyFactor, rServerInterface, rServerPort, initScript, predictScript))

  val observationUnmarshaller = FromByteArrayRequestUnmarshaller{ bytes => Observation.parseFrom(bytes) }

  val route = path("prediction") {
    post {
      entity(observationUnmarshaller) { observation =>
        complete {
          val data = new REXPDouble(observation.getDoubleFeature).asInstanceOf[REXP]
          val dataFrame = REXP.createDataFrame(new RList(Array(data), Array("doubleFeature")))
          val result = Await.result(rMaster.ask(dataFrame)(timeout.duration), timeout.duration)
          result match {
            case Success(score: Double) => HttpResponse(StatusCodes.OK, entity = score.toString)
            case Failure(_) => HttpResponse(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  val interface = "localhost"
  val port = 8080
  val binding = Http().bindAndHandle(route, interface, port)

  println(s"Pia online at http://$interface:$port/...")
  Console.readLine()

  import system.dispatcher
  binding.flatMap(_.unbind()).onComplete(_ => system.shutdown())

}
