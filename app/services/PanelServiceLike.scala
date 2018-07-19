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

  override def addWorkflow(panelId: String,
                           buttonId: String,
                           workflow: Workflow): Future[Either[Error, Boolean]] =
    panels.get(panelId) match {
      case Some(buttons) =>
        buttons.put(buttonId, workflow)
        Future.successful(Right(true))
      case None =>
        panels.put(panelId, mutable.Map(buttonId -> workflow))
        Future.successful(Right(true))
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
    if (workflow.rooms.isEmpty) {
      Future.successful(Left(Error(None, "No rooms specified in workflow")))
    } else {

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

      def handleResult(result: Future[List[Either[Error, PlayerStatus]]], action: String) = {

        result.foreach(z => Logger.debug(s"handleResult $action: BEFORE $z"))
        val t1: Future[List[Either[Error, PlayerStatus]]] = result.map(
          _.filter(t => t.isLeft))
        t1.foreach(z => Logger.debug(s"handleResult $action: T1 $t1"))
        val t2 = t1.map(_.isEmpty)
        t2.foreach(z => Logger.debug(s"handleResult $action: T2 $t2"))

        val x = result.map(
          _.filter(t => t.isLeft)
            .foreach(e => Logger.error(s"handleResult $action: Unable to trigger '$action' in workflow: $e: $workflow"))
            .isEmpty)

        x.foreach(z => Logger.debug(s"handleResult $action: X $z"))

        x
      }

      val volume: Future[List[Either[Error, PlayerStatus]]] = Future.sequence(
        workflow.rooms.flatMap(room => workflow.volume.map(v => player.volume(room, v))))

      val playlist: Future[List[Either[Error, PlayerStatus]]] = Future.sequence(
        workflow.rooms.flatMap(room => workflow.playlist.map(p => player.playPlaylist(room, p))))

      val artist: Future[List[Either[Error, PlayerStatus]]] = Future.sequence(
        workflow.rooms.flatMap(room => workflow.artist.map(a => player.playArtist(room, a))))

      for {
        playResult <- handleResult(play, "play")
        pauseResult <- handleResult(pause, "pause")
        volumeUpResult <- handleResult(volumeUp, "volumeUp")
        volumeDownResult <- handleResult(volumeDown, "volumeDown")
        volumeResult <- handleResult(volume, "volume")
        playlistResult <- handleResult(playlist, "playlist")
        artistResult <- handleResult(artist, "artist")
      } yield {

        Logger.debug(s"Trigger Result: $playResult &&  $pauseResult &&  $volumeUpResult &&  $volumeDownResult &&  $volumeResult &&  $playlistResult &&  $artistResult")

        Right[Error, Boolean](
          playResult &&
            pauseResult &&
            volumeUpResult &&
            volumeDownResult &&
            volumeResult &&
            playlistResult &&
            artistResult)
      }
    }
  }

}
