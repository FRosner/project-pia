package de.frosner.pia

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{StatusCodes}
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.Await

object Main extends App {

  implicit val system = ActorSystem("pia-system")
  implicit val materializer = ActorMaterializer()
  val timeout = Timeout(5 seconds)

  val rMaster = system.actorOf(Props[RMaster])

  val route = path("prediction") {
    get {
      complete {
        Await.result(rMaster.ask(5d)(timeout.duration), timeout.duration).toString
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
