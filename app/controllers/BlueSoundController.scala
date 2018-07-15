package controllers

import java.util.concurrent.TimeUnit

import javax.inject.Inject
import models.{Error, PlayerStatus, Players}
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import play.api.libs.concurrent.Futures._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext


class BlueSoundController @Inject()(cc: ControllerComponents, player: services.Player, configuration: Configuration)(implicit ec: ExecutionContext, fu: play.api.libs.concurrent.Futures) extends AbstractController(cc) {

  implicit def playersWrite: OWrites[Players] = Json.writes[Players]
  implicit def playerStatusWrite: OWrites[PlayerStatus] = Json.writes[PlayerStatus]
  implicit def errorWrite: OWrites[Error] = Json.writes[Error]

  val defaultTimeout: FiniteDuration = Duration.apply(configuration.get[Long]("bluesound.timeout"), TimeUnit.SECONDS)

  def handleResponse: PartialFunction[Either[Error, PlayerStatus], Result] = {
    case Right(playerStatus) => Ok(Json.toJson(playerStatus))
    case Left(error) => InternalServerError(Json.toJson(error))
  }

  def handleTimeout: PartialFunction[Throwable, Result] = {
    case e: scala.concurrent.TimeoutException =>
      Logger.error("PlayerService timed out", e)
      GatewayTimeout(Json.toJson(Error(None, "PlayerService did not respond. Try again later.")))
  }

  def startToPlay(room: String): Action[AnyContent] = Action.async {
    player.play(room).withTimeout(defaultTimeout).map(handleResponse).recover(handleTimeout)
  }

  def pause(room: String): Action[AnyContent] = Action.async {
    player.pause(room).withTimeout(defaultTimeout).map(handleResponse).recover(handleTimeout)
  }

  def volumeUp(room: String): Action[AnyContent] = Action.async {
    player.volumeUp(room).withTimeout(defaultTimeout).map(handleResponse).recover(handleTimeout)
  }

  def volumeDown(room: String): Action[AnyContent] = Action.async {
    player.volumeDown(room).withTimeout(defaultTimeout).map(handleResponse).recover(handleTimeout)
  }

  def getPlayers: Action[AnyContent] = Action.async {
    player.getPlayers.map {
      case Right(players) => Ok(Json.toJson(players))
      case Left(error) => InternalServerError(Json.toJson(error))
    }
  }
}

