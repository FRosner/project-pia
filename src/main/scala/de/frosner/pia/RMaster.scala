package de.frosner.pia

import akka.actor.{Props, Actor}
import akka.event.Logging
import akka.routing.{SmallestMailboxRoutingLogic, Router, ActorRefRoutee}
import org.rosuda.REngine.REXP

class RMaster(concurrencyFactor: Int,
              rInterface: String,
              rPort: Int,
              initScript: String,
              predictScript: String) extends Actor {

  private val log = Logging(context.system, this)

  private def createSlave = {
    val slave = context.actorOf(RSlave.props(rInterface, rPort, initScript, predictScript))
    context.watch(slave)
    slave
  }

  private var router = {
    log.info(s"""Spawning $concurrencyFactor R slave${if (concurrencyFactor > 1) "s" else ""}""")
    val routees = Vector.fill(concurrencyFactor) {
      ActorRefRoutee(createSlave)
    }
    Router(SmallestMailboxRoutingLogic(), routees)
  }

  def receive = {
    case y: REXP => router.route(y, sender())
    case default => log.warning(s"Received unrecognized message: $default")
  }

}

object RMaster {

  def props(concurrencyFactor: Int,
            rInterface: String,
            rPort: Int,
            initScript: String,
            predictScript: String): Props =
    Props(new RMaster(concurrencyFactor, rInterface, rPort, initScript, predictScript))

}
