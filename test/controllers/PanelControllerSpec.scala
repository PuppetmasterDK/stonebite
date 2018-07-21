package controllers

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import generators.{PanelTestCaseGenerator, PlayerListTestCaseGenerator, TestCaseGenerator}
import models._
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{EitherValues, MustMatchers, WordSpec}
import play.api.Configuration
import play.api.http.Status
import play.api.libs.concurrent.Futures._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentType, status, stubControllerComponents}
import protocols.JsonProtocol
import services.{PanelService, Player}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Future, TimeoutException}

class PanelControllerSpec extends WordSpec
  with GeneratorDrivenPropertyChecks
  with PanelTestCaseGenerator
  with MockFactory
  with EitherValues
  with MustMatchers
  with JsonProtocol {

  val configuration = Configuration("bluesound.timeout" -> 5)
  val timeoutConfiguration = Configuration("bluesound.timeout" -> 1)

  implicit val actorSystem = ActorSystem("testActorSystem", ConfigFactory.load())
  implicit val timeout: Timeout = 5.seconds

  "PanelController Happy Path" should {

    "call the trigger method and return the status" in {
      forAll(genPanelTestCase) { testCase =>
        val panelService = stub[PanelService]
        panelService.trigger _ when(testCase.panelId, testCase.buttonId) returns Future.successful(Right(true))

        val controller = new PanelController(stubControllerComponents(), configuration, panelService)

        val result = controller
          .handlePanelCall(testCase.panelId, testCase.buttonId)
          .apply(FakeRequest(GET, s"/panel/${testCase.panelId}/button/${testCase.buttonId}"))


        status(result) mustBe Status.OK
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Map("success" -> true))
      }
    }
  }

  //
  // Error Testing
  //
  "PanelController Error Path" should {

    "call the trigger method and return the error" in {
      forAll(genPanelTestCase) { testCase =>
        val panelService = stub[PanelService]
        panelService.trigger _ when(testCase.panelId, testCase.buttonId) returns Future.successful(Left(testCase.error))

        val controller = new PanelController(stubControllerComponents(), configuration, panelService)

        val result = controller
          .handlePanelCall(testCase.panelId, testCase.buttonId)
          .apply(FakeRequest(GET, s"/panel/${testCase.panelId}/button/${testCase.buttonId}"))


        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(testCase.error)
      }
    }

  }

  //
  // Exception Testing
  //
  "PanelController Exception Path" should {

    "call the trigger method and return the exception" in {
      forAll(genPanelTestCase) { testCase =>
        val panelService = stub[PanelService]
        panelService.trigger _ when(testCase.panelId, testCase.buttonId) returns Future.failed(new Exception("Random exception!"))

        val controller = new PanelController(stubControllerComponents(), configuration, panelService)

        val result = controller
          .handlePanelCall(testCase.panelId, testCase.buttonId)
          .apply(FakeRequest(GET, s"/panel/${testCase.panelId}/button/${testCase.buttonId}"))


        status(result) mustBe Status.INTERNAL_SERVER_ERROR
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PanelService encountered an exception. Try again later."))
      }
    }

  }

  //
  // Timeout testing
  //
  "PanelController Timeout Path" should {

    "call the trigger method and return the timeout" in {
      forAll(genPanelTestCase) { testCase =>
        val panelService = stub[PanelService]
        panelService.trigger _ when(testCase.panelId, testCase.buttonId) returns Future.failed(new TimeoutException("Timeout!"))

        val controller = new PanelController(stubControllerComponents(), configuration, panelService)

        val result = controller
          .handlePanelCall(testCase.panelId, testCase.buttonId)
          .apply(FakeRequest(GET, s"/panel/${testCase.panelId}/button/${testCase.buttonId}"))


        status(result) mustBe Status.GATEWAY_TIMEOUT
        contentType(result).contains("application/json") mustBe true
        contentAsJson(result) mustBe Json.toJson(Error(None, "PanelService did not respond. Try again later."))
      }
    }

  }

}
