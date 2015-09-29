package de.frosner.pia

import scala.io.Source
import scala.util.Try

object Options {

  private val DEFAULT_CACHE_CAPACITY = 100000
  val cacheCapacity = {
    val maybeCacheCapacity = Option(System.getProperty("pia.cacheCapacity"))
    if (maybeCacheCapacity.isDefined) {
      Try {
        val parsedCacheCapacity = maybeCacheCapacity.get.toInt
        require(parsedCacheCapacity > 0)
        parsedCacheCapacity
      }.getOrElse {
        println(s"Invalid cache capacity: Falling back to default of $DEFAULT_CACHE_CAPACITY")
        DEFAULT_CACHE_CAPACITY
      }
    } else {
      println(s"No cache capacity specified. Using default of $DEFAULT_CACHE_CAPACITY")
      DEFAULT_CACHE_CAPACITY
    }
  }

  private val DEFAULT_CONCURRENT_R_CONNECTIONS = 1
  val concurrentRConnections = {
    val maybeConcurrentRConnections = Option(System.getProperty("pia.concurrentRConnections"))
    if (maybeConcurrentRConnections.isDefined) {
      Try {
        val parsedFactor = maybeConcurrentRConnections.get.toInt
        require(parsedFactor > 0)
        parsedFactor
      }.getOrElse {
        println(s"Invalid concurrency factor: Falling back to default of $DEFAULT_CONCURRENT_R_CONNECTIONS")
        DEFAULT_CONCURRENT_R_CONNECTIONS
      }
    } else {
      println(s"No concurrency factor specified. Using default of $DEFAULT_CONCURRENT_R_CONNECTIONS")
      DEFAULT_CONCURRENT_R_CONNECTIONS
    }
  }

  val concurrentHttpConnections = 1

  private val DEFAULT_R_SERVER_INTERFACE = "127.0.0.1"
  val rServerInterface = Option(System.getProperty("pia.rServerInterface")).getOrElse {
    println(s"No R server interface specified. Using default of $DEFAULT_R_SERVER_INTERFACE")
    DEFAULT_R_SERVER_INTERFACE
  }

  private val DEFAULT_R_SERVER_PORT = 6311
  val rServerPort = {
    val maybePort = Option(System.getProperty("pia.rServerPort"))
    if (maybePort.isDefined) {
      Try(maybePort.get.toInt).getOrElse {
        println(s"Invalid R server port format: Falling back to default of $DEFAULT_R_SERVER_PORT")
        DEFAULT_R_SERVER_PORT
      }
    } else {
      println(s"No R server port specified. Using default of $DEFAULT_R_SERVER_PORT")
      DEFAULT_R_SERVER_PORT
    }
  }

  private val DEFAULT_INIT_SCRIPT = "init.R"
  val initScript = {
    val location = Option(System.getProperty("pia.script.init")).getOrElse {
      println(s"No init script specified. Using default of $DEFAULT_INIT_SCRIPT")
      DEFAULT_INIT_SCRIPT
    }
    Source.fromFile(location).mkString
  }

  private val DEFAULT_PREDICT_SCRIPT = "predict.R"
  val predictScript = {
    val location = Option(System.getProperty("pia.script.predict")).getOrElse {
      println(s"No predict script specified. Using default of $DEFAULT_PREDICT_SCRIPT")
      DEFAULT_PREDICT_SCRIPT
    }
    Source.fromFile(location).mkString
  }

}
