Cardshifter
===========

_Updated 2015-02-03_

####What is Cardshifter?
------------------------

Cardshifter is an open-source, online Trading Card Game (TCG). The idea was born in summer of 2014 when a handful of software developers and programming enthusiasts from [Code Review Stack Exchange](http://codereview.stackexchange.com/) were tossing ideas around on how to come up with an innovative and fun game using the latest Java 8 technologies. 

The game is cross-platform compatible (Windows, OS X, Linux) and eventually will feature mobile versions as well. It is early in development, but we are working hard on adding new gameplay features, improving the user interface, and will eventually feature original art from content creators who have since joined the project. 

####How can I get involved?
---------------------------

If you are interested in helping with the project, there are a number of ways you can go about it:

- Open a [Github Issue](https://github.com/Cardshifter/Cardshifter/issues).
 
- Find an issue you would like to work on a create a pull request or branch. Issues tagged "Help Wanted" are particularly good for new contributors to work on.

- Join the [chatroom](http://chat.stackexchange.com/rooms/16134/tcg-creation) on Stack Exchange and discuss how you can contribute. _Please note that a minimum of 20 reputation on any Stack Exchange site (including Stack Overflow) is required before you can post in chat._

If you are not a developer, you can also help by playing the game and [reporting any bug](https://www.Cardshifter.com/report-bug.html) you find or [requesting new features](https://www.Cardshifter.com/request-feature.html) you'd like to see added to the game. See the instructions below. 

####Playing the game
--------------------

To play the game, [download the latest release](https://github.com/Cardshifter/Cardshifter/releases). Start the client jar using `java -jar cardshifter-fx-<version>.jar` or by saving to your computer and simply double-clicking `cardshifter-fx-<version>.jar`. There is a server running at `dwarftowers.com` port `4242` that you may connect to and play with other players as well as AI players. 


####Useful links
----------------

- Master branch: [![Build Status](https://travis-ci.org/Cardshifter/Cardshifter.svg?branch=master)](https://travis-ci.org/Cardshifter/Cardshifter?branch=master)  

- Develop branch: [![Build Status](https://travis-ci.org/Cardshifter/Cardshifter.svg?branch=develop)](https://travis-ci.org/Cardshifter/Cardshifter?branch=develop)

- [Developer guidelines](https://github.com/Cardshifter/Cardshifter/wiki/Developer-Guidelines)

- [Releases & Features](https://github.com/Cardshifter/Cardshifter/wiki/Releases-&-Features)

- [Official Website](https://www.Cardshifter.com/)






Running cardshifter-server from within the IDE
----------------------------------------------

In order to run the cardshifter-server module from within your IDE, you will need to run it with JVM arguments `-Djava.security.manager -Djava.security.policy==server.policy`. For Netbeans, open up the Project Properties for the cardshifter-server project.  Then go to Run, and add the above JVM arguments to the VM Options box.

Running cardshifter-server standalone
-------------------------------------

In order to run the cardshifter server standalone, you will need to use the supplied JAR and policy file, suppose we are on version 0.4-SNAPSHOT, then you can start the server with the following command:
`java -jar -Djava.security.manager -D.java.security.policy==cardshifter-server-0.4-SNAPSHOT.policy cardshifter-server-0.4-SNAPSHOT.jar`

For secure execution, you must use a double equals sign in the -Djava.security.policy==x declaration and you must not edit the policy file.
