package generators

import models.{PlayerStatus, Error}
import org.scalacheck.Gen

trait ErrorGenerator extends PlayerStatusGenerator {

  def genError(playerStatus: PlayerStatus): Gen[Error] = for {
    maybePlayerStatus <- Gen.oneOf(None, Option(playerStatus))
    message <- Gen.alphaNumStr
  } yield Error(maybePlayerStatus, message)

  def genError(): Gen[Error] = for {
    playerStatus <- genPlayerStatus()
    maybePlayerStatus <- Gen.oneOf(None, Option(playerStatus))
    message <- Gen.alphaNumStr
  } yield Error(maybePlayerStatus, message)

}
