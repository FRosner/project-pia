package de.frosner.pia

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, PredefinedFromEntityUnmarshallers}
import akka.stream.Materializer

import scala.concurrent.{Future, ExecutionContext}

case class FromByteArrayRequestUnmarshaller[T](parser: Array[Byte] => T) extends FromRequestUnmarshaller[T] {

  override def apply(request: HttpRequest)(implicit ec: ExecutionContext): Future[T] = {
    val futureRequestEntity = PredefinedFromEntityUnmarshallers.byteStringUnmarshaller(null).apply(request.entity)
    futureRequestEntity.map((byteString: akka.util.ByteString) => parser(byteString.toArray))
  }
  
}
