package de.frosner.pia

import akka.actor.{Terminated, Props, Actor}
import akka.event.Logging
import akka.routing.{SmallestMailboxRoutingLogic, Router, ActorRefRoutee}

class RMaster(val concurrencyFactor: Int, rInterface: Option[String], rPort: Option[Int]) extends Actor {

  private val log = Logging(context.system, this)

  private def createSlave = {
    val slave = context.actorOf(RSlave.props(rInterface, rPort))
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
    case y: Double => router.route(y, sender())
  }

}

object RMaster {

  def props(concurrencyFactor: Int, rInterface: Option[String], rPort: Option[Int]): Props =
    Props(new RMaster(concurrencyFactor, rInterface, rPort))

}
