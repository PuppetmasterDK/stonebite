package services

import javax.inject.Inject
import models.{Error, PlayerStatus, Workflow}
import play.api.Logger
import cats._
import cats.data._
import cats.implicits._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class PanelServiceLike @Inject()(player: Player)(implicit ec: ExecutionContext)
  extends PanelService {

  private val panels: mutable.Map[String, mutable.Map[String, Workflow]] = mutable.Map()
  /*Map(
    "alrum" -> Map(
      "volumeUp"   -> Workflow(volumeUp = true),
      "volumeDown" -> Workflow(volumeUp = true),
      "night" -> Workflow(rooms = List("alrum"),
                          pressPlay = true,
                          volume = Some(5),
                          playlist = Some("Bryllup"))
    ),
    "voksenbad" -> Map(
      "dessert" -> Workflow(rooms = List("voksenbad"),
                            artist = Some("Ørkenens Sønner"),
                            volume = Some(10),
                            pressPlay = true)
    )
  )
   */

  panels.put(
    "alrum",
    mutable.Map(
      "night" -> Workflow(rooms = List("Alrum - Bord"),
        pressPlay = true,
        volume = Some(20),
        playlist = Some("Bryllup")),
      "volumeUp" -> Workflow(rooms = List("Alrum - Bord"), volumeUp = true),
      "volumeDown" -> Workflow(rooms = List("Alrum - Bord"), volumeDown = true)
    )
  )

  override def addWorkflow(panelId: String,
                           buttonId: String,
                           workflow: Workflow): Future[Either[Error, Boolean]] =
    if (workflow.rooms.isEmpty) {
      Future.successful(Left(Error(None, "No rooms specified in workflow")))
    } else {
      panels.get(panelId) match {
        case Some(buttons) =>
          buttons.put(buttonId, workflow)
          Future.successful(Right(true))
        case None =>
          panels.put(panelId, mutable.Map(buttonId -> workflow))
          Future.successful(Right(true))
      }
    }

  def trigger(panelId: String, buttonId: String): Future[Either[Error, Boolean]] =
    panels.get(panelId) match {
      case Some(buttons) =>
        buttons.get(buttonId) match {
          case Some(workflow) => {
            Logger.debug(
              s"Triggering panel '$panelId', button '$buttonId' with workflow: $workflow")
            triggerWorkflow(workflow)
          }
          case None => {
            Logger.error(s"Could not trigger panel '$panelId' with unknown button '$buttonId'")
            Future.successful(
              Left(Error(None, s"Could not trigger panel. Unknown button: '$buttonId'")))
          }
        }
      case None => {
        Logger.error(s"Could not find panel '$panelId' with button '$buttonId'")
        Future.successful(Left(Error(None, s"Could not trigger panel. Unknown panel: '$panelId'")))
      }
    }

  private def triggerWorkflow(workflow: Workflow): Future[Either[Error, Boolean]] = {

    val volumeUp: Future[List[Either[Error, PlayerStatus]]] = if (workflow.volumeUp) {
      Future.sequence(workflow.rooms.map(player.volumeUp))
    } else {
      Future.successful(Nil)
    }

    val volumeDown: Future[List[Either[Error, PlayerStatus]]] = if (workflow.volumeDown) {
      Future.sequence(workflow.rooms.map(player.volumeDown))
    } else {
      Future.successful(Nil)
    }

    def handleResult(result: Future[List[Either[Error, PlayerStatus]]], action: String) =
      result.map(
        _.filter(t => t.isLeft)
          .map(e => {
            Logger.error(
              s"handleResult $action: Unable to trigger '$action' in workflow: $e: $workflow")
            true
          })
          .isEmpty)


    val volume: Future[List[Either[Error, PlayerStatus]]] = Future.sequence(
      workflow.rooms.flatMap(room => workflow.volume.map(v => player.volume(room, v))))
    val sleep: Future[List[Either[Error, PlayerStatus]]] = Future.sequence(
      workflow.rooms.flatMap(room => workflow.sleep.map(s => player.sleep(room, s))))

    val playlist: Future[List[Either[Error, PlayerStatus]]] = Future.sequence(
      workflow.rooms.flatMap(room => workflow.playlist.map(p => player.playPlaylist(room, p))))

    val artist: Future[List[Either[Error, PlayerStatus]]] = Future.sequence(
      workflow.rooms.flatMap(room => workflow.artist.map(a => player.playArtist(room, a))))

    val play: Future[List[Either[Error, PlayerStatus]]] = if (workflow.pressPlay) {
      Future.sequence(workflow.rooms.map(player.play))
    } else {
      Future.successful(Nil)
    }

    val pause: Future[List[Either[Error, PlayerStatus]]] = if (workflow.pressPause) {
      Future.sequence(workflow.rooms.map(player.pause))
    } else {
      Future.successful(Nil)
    }

    for {
      volumeUpResult <- handleResult(volumeUp, "volumeUp")
      volumeDownResult <- handleResult(volumeDown, "volumeDown")
      volumeResult <- handleResult(volume, "volume")
      sleepResult <- handleResult(sleep, "sleep")
      playlistResult <- handleResult(playlist, "playlist")
      artistResult <- handleResult(artist, "artist")
      pauseResult <- handleResult(pause, "pause")
      playResult <- handleResult(play, "play")
    } yield {

      Logger.debug(
        s"Trigger Result: $playResult &&  $pauseResult &&  $volumeUpResult &&  $volumeDownResult &&  $volumeResult &&  $playlistResult &&  $artistResult && $sleepResult")

      Right[Error, Boolean](
        playResult &&
          pauseResult &&
          volumeUpResult &&
          volumeDownResult &&
          volumeResult &&
          playlistResult &&
          artistResult &&
          sleepResult)
    }


  }
}
