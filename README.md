# Cardshifter

- Master branch: [![Build Status](https://travis-ci.org/Cardshifter/Cardshifter.svg?branch=master)](https://travis-ci.org/Cardshifter/Cardshifter?branch=master)  

- Develop branch: [![Build Status](https://travis-ci.org/Cardshifter/Cardshifter.svg?branch=develop)](https://travis-ci.org/Cardshifter/Cardshifter?branch=develop)

## What is Cardshifter?

Cardshifter is an open-source, online Trading Card Game (TCG). The idea was born in summer of 2014 when a handful of software developers and programming enthusiasts from [Code Review Stack Exchange](http://codereview.stackexchange.com/) were tossing around ideas around on how to come up with an innovative, flexible and fun game. 

The primary game client as of August 2015 is web based and is hosted at http://play.cardshifter.com. Its source code can be found in the [Cardshifter/HTML-Client](https://github.com/Cardshifter/HTML-Client) repository. The server is cross-platform compatible and runs on Windows, OS X and Linux. There are plans to feature a mobile client as well. The game is early in development, but we are working hard on adding new gameplay features, improving the user interface, and will eventually feature original art from content creators who have since joined the project.

- [Official Website](http://www.Cardshifter.com/)

## Playing the game

To play the game, [download the latest release](https://github.com/Cardshifter/Cardshifter/releases). Start the client jar using the computer console/terminal with `java -jar cardshifter-fx-<version>.jar` or by saving to your computer and simply double-clicking `cardshifter-fx-<version>.jar`. There is usually a server running at `dwarftowers.com` port `4242` that you may connect to and play with other players as well as AI players. If this is your first time playing Cardshifter, you may want to have a look at the [Wiki](https://github.com/Cardshifter/Cardshifter/wiki).

## How can I get involved?

If you are interested in helping with the project, there are a number of ways you can go about it:

- Open a [Github Issue](https://github.com/Cardshifter/Cardshifter/issues).
 
- Find an issue you would like to work on a create a pull request or branch. Issues tagged "Help Wanted" are particularly good for new contributors to work on. Make sure you read the [Developer guidelines](https://github.com/Cardshifter/Cardshifter/wiki/Developer-Guidelines). We also have a detailed guide on [Getting started with development](https://github.com/Cardshifter/Cardshifter/wiki/1--Getting-started-with-development) which should have you up and coding in no time!

- Join the [chatroom](http://chat.stackexchange.com/rooms/16134/tcg-creation) on Stack Exchange and discuss how you can contribute. _Please note that a minimum of 20 reputation on any Stack Exchange site (including Stack Overflow) is required before you can post in chat._

- Check the [Cardshifter Wiki](https://github.com/Cardshifter/Cardshifter/wiki) for more detailed technical information about the project.

If you are not a developer, you can also help by playing the game and [reporting any bug](http://www.Cardshifter.com/report-bug.html) you find or [requesting new features](http://www.Cardshifter.com/request-feature.html) you'd like to see added to the game. You can even become a game designer if you'd like to contribute gameplay ideas and/or game content, please join the [chatroom](http://chat.stackexchange.com/rooms/16134/tcg-creation) to discuss in more detail. 

### Submodules
 
 - `cardshifter-api` - Classes for data being sent between client and server.
 - `cardshifter-core` - Contains our core TCG mod implementation, as well as various features for inclusion in the server and the JavaFX client.
 - `cardshifter-fx` - JavaFX Client.
 - `cardshifter-modapi` - Contains the core Entity-Component-System code and several components and systems for some common TCG features.
 - `cardshifter-server` - Server for the game.
 - `cardshifter-test` - Code to simplify testing mods.
 - `gdx / core` - libGDX core game client code.
 - `gdx / android, ios, desktop, html5` - Platform specific libGDX client code.
 - `Documentation` - Guides for game developers and content creators.
 - `extra-resources` - Game content and configurations for the various mods available in Cardshifter. 

## License

All files in this repository, unless explicitly specified otherwise in the files itself 
are licensed under Apache Software License, Version 2.0 (the "License");

Copyright 2014-2015 Simon Forsberg, Frank van Heeswijk, Francis Veilleux-Gaboury, Matt Olsen, Jeremiah Smith, Jacob Wahlgren

You may not use the files except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
