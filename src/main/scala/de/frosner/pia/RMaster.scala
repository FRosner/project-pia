package de.frosner.pia

import akka.actor.{Terminated, Props, Actor}
import akka.event.Logging
import akka.routing.{SmallestMailboxRoutingLogic, Router, ActorRefRoutee}

class RMaster extends Actor {

  val log = Logging(context.system, this)

  private def createSlave = {
    val slave = context.actorOf(Props[RSlave])
    context.watch(slave)
    slave
  }

  private var router = {
    val routees = Vector.fill(2) {
      ActorRefRoutee(createSlave)
    }
    Router(SmallestMailboxRoutingLogic(), routees)
  }

  def receive = {
    case y: Double => router.route(y, sender())
  }

}
