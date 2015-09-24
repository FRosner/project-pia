package de.frosner.pia

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{StatusCodes}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import org.rosuda.REngine.JRI.JRIEngine

object Main extends App {

  implicit val system = ActorSystem("pia-system")
  implicit val materializer = ActorMaterializer()

  val r = JRIEngine.createEngine()

  val route = path("prediction") {
    get {
      complete {
        r.parseAndEval("x <- 5\nx").asString()
      }
    }
  }

  val interface = "localhost"
  val port = 8080
  val binding = Http().bindAndHandle(route, interface, port)

  println(s"Pia online at http://$interface:$port/\nPress RETURN to stop...")
  Console.readLine()

  import system.dispatcher
  binding.flatMap(_.unbind()).onComplete(_ => system.shutdown())

}
