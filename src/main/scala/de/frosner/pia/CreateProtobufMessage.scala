package de.frosner.pia

import java.io.{FileInputStream, FileOutputStream}

import de.frosner.pia.Observations.Observation

object CreateProtobufMessage extends App {

  for (i <- 1 to 10) {
    val out = new FileOutputStream(s"$i.obs")
    Observation.newBuilder().setDoubleFeature(i).build().writeTo(out)
    out.close()
  }

}
