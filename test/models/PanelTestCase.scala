package models

case class PanelTestCase(panelId: String, buttonId: String, error: Error, workflow: Workflow, playerStatus: PlayerStatus)
