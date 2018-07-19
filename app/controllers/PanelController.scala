package controllers

import java.util.concurrent.TimeUnit

import javax.inject.Inject
import models.{Error, PlayerStatus}
import play.api.libs.concurrent.Futures._
import play.api.libs.json._
import play.api.mvc._
import play.api.{Configuration, Logger}
import protocols.JsonProtocol
import services.PanelService

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class PanelController @Inject()(
    cc: ControllerComponents,
    configuration: Configuration,
    panelService: PanelService)(implicit ec: ExecutionContext, fu: play.api.libs.concurrent.Futures)
    extends AbstractController(cc)
    with JsonProtocol {

  val defaultTimeout: FiniteDuration =
    Duration.apply(configuration.get[Long]("bluesound.timeout"), TimeUnit.SECONDS)

  def handlePanelCall(panelId: String, buttonId: String) = Action.async {
    Logger.debug(s"Panel '$panelId' called with button '$buttonId'")
    panelService
      .trigger(panelId, buttonId)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }

  private def handleResponse: PartialFunction[Either[Error, Boolean], Result] = {
    case Right(success) => Ok(Json.toJson("success" -> success))
    case Left(error)         => InternalServerError(Json.toJson(error))
  }

  private def handleExceptions: PartialFunction[Throwable, Result] = {
    case e: scala.concurrent.TimeoutException =>
      Logger.error("PanelService timed out", e)
      GatewayTimeout(Json.toJson(Error(None, "PanelService did not respond. Try again later.")))
    case e: Exception =>
      Logger.error("PanelService encountered an exception", e)
      InternalServerError(
        Json.toJson(Error(None, "PanelService encountered an exception. Try again later.")))
  }

}
