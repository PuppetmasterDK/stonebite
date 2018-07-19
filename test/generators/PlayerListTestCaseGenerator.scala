package generators

import models.{PlayerListTestCase, Players}
import org.scalacheck.Gen

trait PlayerListTestCaseGenerator extends ErrorGenerator{

  def genPlayerListTestCase: Gen[PlayerListTestCase] = for {
    error <- genError()
  } yield PlayerListTestCase(Players(Map("alrum" -> "")), error)

}
