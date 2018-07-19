package services

import java.io.IOException
import java.net.UnknownHostException

import models.{Error, Players}
import net.straylightlabs.hola.dns.Domain
import net.straylightlabs.hola.sd.{Instance, Query, Service}
import play.api.Logger

import collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait PlayerDiscoveryLike extends Player{

  implicit val ec: ExecutionContext

  def getPlayers: Future[Either[Error, Players]] =
    try {
      Future {
        val service = Service.fromName("_musc._tcp")

        val query                            = Query.createFor(service, Domain.LOCAL)
        val instances: mutable.Set[Instance] = query.runOnce.asScala

        Right(
          Players(
            instances
              .map(i =>
                i.getName -> s"http://${i.getAddresses.asScala.headOption.getOrElse("").toString.substring(1)}:11000").toMap))
      }
    } catch {
      case e: UnknownHostException =>
        Logger.error("Unknown host: ", e)
        Future.successful(Left(Error(None, s"Unknown host: $e")))
      case e: IOException =>
        Logger.error("IO error: ", e)
        Future.successful(Left(Error(None, s"IO error: $e")))
      case e: Throwable =>
        Logger.error("Unknown exception: ", e)
        Future.successful(Left(Error(None, s"Unknown exception: $e")))
    }
}
