package services

import javax.inject.Inject
import models.{Error, PlayerStatus, Players}
import play.api.{Configuration, Logger}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class PlayerLike @Inject()(ws: WSClient, configuration: Configuration)(implicit ec: ExecutionContext) extends Player {

  private def roomToAddress(room: String): Either[Error, String] =
    try {
      Right(configuration.get[String](s"bluesound.players.$room"))
    } catch {
      case e: Exception => Left(Error(None, s"Room '$room' not found in configuration"))
    }

  override def getPlayers: Future[Either[Error, Players]] =
    try {
      Future.successful(Right(Players(configuration.get[Map[String, String]]("bluesound.players").keys.toList.sorted)))
    } catch {
      case e: Exception => Future.successful(Left(Error(None, s"Unable to fetch players from configuration")))
    }

  override def play(room: String): Future[Either[Error, PlayerStatus]] =
    doAction(
      room = room,
      action = "Play"
    )

  override def pause(room: String): Future[Either[Error, PlayerStatus]] =
    doAction(
      room = room,
      action = "Pause"
    )

  override def volumeUp(room: String): Future[Either[Error, PlayerStatus]] =
    doAction(
      room = room,
      action = "Volume",
      getArguments = {
        playerStatus =>
          List("level" -> Math.min(100, (Math.round(playerStatus.volume / 5) * 5) + 5).toString)
      }
    )

  override def volumeDown(room: String): Future[Either[Error, PlayerStatus]] =
    doAction(
      room = room,
      action = "Volume",
      getArguments = {
        playerStatus =>
          List("level" -> Math.min(100, (Math.round(playerStatus.volume / 5) * 5) - 5).toString)
      }
    )

  override def getStatus(room: String): Future[Either[Error, PlayerStatus]] = {
    def parsePlayerResponse(response: WSResponse): Either[Error, PlayerStatus] = {
      try {
        val playerStatus: Option[PlayerStatus] = for {
          state <- (response.xml \ "state" headOption).map(_.text)
          volume <- (response.xml \ "volume" headOption).map(_.text).map(_.toInt)
        } yield PlayerStatus(List("stream", "play").contains(state), volume)

        playerStatus.map(x => Right(x)).getOrElse(Left(Error(None, s"Unable to find state and volume in XML for room '$room'. Got ${
          response.status
        } with body '${
          response.body
        }'")))
      } catch {
        case e: Exception => Left(Error(None, s"Unable to parse XML: '${response.body}"))
      }

    }

    val action = "Status"

    def errorMessage(response: WSResponse) = s"Bluesound returned non OK for room '$room'. Got ${
      response.status
    } with body '${
      response.body
    }'"

    def success(room: String, response: WSResponse) = {
      Future.successful(parsePlayerResponse(response))
    }

    roomToAddress(room) match {
      case Right(url) =>
        Logger.debug(s"Get Status for $room on $url/$action ")
        ws
          .url(s"$url/$action")
          .get()
          .flatMap {
            response: WSResponse =>
              response.status match {
                case 200 => success(room, response)
                case _ => Future.successful(Left(Error(None, errorMessage(response))))
              }
          }
      case Left(error) => Future.successful(Left(error))
    }
  }


  private def doAction(
                        room: String,
                        action: String,
                        errorMessage: (String, WSResponse) => String = {
                          (room, response) =>
                            s"Unable to play in room '$room'. Got: HTTP Code: ${
                              response.status
                            } with body: '${
                              response.body
                            }'"
                        },
                        success: (String, WSResponse) => Future[Either[Error, PlayerStatus]] = {
                          (response, _) => getStatus(response)
                        },
                        getArguments: PlayerStatus => List[(String, String)] = _ => List.empty
                      ): Future[Either[Error, PlayerStatus]] = {

    Logger.debug(s"Start '$action' in room '$room")
    roomToAddress(room) match {
      case Right(url) =>
        getStatus(room).flatMap {
          case Right(playerStatus) =>
            Logger.debug(s"Calling $room on $url/$action")
            ws
              .url(s"$url/$action")
              .addQueryStringParameters(getArguments(playerStatus): _*)
              .get()
              .flatMap {
                response: WSResponse =>
                  response.status match {
                    case 200 => success(room, response)
                    case _ => Future.successful(Left(Error(Some(playerStatus), errorMessage(room, response))))
                  }
              }
          case Left(error) => Future.successful(Left(error))
        }
      case Left(error) => Future.successful(Left(error))
    }
  }
}
