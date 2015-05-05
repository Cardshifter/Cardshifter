package com.cardshifter.server.main;

import org.apache.log4j.PropertyConfigurator;

import com.cardshifter.server.model.MainServer;

import java.util.Scanner;

public class ServerMain {
	
	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.out.println("No security manager has been installed, you need to install one to be protected against malicious mods, please refer to the documentation to install a correct security manager.");
            System.out.println("CONTINUE AT YOUR OWN RISK");
            System.out.println("Do you want to continue without a security manager? (y/n)");
            if (!new Scanner(System.in, "UTF-8").nextLine().equalsIgnoreCase("y")) {
                System.exit(1);
            }
		}
		PropertyConfigurator.configure(ServerMain.class.getResource("log4j.properties"));
		new MainServer().start();
	}

}
