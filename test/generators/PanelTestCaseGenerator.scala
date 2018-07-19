package generators

import models.{PanelTestCase, TestCase}
import org.scalacheck.Gen

trait PanelTestCaseGenerator
    extends ErrorGenerator
    with WorkflowGenerator
    with PlayerStatusGenerator {

  def genPanelTestCase: Gen[PanelTestCase] =
    for {
      panelId      <- Gen.alphaNumStr.suchThat(!_.isEmpty)
      buttonId     <- Gen.alphaNumStr.suchThat(!_.isEmpty)
      error        <- genError()
      workflow     <- genWorkflow()
      playerStatus <- genPlayerStatus()
    } yield PanelTestCase(panelId, buttonId, error, workflow, playerStatus)

}
