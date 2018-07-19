package generators

import models.PlayerStatus
import org.scalacheck.Gen

trait PlayerStatusGenerator {

  def genPlayerStatus(): Gen[PlayerStatus] =
    for {
      isPlaying <- Gen.oneOf(false, true)
      volume <- Gen.choose(0, 100)
      sleep <- Gen.option(Gen.oneOf(15, 30, 45, 60, 90))
    } yield PlayerStatus(isPlaying, volume, sleep)

}
