package com.cardshifter.server.main;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.model.MainServer;

import java.util.Scanner;

public class ServerMain {

    private static final Logger logger = LogManager.getLogger(ServerMain.class);
	
	public static void main(String[] args) {
        ServerConfiguration config = ServerConfiguration.readFrom("server.properties");
		if (System.getSecurityManager() == null) {
            ServerConfiguration.WarningLevel warningLevel = config.getMissingSecurity();
            logger.warn("No security manager has been installed, you need to install one to be protected against malicious mods, please refer to the documentation to install a correct security manager.");
            switch (warningLevel) {
                case WARN:
                    logger.warn("CONTINUE AT YOUR OWN RISK");
                    logger.warn("Do you want to continue without a security manager? (y/n)");
                    if (!new Scanner(System.in, "UTF-8").nextLine().equalsIgnoreCase("y")) {
                        System.exit(1);
                    }
                    break;
                case IGNORE:
                    // do nothing
                    break;
                case FAIL:
                default:
                    System.exit(1);
                    break;
            }
		}

		new MainServer(config).start();
	}

}
