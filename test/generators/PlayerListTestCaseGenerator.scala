package generators

import models.{PlayerListTestCase, Players}
import org.scalacheck.Gen

trait PlayerListTestCaseGenerator extends ErrorGenerator{

  def genPlayerListTestCase: Gen[PlayerListTestCase] = for {
    list <- Gen.nonEmptyListOf(Gen.alphaNumStr.suchThat(!_.isEmpty))
    error <- genError()
  } yield PlayerListTestCase(Players(list), error)

}
