package de.frosner.pia

import akka.actor.{Props, Actor}
import akka.event.Logging
import org.rosuda.REngine.Rserve.RConnection

class RSlave(val rInterface: Option[String], rPort: Option[Int]) extends Actor {
  
  private val actualInterface = rInterface.getOrElse("127.0.0.1")
  private val actualPort = rPort.getOrElse(6311)

  private val log = Logging(context.system, this)

  log.info(s"Connecting to R on $actualInterface:$actualPort")
  private val r = new RConnection(actualInterface, actualPort)
  r.eval("x <- 5")

  def receive = {
    case y: Double => {
      log.info(s"Received $y")
      sender() ! r.parseAndEval("x").asDouble()
    }
  }

}

object RSlave {
  
  def props(rInterface: Option[String], rPort: Option[Int]): Props = Props(new RSlave(rInterface, rPort))
  
}
