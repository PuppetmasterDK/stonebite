package controllers

import java.util.concurrent.TimeUnit

import javax.inject.Inject
import models.{Error, PlayerStatus, Players}
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import play.api.libs.concurrent.Futures._
import play.api.mvc._
import play.api.libs.json._
import protocols.JsonProtocol

import scala.concurrent.ExecutionContext

class BlueSoundController @Inject()(cc: ControllerComponents,
                                    player: services.Player,
                                    configuration: Configuration)(
    implicit ec: ExecutionContext,
    fu: play.api.libs.concurrent.Futures)
    extends AbstractController(cc)
    with JsonProtocol {

  val defaultTimeout: FiniteDuration =
    Duration.apply(configuration.get[Long]("bluesound.timeout"), TimeUnit.SECONDS)

  private def handleResponse: PartialFunction[Either[Error, PlayerStatus], Result] = {
    case Right(playerStatus) => Ok(Json.toJson(playerStatus))
    case Left(error)         => InternalServerError(Json.toJson(error))
  }

  private def handleExceptions: PartialFunction[Throwable, Result] = {
    case e: scala.concurrent.TimeoutException =>
      Logger.error("PlayerService timed out", e)
      GatewayTimeout(Json.toJson(Error(None, "PlayerService did not respond. Try again later.")))
    case e: Exception =>
      Logger.error("PlayerService encountered an exception", e)
      InternalServerError(
        Json.toJson(Error(None, "PlayerService encountered an exception. Try again later.")))
  }

  def startToPlay(room: String): Action[AnyContent] = Action.async {
    player
      .play(room)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }

  def pause(room: String): Action[AnyContent] = Action.async {
    player
      .pause(room)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }

  def volumeUp(room: String): Action[AnyContent] = Action.async {
    player
      .volumeUp(room)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }

  def volumeDown(room: String): Action[AnyContent] = Action.async {
    player
      .volumeDown(room)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }

  def volume(room: String, volume: Int): Action[AnyContent] = Action.async {
    player
      .volume(room, volume)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }

  def getPlayers: Action[AnyContent] = Action.async {
    player.getPlayers
      .map {
        case Right(players) => Ok(Json.toJson(players))
        case Left(error)    => InternalServerError(Json.toJson(error))
      }
      .recover(handleExceptions)
  }

  def status(room: String): Action[AnyContent] = Action.async {
    player
      .getStatus(room)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }

  def playlist(room: String, playlist: String): Action[AnyContent] = Action.async {
    player
      .playPlaylist(room, playlist)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }

  def artist(room: String, artist: String): Action[AnyContent] = Action.async {
    player
      .playArtist(room, artist)
      .withTimeout(defaultTimeout)
      .map(handleResponse)
      .recover(handleExceptions)
  }
}
