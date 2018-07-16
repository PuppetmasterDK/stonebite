package services

import generators.{PlayerListTestCaseGenerator, TestCaseGenerator}
import org.scalatest.{EitherValues, MustMatchers, WordSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.Configuration
import play.core.server.Server
import play.api.routing.sird._
import play.api.mvc._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class PlayerLikeSpec extends WordSpec
  with GeneratorDrivenPropertyChecks
  with MustMatchers
  with EitherValues
  with TestCaseGenerator
  with PlayerListTestCaseGenerator {

  private def volumeDown(volume: Int): Int =
    Math.min(100, (Math.round(volume / 5) * 5) - 5)

  private def volumeUp(volume: Int): Int =
    Math.min(100, (Math.round(volume / 5) * 5) + 5)

  "PlayerLike Happy Path" should {

    "Call Get Players" in {
      forAll(genPlayerListTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Play") => Action {
            Ok(<state>play</state>)
          }
        }
        } { implicit port =>
          val distinctPlayers = testCase.players.copy(players = testCase.players.players.distinct)
          WsTestClient.withClient { client =>
            val configuration = Configuration(distinctPlayers.players.map(p => s"bluesound.players.${p}" -> p).toList: _*)
            val player = new PlayerLike(client, configuration)
            val result = Await.result(player.getPlayers, 10.seconds)
            result mustBe Right(distinctPlayers.copy(players = distinctPlayers.players.sorted))
          }
        }
      }
    }

    "Call the Play endpoint" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Play") => Action {
            Ok(<state>play</state>)
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration)
            val result = Await.result(player.play(testCase.room), 10.seconds)
            result mustBe Right(testCase.playerStatus)
          }
        }
      }
    }

    "Call the Pause endpoint" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Pause") => Action {
            Ok(<state>pause</state>)
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration)
            val result = Await.result(player.pause(testCase.room), 10.seconds)
            result mustBe Right(testCase.playerStatus)
          }
        }
      }
    }

    "Call the VolumeUp endpoint" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Volume") => Action { request =>
            val expectedVolume = volumeUp(testCase.playerStatus.volume).toString
            if (request.getQueryString("level").contains(expectedVolume)) {
              Ok(<state>pause</state>)
            } else {
              BadRequest(s"Query String: ${request.getQueryString("level")} != ${expectedVolume}")
            }
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration)
            val result = Await.result(player.volumeUp(testCase.room), 10.seconds)
            result mustBe Right(testCase.playerStatus)
          }
        }
      }
    }

    "Call the VolumeDown endpoint" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Volume") => Action { request =>
            val expectedVolume = volumeDown(testCase.playerStatus.volume).toString
            if (request.getQueryString("level").contains(expectedVolume)) {
              Ok(<state>pause</state>)
            } else {
              BadRequest(s"Query String: ${request.getQueryString("level")} != ${expectedVolume}")
            }
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration)
            val result = Await.result(player.volumeDown(testCase.room), 10.seconds)
            result mustBe Right(testCase.playerStatus)
          }
        }
      }
    }
  }

}
