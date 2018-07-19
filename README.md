# StoneBite
[![Build Status](https://travis-ci.org/PuppetmasterDK/stonebite.svg?branch=master)](https://travis-ci.org/PuppetmasterDK/stonebite)
[![Coverage Status](https://coveralls.io/repos/github/PuppetmasterDK/stonebite/badge.png?branch=master)](https://coveralls.io/github/PuppetmasterDK/stonebite?branch=master)


This simple service wraps the BlueSound players with a small web API. It automatically detects the players on the network

It have some convenience methods to ensure that pressing play does not give a break in the sound even when playing from streaming services.

The endpoints are based on the API discussion here: https://helpdesk.bluesound.com/discussions/viewtopic.php?t=2293.
And some more digging into network traffic

## Further API Endpoints
Playlists with all playlists:
```
http://192.168.0.103:11000/Playlists?imported=1
```

Play playlist (aka add it to the current playlist, but clear the existing and start by the first song):
```
http://192.168.0.103:11000/Add?service=LocalMusic&playnow=1&playlistid=Musik+til+reception&clear=1&listindex=0&playlist=Musik+til+reception
``` 

Play artist:
```
URL	http://192.168.0.103:11000/Add?service=LocalMusic&playnow=1&where=last&all=1&listindex=0&nextlist=1&cursor=last&artist=Adele
```

Sleep (Call multiple times to set sleep):
```
http://192.168.0.130:11000/Sleep
```

## Configuration
The configuration file contains a timeout in seconds
```
bluesound = {
  timeout = 5
}
```

The players are found using the mDNS group.

## Running
Just run the Play app:
```
sbt run
```

Then navigate to: ```http://localhost:9000``` this will redirect you to the documentation of the endpoints

## Running the tests:
To run the tests with no coverage information collected:
```
sbt test
```
Run with coverage:
```
sbt clean coverage test coverageReport
```

# Example:
```
curl -X GET "http://localhost:9000/bluesound/alrum/play" -H "accept: application/json"
```

# To Do
* CRUD for workflows
* Can it be used for a Doorbell?
* Consider mapping from switch ID to room
* Setup Guice with modules

# License
This project is using the MIT License. See [LICENSE](LICENSE).

This project is not affiliated with BlueSound or NAD in any way.

