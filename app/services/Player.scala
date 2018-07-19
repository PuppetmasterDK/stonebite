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
  def volume(room: String, volume: Int): Future[Either[Error, PlayerStatus]]
  def getStatus(room: String): Future[Either[Error, PlayerStatus]]
  def playPlaylist(room: String, playlist: String): Future[Either[Error, PlayerStatus]]
  def playArtist(room: String, artist: String): Future[Either[Error, PlayerStatus]]
  def sleep(room: String, time: Int): Future[Either[Error, PlayerStatus]]

}
