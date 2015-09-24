package de.frosner.pia

import akka.actor.Actor
import akka.event.Logging
import org.rosuda.REngine.Rserve.RConnection

class RSlave extends Actor {

  val log = Logging(context.system, this)

  log.info("Starting R engine")
  val r = new RConnection()
  r.eval("x <- 5")

  def receive = {
    case y: Double => {
      log.info(s"Received $y")
      sender() ! r.parseAndEval("x").asDouble()
    }
  }

}
