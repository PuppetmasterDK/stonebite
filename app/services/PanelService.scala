package services

import com.google.inject.ImplementedBy
import models.{Error, PlayerStatus, Workflow}

import scala.concurrent.Future

@ImplementedBy(classOf[PanelServiceLike])
trait PanelService {

  def addWorkflow(panelId: String, buttonId: String, workflow: Workflow): Future[Either[Error, Boolean]]
  def trigger(panelId: String, buttonId: String): Future[Either[Error, Boolean]]

}
