# StoneBite
[![Build Status](https://travis-ci.org/PuppetmasterDK/stonebite.svg?branch=master)](https://travis-ci.org/PuppetmasterDK/stonebite)
[![Coverage Status](https://coveralls.io/repos/github/PuppetmasterDK/stonebite/badge.svg?branch=master)](https://coveralls.io/github/PuppetmasterDK/stonebite?branch=master)


This simple service wraps the BlueSound players with a small web API.

It have some convenience methods to ensure that pressing play does not give a break in the sound even when playing from streaming services.

The endpoints are based on the API discussion here: https://helpdesk.bluesound.com/discussions/viewtopic.php?t=2293.

## Configuration
The configuration file contains the players and their addresses and a timeout in seconds
```
bluesound = {
  timeout = 5
  players = {
    alrum: "http://192.168.0.103:11000"
    masterbedroom: "http://192.168.0.130:11000"
    voksenbad: "http://192.168.0.102:11000"
  }
}
```

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
* Auto discovery of players
* More control endpoints
* Consider mapping from switch ID to room
* Setup Guice with modules

# License
This project is using the MIT License. See [LICENSE](LICENSE).

This project is not affiliated with BlueSound or NAD in any way.

