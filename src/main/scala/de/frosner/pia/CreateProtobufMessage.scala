package de.frosner.pia

import java.io.{FileInputStream, FileOutputStream}

import de.frosner.pia.Observations.Observation

object CreateProtobufMessage extends App {

  for (i <- 1 to 10) {
    val out = new FileOutputStream(s"$i.obs")
    val obsI = Observation.newBuilder().setDoubleFeature(i).build()
    obsI.writeTo(out)
    val observationsProto = Observations.getDescriptor
    println(observationsProto.getName)
    val observation = observationsProto.getMessageTypes.get(0)
    println(observation.getName)
    val firstFeature = observation.getFields.get(0)
    println(firstFeature.getName)
    out.close()
  }

}
