package services

import com.google.inject.ImplementedBy
import models.{Error, PlayerStatus, Players}

import scala.concurrent.Future

@ImplementedBy(classOf[PlayerLike])
trait Player {

  def getPlayers: Future[Either[Error, Players]]
  def play(room: String): Future[Either[Error, PlayerStatus]]
  def pause(room: String): Future[Either[Error, PlayerStatus]]
  def volumeUp(room: String): Future[Either[Error, PlayerStatus]]
  def volumeDown(room: String): Future[Either[Error, PlayerStatus]]
  def getStatus(room: String): Future[Either[Error, PlayerStatus]]

}
