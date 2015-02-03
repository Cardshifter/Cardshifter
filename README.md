Cardshifter
===========

_Updated 2015-02-03_

- Master branch: [![Build Status](https://travis-ci.org/Cardshifter/Cardshifter.svg?branch=master)](https://travis-ci.org/Cardshifter/Cardshifter?branch=master)  

- Develop branch: [![Build Status](https://travis-ci.org/Cardshifter/Cardshifter.svg?branch=develop)](https://travis-ci.org/Cardshifter/Cardshifter?branch=develop)

####What is Cardshifter?
------------------------

Cardshifter is an open-source, online Trading Card Game (TCG). The idea was born in summer of 2014 when a handful of software developers and programming enthusiasts from [Code Review Stack Exchange](http://codereview.stackexchange.com/) were tossing ideas around on how to come up with an innovative and fun game using the latest Java 8 technologies. 

The game is cross-platform compatible (Windows, OS X, Linux) and eventually will feature mobile versions as well. It is early in development, but we are working hard on adding new gameplay features, improving the user interface, and will eventually feature original art from content creators who have since joined the project. 

- [Official Website](http://www.Cardshifter.com/)

####Playing the game
--------------------

To play the game, [download the latest release](https://github.com/Cardshifter/Cardshifter/releases). Start the client jar using `java -jar cardshifter-fx-<version>.jar` or by saving to your computer and simply double-clicking `cardshifter-fx-<version>.jar`. There is a server running at `dwarftowers.com` port `4242` that you may connect to and play with other players as well as AI players. If this is your first time playing Cardshifter, you may want to have a look at the [Game Rules](https://github.com/Cardshifter/Cardshifter/wiki/Game-Rules).

####How can I get involved?
---------------------------

If you are interested in helping with the project, there are a number of ways you can go about it:

- Open a [Github Issue](https://github.com/Cardshifter/Cardshifter/issues).
 
- Find an issue you would like to work on a create a pull request or branch. Issues tagged "Help Wanted" are particularly good for new contributors to work on. Make sure you read the [Developer guidelines](https://github.com/Cardshifter/Cardshifter/wiki/Developer-Guidelines). 

- Join the [chatroom](http://chat.stackexchange.com/rooms/16134/tcg-creation) on Stack Exchange and discuss how you can contribute. _Please note that a minimum of 20 reputation on any Stack Exchange site (including Stack Overflow) is required before you can post in chat._

- Check the [Cardshifter Wiki](https://github.com/Cardshifter/Cardshifter/wiki) for more detailed technical information about the project.

If you are not a developer, you can also help by playing the game and [reporting any bug](http://www.Cardshifter.com/report-bug.html) you find or [requesting new features](http://www.Cardshifter.com/request-feature.html) you'd like to see added to the game. 

####Submodules: 
---------------
 
 - `cardshifter-api` - Classes for data being sent between client and server 
 - `cardshifter-console` - Console client (deprecated / discontinued) 
 - `cardshifter-core` - Contains our own TCG mod implementation, as well as various features for inclusion in the server and the JavaFX client 
 - `cardshifter-fx` - JavaFX Client 
 - `cardshifter-modapi` - Contains the core Entity-Component-System code and several components and systems for some common TCG features 
 - `cardshifter-server` - Multiplayer Server for the game 
 - `cardshifter-test` - Code to simplify testing mods 
 - `gdx / core` - libGDX core game code 
 - `gdx / android, ios, desktop, html5` - Platform specific libGDX code
