package generators

import models.PlayerStatus
import org.scalacheck.Gen

trait PlayerStatusGenerator {

  def genPlayerStatus(): Gen[PlayerStatus] =
    for {
      isPlaying <- Gen.oneOf(false, true)
      volume <- Gen.choose(Int.MinValue, Int.MaxValue)
    } yield PlayerStatus(isPlaying, volume)

}
