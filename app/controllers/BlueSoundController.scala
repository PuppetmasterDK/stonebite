package controllers

import javax.inject.Inject
import models.{Error, PlayerStatus, Players}
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext
import play.api.libs.json._

class BlueSoundController @Inject()(cc: ControllerComponents, ws: WSClient, player: services.Player)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit def playersWrite: OWrites[Players] = Json.writes[Players]
  implicit def playerStatusWrite: OWrites[PlayerStatus] = Json.writes[PlayerStatus]
  implicit def errorWrite: OWrites[Error] = Json.writes[Error]

  def handleResponse: PartialFunction[Either[Error, PlayerStatus], Result] = {
    case Right(playerStatus) => Ok(Json.toJson(playerStatus))
    case Left(error) => InternalServerError(Json.toJson(error))
  }

  def startToPlay(room: String): Action[AnyContent] = Action.async {
    player.play(room).map(handleResponse)
  }

  def pause(room: String): Action[AnyContent] = Action.async {
    player.pause(room).map(handleResponse)
  }

  def volumeUp(room: String, increment: Int = 5) = Action.async {
    player.volumeUp(room).map(handleResponse)
  }

  def volumeDown(room: String, increment: Int = 5) = Action.async {
    player.volumeDown(room).map(handleResponse)
  }

  def getPlayers = Action.async {
    player.getPlayers.map {
      case Right(players) => Ok(Json.toJson(players))
      case Left(error) => InternalServerError(Json.toJson(error))
    }
  }
}

