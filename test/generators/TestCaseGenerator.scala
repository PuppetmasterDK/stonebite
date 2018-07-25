package generators

import models.TestCase
import org.scalacheck.Gen

trait TestCaseGenerator extends PlayerStatusGenerator with ErrorGenerator {

  def genTestCase: Gen[TestCase] =
    for {
      room         <- Gen.alphaNumStr.suchThat(!_.isEmpty)
      playerStatus <- genPlayerStatus()
      error        <- genError(playerStatus)
      playlist     <- Gen.alphaNumStr.suchThat(!_.isEmpty)
      artist       <- Gen.alphaNumStr.suchThat(!_.isEmpty)
      sleep        <- Gen.oneOf(0, 15, 30, 45, 60, 90)
    } yield TestCase(room, playerStatus, error, playlist, artist, sleep)

}
