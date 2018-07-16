package protocols
import models.{Error, PlayerStatus, Players}
import play.api.libs.json.{Json, OFormat, OWrites}

trait JsonProtocol {

  implicit def playersFormat: OFormat[Players] = Json.format[Players]

  implicit def playerStatusFormat: OFormat[PlayerStatus] = Json.format[PlayerStatus]

  implicit def errorFormat: OFormat[Error] = Json.format[Error]
}
