package services

import generators.{PlayerListTestCaseGenerator, TestCaseGenerator}
import models.{Error, Players}
import org.scalatest.{EitherValues, MustMatchers, WordSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.Configuration
import play.core.server.Server
import play.api.routing.sird._
import play.api.mvc._
import play.api.test._

import scala.concurrent.{Await, Future}
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
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
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
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
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
              BadRequest(s"Query String: ${request.getQueryString("level")} != $expectedVolume")
            }
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
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
              BadRequest(s"Query String: ${request.getQueryString("level")} != $expectedVolume")
            }
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.volumeDown(testCase.room), 10.seconds)
            result mustBe Right(testCase.playerStatus)
          }
        }
      }
    }

    "Call the Volume endpoint" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Volume") => Action { request =>
            val expectedVolume = testCase.playerStatus.volume.toString
            if (request.getQueryString("level").contains(expectedVolume)) {
              Ok(<state>pause</state>)
            } else {
              BadRequest(s"Query String: ${request.getQueryString("level")} != $expectedVolume")
            }
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.volume(testCase.room, testCase.playerStatus.volume), 10.seconds)
            result mustBe Right(testCase.playerStatus)
          }
        }
      }
    }

    "Call the Add endpoint to queue Playlist" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Add") => Action { request =>
            if (request.getQueryString("playlist").contains(testCase.playlist)) {
              Ok("...")
            } else {
              BadRequest(s"Query String: ${request.getQueryString("playlist")} != ${testCase.playlist}")
            }
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.playPlaylist(testCase.room, testCase.playlist), 10.seconds)
            result mustBe Right(testCase.playerStatus)
          }
        }
      }
    }

    "Call the Add endpoint to queue Artist" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Add") => Action { request =>
            if (request.getQueryString("artist").contains(testCase.artist)) {
              Ok("...")
            } else {
              BadRequest(s"Query String: ${request.getQueryString("artist")} != ${testCase.artist}")
            }
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.playArtist(testCase.room, testCase.artist), 10.seconds)
            result mustBe Right(testCase.playerStatus)
          }
        }
      }
    }
  }

  "Exception Path" should {

    "Call the Play endpoint and handle an exception" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Play") => Action {
            throw new Exception("No")
            InternalServerError("This should not happen")
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.play(testCase.room), 10.seconds)
            result.isLeft mustBe true
          }
        }
      }
    }

    "Call the Pause endpoint and handle an exception" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Pause") => Action {
            throw new Exception("No")
            InternalServerError("This should not happen")
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.pause(testCase.room), 10.seconds)
            result.isLeft mustBe true
          }
        }
      }
    }

    "Call the Volume Up endpoint and handle an exception" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Volume") => Action {
            throw new Exception("No")
            InternalServerError("This should not happen")
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.volumeUp(testCase.room), 10.seconds)
            result.isLeft mustBe true
          }
        }
      }
    }

    "Call the Volume Down endpoint and handle an exception" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Volume") => Action {
            throw new Exception("No")
            InternalServerError("This should not happen")
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.volumeDown(testCase.room), 10.seconds)
            result.isLeft mustBe true
          }
        }
      }
    }

    "Call the Volume endpoint and handle an exception" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Volume") => Action {
            throw new Exception("No")
            InternalServerError("This should not happen")
          }
          case GET(p"/Status") => Action {
            Ok(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.volume(testCase.room, testCase.playerStatus.volume), 10.seconds)
            result.isLeft mustBe true
          }
        }
      }
    }

    "Call the Pause endpoint and handle invalid XML" in {
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
              }}</state> <volume>
                {testCase.playerStatus.volume}
              </volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.pause(testCase.room), 10.seconds)
            result.isLeft mustBe true
          }
        }
      }
    }

    "Call the Pause endpoint and handle missing XML values" in {
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
              }}</state>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.pause(testCase.room), 10.seconds)
            result.isLeft mustBe true
          }
        }
      }
    }

    "Call the Pause endpoint and return non 200" in {
      forAll(genTestCase) { testCase =>
        Server.withRouterFromComponents() { components =>
          import Results._
          import components.{defaultActionBuilder => Action}
        {
          case GET(p"/Pause") => Action {
            Ok(<state>pause</state>)
          }
          case GET(p"/Status") => Action {
            BadRequest(<status>
              <state>{if (testCase.playerStatus.isPlaying) {
                "play"
              } else {
                "pause"
              }}</state> <volume>{testCase.playerStatus.volume}</volume><sleep>{testCase.playerStatus.sleep.getOrElse("")}</sleep>
            </status>)
          }
        }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val configuration = Configuration(s"bluesound.players.${testCase.room}" -> "")
            val player = new PlayerLike(client, configuration) {
              override def getPlayers: Future[Either[Error, Players]] = Future.successful(Right(Players(Map(testCase.room -> ""))))
            }
            val result = Await.result(player.pause(testCase.room), 10.seconds)
            result.isLeft mustBe true
          }
        }
      }
    }
  }

}
