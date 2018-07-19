package generators

import models.Workflow
import org.scalacheck.Gen

trait WorkflowGenerator {

  def genWorkflow(): Gen[Workflow] = for {
    rooms <- Gen.nonEmptyListOf[String](Gen.alphaNumStr)
    pressPlay <- Gen.oneOf(true, false)
    pressPause <- Gen.oneOf(true, false)
    volumeUp <- Gen.oneOf(true, false)
    volumeDown <- Gen.oneOf(true, false)
    volume <- Gen.option(Gen.chooseNum(0, 100))
    playlist <- Gen.option(Gen.alphaNumStr)
    artist <- Gen.option(Gen.alphaNumStr)
  } yield Workflow(rooms,
    pressPlay,
    pressPause,
    volumeUp,
    volumeDown,
    volume,
    playlist,
    artist)

}
