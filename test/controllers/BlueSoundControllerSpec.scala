package controllers

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import generators.{PlayerListTestCaseGenerator, TestCaseGenerator}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{EitherValues, MustMatchers, WordSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import services.Player
import models._
import play.api.Configuration
import play.api.http.Status
import play.api.libs.concurrent.Futures._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentType, status, stubControllerComponents}
import protocols.JsonProtocol

import scala.concurrent.{Future, TimeoutException}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class BlueSoundControllerSpec extends WordSpec
  with GeneratorDrivenPropertyChecks
  with TestCaseGenerator
  with PlayerListTestCaseGenerator
  with MockFactory
  with EitherValues
  with MustMatchers
  with JsonProtocol {

  val configuration = Configuration("bluesound.timeout" -> 5)
  val timeoutConfiguration = Configuration("bluesound.timeout" -> 1)

  implicit val actorSystem = ActorSystem("testActorSystem", ConfigFactory.load())
  implicit val timeout: Timeout = 5.seconds

  "BlueSoundController Happy Path" should {

    "call the play method and return the player status" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.play _ when testCase.room returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .startToPlay(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))


        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }

    "call the pause method and return the player status" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.pause _ when testCase.room returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .pause(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/pause"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }

    "call the volumeUp method and return the player status" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volumeUp _ when testCase.room returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volumeUp(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/volume/up"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }

    "call the volumeDown method and return the player status" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volumeDown _ when testCase.room returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volumeDown(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/volume/down"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }

    "call the status method and return the player status" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.getStatus _ when testCase.room returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .status(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/volume/down"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }

    "call the getPlayers method and return the players" in {
      forAll(genPlayerListTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.getPlayers _ when() returns Future.successful(Right(testCase.players))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .getPlayers()
          .apply(FakeRequest(GET, s"/players/"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.players)
      }
    }

    "call the volume method and return the player" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volume _ when(testCase.room, testCase.playerStatus.volume) returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volume(testCase.room, testCase.playerStatus.volume)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/volume/${testCase.playerStatus.volume}"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }

    "call the playlist method and return the player" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.playPlaylist _ when(testCase.room, testCase.playlist) returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .playlist(testCase.room, testCase.playlist)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/playlist/${testCase.playlist}"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }

    "call the artist method and return the player" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.playArtist _ when(testCase.room, testCase.artist) returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .artist(testCase.room, testCase.artist)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/artist/${testCase.artist}"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }

    "call the sleep method and return the player" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.sleep _ when(testCase.room, testCase.sleep) returns Future.successful(Right(testCase.playerStatus))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .sleep(testCase.room, testCase.sleep)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/sleep/${testCase.sleep}"))

        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.playerStatus)
      }
    }
  }

  //
  // Error Testing
  //
  "BlueSoundController Error Path" should {


    "call the play method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.play _ when testCase.room returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .startToPlay(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the pause method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.pause _ when testCase.room returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .pause(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the volumeUp method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volumeUp _ when testCase.room returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volumeUp(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the volumeDown method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volumeDown _ when testCase.room returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volumeDown(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the volume method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volume _ when (testCase.room, testCase.playerStatus.volume) returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volume(testCase.room, testCase.playerStatus.volume)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the playlist method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.playPlaylist _ when (testCase.room, testCase.playlist) returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .playlist(testCase.room, testCase.playlist)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/playlist/${testCase.playlist}"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the artist method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.playArtist _ when (testCase.room, testCase.artist) returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .artist(testCase.room, testCase.artist)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/artist/${testCase.artist}"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the sleep method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.sleep _ when (testCase.room, testCase.sleep) returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .sleep(testCase.room, testCase.sleep)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/sleep/${testCase.sleep}"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the status method and handle an error" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.getStatus _ when testCase.room returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .status(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

    "call the getPlayers method and handle an error" in {
      forAll(genPlayerListTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.getPlayers _ when() returns Future.successful(Left(testCase.error))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .getPlayers()
          .apply(FakeRequest(GET, s"/players/"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }
  }

  //
  // Exception Testing
  //
  "BlueSoundController Exception Path" should {

    "call the play method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.play _ when testCase.room returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .startToPlay(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the pause method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.pause _ when testCase.room returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .pause(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the volumeUp method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volumeUp _ when testCase.room returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volumeUp(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the volumeDown method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volumeDown _ when testCase.room returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volumeDown(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the volume method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volume _ when (testCase.room, testCase.playerStatus.volume) returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .volume(testCase.room, testCase.playerStatus.volume)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the playlist method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.playPlaylist _ when (testCase.room, testCase.playlist) returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .playlist(testCase.room, testCase.playlist)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/playlist/${testCase.playlist}"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the artist method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.playArtist _ when (testCase.room, testCase.artist) returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .artist(testCase.room, testCase.artist)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/artist/${testCase.artist}"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the sleep method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.sleep _ when (testCase.room, testCase.sleep) returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .sleep(testCase.room, testCase.sleep)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/sleep/${testCase.sleep}"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the status method and handle an exception" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.getStatus _ when (testCase.room) returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .status(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

    "call the getPlayers method and handle an error" in {
      forAll(genPlayerListTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.getPlayers _ when() returns Future.failed(new Exception("Random exception!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .getPlayers()
          .apply(FakeRequest(GET, s"/players/"))

        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService encountered an exception. Try again later."))
      }
    }

  }

  //
  // Timeout testing
  //
  "BlueSoundController Timeout Path" should {

    "call the play method and handle a timeout" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.play _ when testCase.room returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .startToPlay(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) mustBe Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

    "call the pause method and handle a timeout" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.pause _ when testCase.room returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, timeoutConfiguration)

        val result = controller
          .pause(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) == Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json")
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

    "call the volumeUp method and handle a timeout" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volumeUp _ when testCase.room returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, timeoutConfiguration)

        val result = controller
          .volumeUp(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) == Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json")
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

    "call the volumeDown method and handle a timeout" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volumeDown _ when testCase.room returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, timeoutConfiguration)

        val result = controller
          .volumeDown(testCase.room)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) == Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json")
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

    "call the volume method and handle a timeout" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.volume _ when (testCase.room, testCase.playerStatus.volume) returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, timeoutConfiguration)

        val result = controller
          .volume(testCase.room, testCase.playerStatus.volume)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/play"))

        status(result) == Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json")
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

    "call the playlist method and handle a timeout" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.playPlaylist _ when (testCase.room, testCase.playlist) returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, timeoutConfiguration)

        val result = controller
          .playlist(testCase.room, testCase.playlist)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/playlist/${testCase.playlist}"))

        status(result) == Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json")
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

    "call the artist method and handle a timeout" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.playArtist _ when (testCase.room, testCase.artist) returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, timeoutConfiguration)

        val result = controller
          .artist(testCase.room, testCase.artist)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/artist/${testCase.artist}"))

        status(result) == Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json")
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

    "call the sleep method and handle a timeout" in {
      forAll(genTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.sleep _ when (testCase.room, testCase.sleep) returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, timeoutConfiguration)

        val result = controller
          .sleep(testCase.room, testCase.sleep)
          .apply(FakeRequest(GET, s"/players/${testCase.room}/sleep/${testCase.sleep}"))

        status(result) == Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json")
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

    "call the getPlayers method and handle a timeout" in {
      forAll(genPlayerListTestCase) { testCase =>
        val playerService = stub[Player]
        playerService.getPlayers _ when() returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new BlueSoundController(stubControllerComponents(), playerService, configuration)

        val result = controller
          .getPlayers()
          .apply(FakeRequest(GET, s"/players/"))

        status(result) == Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json")
        contentAsJson(result) mustBe Json.toJson(Error(None, "PlayerService did not respond. Try again later."))
      }
    }

  }

}
