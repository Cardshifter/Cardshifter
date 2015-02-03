Cardshifter
===========

_Updated 2015-02-03_

Master branch: [![Build Status](https://travis-ci.org/Cardshifter/Cardshifter.svg?branch=master)](https://travis-ci.org/Cardshifter/Cardshifter?branch=master)  
Develop branch: [![Build Status](https://travis-ci.org/Cardshifter/Cardshifter.svg?branch=develop)](https://travis-ci.org/Cardshifter/Cardshifter?branch=develop)

####Useful links

- [Developer guidelines](https://github.com/Cardshifter/Cardshifter/wiki/Developer-Guidelines)

- [Releases & Features](https://github.com/Cardshifter/Cardshifter/wiki/Releases-&-Features)

- [Official Website](http://www.Cardshifter.com/)


Playing the game
----------------

To play the game, [download the latest release](https://github.com/Cardshifter/Cardshifter/releases). Start the client jar using `java -jar cardshifter-fx-<version>.jar` or by saving to your computer and simply double-clicking `cardshifter-fx-<version>.jar`. There is a server running at `dwarftowers.com` port `4242` that you may connect to and play with other players as well as AI players. 




Running cardshifter-server from within the IDE
----------------------------------------------

In order to run the cardshifter-server module from within your IDE, you will need to run it with JVM arguments `-Djava.security.manager -Djava.security.policy==server.policy`. For Netbeans, open up the Project Properties for the cardshifter-server project.  Then go to Run, and add the above JVM arguments to the VM Options box.

Running cardshifter-server standalone
-------------------------------------

In order to run the cardshifter server standalone, you will need to use the supplied JAR and policy file, suppose we are on version 0.4-SNAPSHOT, then you can start the server with the following command:
`java -jar -Djava.security.manager -D.java.security.policy==cardshifter-server-0.4-SNAPSHOT.policy cardshifter-server-0.4-SNAPSHOT.jar`

For secure execution, you must use a double equals sign in the -Djava.security.policy==x declaration and you must not edit the policy file.
