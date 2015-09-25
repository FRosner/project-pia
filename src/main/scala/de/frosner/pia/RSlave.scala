package de.frosner.pia

import akka.actor.{Props, Actor}
import akka.event.Logging
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection

import scala.util.{Failure, Try}

class RSlave(rInterface: Option[String], rPort: Option[Int], initScript: String, predictScript: String) extends Actor {
  
  private val actualInterface = rInterface.getOrElse("127.0.0.1")
  private val actualPort = rPort.getOrElse(6311)

  private val log = Logging(context.system, this)

  private def executeWithLogging(r: RConnection, script: () => REXP, scriptType: String): Try[REXP] = {
    if (log.isDebugEnabled) log.debug(s"Executing $scriptType script:\n$script")
    else log.info(s"Executing $scriptType script")
    Try(script()).recoverWith {
      case t: Throwable => {
        log.error(s"Executing $scriptType failed: $t")
        Failure(t)
      }
    }
  }

  log.info(s"Connecting to R on $actualInterface:$actualPort")
  private val r = new RConnection(actualInterface, actualPort)

  executeWithLogging(r, () => r.eval(initScript), "init")

  def receive = {
    case y: REXP => {
      val toExecute = () => {
        r.assign("y", y)
        r.eval(predictScript)
      }
      sender() ! executeWithLogging(r, toExecute, "predict").map(_.asDouble())
    }
    case default => log.warning(s"Received unrecognized message: $default")
  }

}

object RSlave {
  
  def props(rInterface: Option[String], rPort: Option[Int], initScript: String, predictScript: String): Props =
    Props(new RSlave(rInterface, rPort, initScript, predictScript))
  
}
