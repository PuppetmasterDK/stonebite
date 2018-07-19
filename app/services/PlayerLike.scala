package services

import javax.inject.Inject
import models.{Error, PlayerStatus, Players}
import play.api.{Configuration, Logger}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class PlayerLike @Inject()(ws: WSClient, configuration: Configuration)(
    implicit val ec: ExecutionContext)
    extends Player with PlayerDiscoveryLike {

  private def roomToAddress(room: String): Future[Either[Error, String]] =
    getPlayers.map {
      case Right(players) =>
        players.players.get(room) match {
          case Some(address) => Right(address)
          case None => Left(Error(None, s"Room '$room' not found in list of players: $players"))
        }

      case Left(error) => Left(error)
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
      getArguments = { playerStatus =>
        List("level" -> Math.min(100, (Math.round(playerStatus.volume / 5) * 5) + 5).toString)
      }
    )

  override def volumeDown(room: String): Future[Either[Error, PlayerStatus]] =
    doAction(
      room = room,
      action = "Volume",
      getArguments = { playerStatus =>
        List("level" -> Math.min(100, (Math.round(playerStatus.volume / 5) * 5) - 5).toString)
      }
    )

  override def getStatus(room: String): Future[Either[Error, PlayerStatus]] = {
    def parsePlayerResponse(response: WSResponse): Either[Error, PlayerStatus] =
      try {
        val playerStatus: Option[PlayerStatus] = for {
          state  <- (response.xml \ "state" headOption).map(_.text)
          volume <- (response.xml \ "volume" headOption).map(_.text).map(_.toInt)
        } yield
          PlayerStatus(List("stream", "play").contains(state),
                       volume,
                       (response.xml \ "sleep" headOption).map(_.text).filterNot(_.isEmpty).map(_.toInt))

        playerStatus
          .map(x => Right(x))
          .getOrElse(Left(Error(
            None,
            s"Unable to find state and volume in XML for room '$room'. Got ${response.status} with body '${response.body}'")))
      } catch {
        case e: Exception =>
          Logger.error(s"Unable to parse XML for status for room '$room'", e)
          Left(Error(None, s"Unable to parse XML: '${response.body}"))
      }

    val action = "Status"

    def errorMessage(response: WSResponse) =
      s"Bluesound returned non OK for room '$room'. Got ${response.status} with body '${response.body}'"

    def success(room: String, response: WSResponse) =
      Future.successful(parsePlayerResponse(response))

    roomToAddress(room) flatMap {
      case Right(url) =>
        Logger.debug(s"Get Status for '$room' on $url/$action ")
        ws.url(s"$url/$action")
          .get()
          .flatMap { response: WSResponse =>
            response.status match {
              case 200 => success(room, response)
              case _   => Future.successful(Left(Error(None, errorMessage(response))))
            }
          }
      case Left(error) => Future.successful(Left(error))
    }
  }

  private def doAction(
      room: String,
      action: String,
      errorMessage: (String, WSResponse) => String = { (room, response) =>
        s"Unable to play in room '$room'. Got: HTTP Code: ${response.status} with body: '${response.body}'"
      },
      success: (String, WSResponse) => Future[Either[Error, PlayerStatus]] = { (response, _) =>
        getStatus(response)
      },
      getArguments: PlayerStatus => List[(String, String)] = _ => List.empty
  ): Future[Either[Error, PlayerStatus]] = {

    Logger.debug(s"Start '$action' in room '$room'")
    roomToAddress(room).flatMap {
      case Right(url) =>
        getStatus(room).flatMap {
          case Right(playerStatus) =>
            Logger.debug(
              s"Calling $room on $url/$action with arguments: ${getArguments(playerStatus)}")
            ws.url(s"$url/$action")
              .addQueryStringParameters(getArguments(playerStatus): _*)
              .get()
              .flatMap { response: WSResponse =>
                response.status match {
                  case 200 => success(room, response)
                  case _ =>
                    Future.successful(Left(Error(Some(playerStatus), errorMessage(room, response))))
                }
              }
          case Left(error) => Future.successful(Left(error))
        }
      case Left(error) => Future.successful(Left(error))
    }
  }

  override def volume(room: String, volume: Int): Future[Either[Error, PlayerStatus]] =
    doAction(
      room = room,
      action = "Volume",
      getArguments = { _ =>
        List("level" -> Math.min(100, Math.max(0, volume)).toString)
      }
    )

  override def playPlaylist(room: String, playlist: String): Future[Either[Error, PlayerStatus]] =
    doAction(
      room = room,
      action = "Add",
      getArguments = { _ =>
        List(
          "service"    -> "LocalMusic",
          "playnow"    -> "1",
          "playlistid" -> playlist,
          "clear"      -> "1",
          "listindex"  -> "0",
          "playlist"   -> playlist
        )
      }
    )

  override def playArtist(room: String, artist: String): Future[Either[Error, PlayerStatus]] =
    doAction(
      room = room,
      action = "Add",
      getArguments = { _ =>
        List(
          "service"   -> "LocalMusic",
          "playnow"   -> "1",
          "where"     -> "last",
          "all"       -> "1",
          "listindex" -> "0",
          "nextlist"  -> "1",
          "cursor"    -> "last",
          "artist"    -> artist
        )
      }
    )

  override def sleep(room: String, time: Int): Future[Either[Error, PlayerStatus]] =
    getStatus(room).flatMap {
      case Right(playerStatus) =>
        playerStatus.sleep match {
          case Some(timeout) if timeout < time =>
            callSleepEndpoint(room).flatMap {
              case Right(_)    => sleep(room, time)
              case Left(error) => Future.successful(Left(error))
            }
          case Some(_) => Future.successful(Right(playerStatus))
          case None    => sleep(room, time)
        }
      case Left(error) => Future.successful(Left(error))
    }

  private def callSleepEndpoint(room: String): Future[Either[Error, PlayerStatus]] =
    roomToAddress(room).flatMap {
      case Right(url) =>
        ws.url(s"$url/Sleep")
          .get()
          .flatMap { response: WSResponse =>
            response.status match {
              case 200 => getStatus(room)
              case _ =>
                Future.successful(Left(Error(
                  None,
                  s"Calling Sleep for room '$room' failed with status: '${response.status}' and body '${response.body}'")))
            }
          }
      case Left(error) => Future.successful(Left(error))

    }
}
