package services

import generators.PanelTestCaseGenerator
import models.Error
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{EitherValues, MustMatchers, WordSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class PanelServiceLikeSpec
    extends WordSpec
    with GeneratorDrivenPropertyChecks
    with MustMatchers
    with EitherValues
    with PanelTestCaseGenerator
    with MockFactory {

  "PanelServiceLike Happy Path" should {

    "Add Workflows" in {
      forAll(genPanelTestCase) { testCase =>
        val playerService: Player = stub[Player]

        if (testCase.workflow.pressPlay) {
          testCase.workflow.rooms.foreach(room =>
            playerService.play _ when room returns Future.successful(Right(testCase.playerStatus)))
        }
        if (testCase.workflow.pressPause) {
          testCase.workflow.rooms.foreach(room =>
            playerService.pause _ when room returns Future.successful(Right(testCase.playerStatus)))
        }
        if (testCase.workflow.volumeUp) {
          testCase.workflow.rooms.foreach(room =>
            playerService.volumeUp _ when room returns Future.successful(
              Right(testCase.playerStatus)))
        }
        if (testCase.workflow.volumeDown) {
          testCase.workflow.rooms.foreach(room =>
            playerService.volumeDown _ when room returns Future.successful(
              Right(testCase.playerStatus)))
        }
        testCase.workflow.volume.foreach(volume =>
          testCase.workflow.rooms.foreach(room =>
            playerService.volume _ when (room, volume) returns Future.successful(
              Right(testCase.playerStatus))))
        testCase.workflow.playlist.foreach(playlist =>
          testCase.workflow.rooms.foreach(room =>
            playerService.playPlaylist _ when (room, playlist) returns Future.successful(
              Right(testCase.playerStatus))))
        testCase.workflow.artist.foreach(artist =>
          testCase.workflow.rooms.foreach(room =>
            playerService.playArtist _ when (room, artist) returns Future.successful(
              Right(testCase.playerStatus))))
        testCase.workflow.sleep.foreach(sleep =>
          testCase.workflow.rooms.foreach(room =>
            playerService.sleep _ when (room, sleep) returns Future.successful(
              Right(testCase.playerStatus))))

        val panelService = new PanelServiceLike(playerService)
        Await.result(panelService.addWorkflow(testCase.panelId,
                                              testCase.buttonId,
                                              testCase.workflow),
                     5.seconds) mustBe Right(true)

        Await.result(panelService.addWorkflow(testCase.panelId,
                                              testCase.buttonId + "a",
                                              testCase.workflow),
                     5.seconds) mustBe Right(true)

        Await.result(panelService.trigger(testCase.panelId, testCase.buttonId), 5.seconds) mustBe Right(
          true)
      }
    }

    "Trigger Workflow" in {
      forAll(genPanelTestCase) { testCase =>
        val playerService: Player = stub[Player]

        if (testCase.workflow.pressPlay) {
          testCase.workflow.rooms.foreach(room =>
            playerService.play _ when room returns Future.successful(Right(testCase.playerStatus)))
        }
        if (testCase.workflow.pressPause) {
          testCase.workflow.rooms.foreach(room =>
            playerService.pause _ when room returns Future.successful(Right(testCase.playerStatus)))
        }
        if (testCase.workflow.volumeUp) {
          testCase.workflow.rooms.foreach(room =>
            playerService.volumeUp _ when room returns Future.successful(
              Right(testCase.playerStatus)))
        }
        if (testCase.workflow.volumeDown) {
          testCase.workflow.rooms.foreach(room =>
            playerService.volumeDown _ when room returns Future.successful(
              Right(testCase.playerStatus)))
        }
        testCase.workflow.volume.foreach(volume =>
          testCase.workflow.rooms.foreach(room =>
            playerService.volume _ when (room, volume) returns Future.successful(
              Right(testCase.playerStatus))))
        testCase.workflow.playlist.foreach(playlist =>
          testCase.workflow.rooms.foreach(room =>
            playerService.playPlaylist _ when (room, playlist) returns Future.successful(
              Right(testCase.playerStatus))))
        testCase.workflow.artist.foreach(artist =>
          testCase.workflow.rooms.foreach(room =>
            playerService.playArtist _ when (room, artist) returns Future.successful(
              Right(testCase.playerStatus))))
        testCase.workflow.sleep.foreach(sleep =>
          testCase.workflow.rooms.foreach(room =>
            playerService.sleep _ when (room, sleep) returns Future.successful(
              Right(testCase.playerStatus))))

        val panelService = new PanelServiceLike(playerService)
        Await.result(panelService.addWorkflow(testCase.panelId,
                                              testCase.buttonId,
                                              testCase.workflow),
                     5.seconds) mustBe Right(true)

        Await.result(panelService.trigger(testCase.panelId, testCase.buttonId), 5.seconds) mustBe Right(
          true)
      }
    }

  }

  "PanelServiceLike Error Path" should {

    "Add Workflow should return error because of no rooms" in {
      forAll(genPanelTestCase) { testCase =>
        val playerService: Player = stub[Player]

        val panelService = new PanelServiceLike(playerService)
        Await.result(panelService.addWorkflow(testCase.panelId,
          testCase.buttonId,
          testCase.workflow.copy(rooms = Nil)),
          5.seconds) mustBe Left(Error(None, "No rooms specified in workflow"))
      }
    }

    "Trigger Workflow with unknown button" in {
      forAll(genPanelTestCase) { testCase =>
        val playerService: Player = stub[Player]

        val panelService = new PanelServiceLike(playerService)
        Await.result(panelService.addWorkflow(testCase.panelId,
                                              testCase.buttonId + "a",
                                              testCase.workflow),
                     5.seconds) mustBe Right(true)

        Await.result(panelService.trigger(testCase.panelId, testCase.buttonId), 5.seconds) mustBe Left(
          Error(None, s"Could not trigger panel. Unknown button: '${testCase.buttonId}'"))
      }
    }

    "Trigger Workflow with unknown panel" in {
      forAll(genPanelTestCase) { testCase =>
        val playerService: Player = stub[Player]

        val panelService = new PanelServiceLike(playerService)

        Await.result(panelService.trigger(testCase.panelId, testCase.buttonId), 5.seconds) mustBe Left(
          Error(None, s"Could not trigger panel. Unknown panel: '${testCase.panelId}'"))
      }
    }

    "Triggerz Workflow" in {
      forAll(genPanelTestCase) { testCase =>
        println()
        println()
        println()
        println()
        println()
        println()
        println(s"################# $testCase")

        val playerService: Player = stub[Player]

        if (testCase.workflow.pressPlay) {
          testCase.workflow.rooms.foreach(room =>
            playerService.play _ when room returns Future.successful(Left(Error(None, "Error"))))
        }
        if (testCase.workflow.pressPause) {
          testCase.workflow.rooms.foreach(room =>
            playerService.pause _ when room returns Future.successful(Left(Error(None, "Error"))))
        }
        if (testCase.workflow.volumeUp) {
          testCase.workflow.rooms.foreach(room =>
            playerService.volumeUp _ when room returns Future.successful(
              Left(Error(None, "Error"))))
        }
        if (testCase.workflow.volumeDown) {
          testCase.workflow.rooms.foreach(room =>
            playerService.volumeDown _ when room returns Future.successful(
              Left(Error(None, "Error"))))
        }
        testCase.workflow.volume.foreach(volume =>
          testCase.workflow.rooms.foreach(room =>
            playerService.volume _ when (room, volume) returns Future.successful(
              Left(Error(None, "Error")))))
        testCase.workflow.playlist.foreach(playlist =>
          testCase.workflow.rooms.foreach(room =>
            playerService.playPlaylist _ when (room, playlist) returns Future.successful(
              Left(Error(None, "Error")))))
        testCase.workflow.artist.foreach(artist =>
          testCase.workflow.rooms.foreach(room =>
            playerService.playArtist _ when (room, artist) returns Future.successful(
              Left(Error(None, "Error")))))

        testCase.workflow.sleep.foreach(sleep =>
          testCase.workflow.rooms.foreach(room =>
            playerService.sleep _ when (room, sleep) returns Future.successful(
              Left(Error(None, "Error")))))

        val panelService = new PanelServiceLike(playerService)
        Await.result(panelService.addWorkflow(testCase.panelId,
                                              testCase.buttonId,
                                              testCase.workflow),
                     5.seconds) mustBe Right(true)

        Await.result(panelService.trigger(testCase.panelId, testCase.buttonId), 5.seconds) mustBe Right(
          false)
      }
    }
  }

}
