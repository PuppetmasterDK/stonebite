package models

/**
  * This is used to store what happens when a button is pressed
  *
  * @param rooms      A list of rooms to interact with
  * @param pressPlay  Should we press the play button?
  * @param pressPause Should we press the pause button?
  * @param volumeUp   Should we turn the volume up?
  * @param volumeDown Should we turn the volume down?
  * @param volume     Should we set the volume to a specific amount?
  * @param playlist   Should we change to a playlist
  * @param artist     Should we play a specific artist?
  * @param sleep      Should we enable the sleep function?
  */
case class Workflow(
    rooms: List[String] = Nil,
    pressPlay: Boolean = false,
    pressPause: Boolean = false,
    volumeUp: Boolean = false,
    volumeDown: Boolean = false,
    volume: Option[Int] = None,
    playlist: Option[String] = None,
    artist: Option[String] = None,
    sleep: Option[Int] = None
)
