package com.cardshifter.console;

import java.util.Random;

import com.beust.jcommander.Parameter;

public class CommandLineOptions {
	@Parameter(names = { "--file", "-f" }, description = "Script file to run")
	private String script;
	
	@Parameter(names = { "--seed", "-s" }, description = "Set random seed")
	private Integer seed;
	
	@Parameter(names = { "--network" }, description = "Connect to a server for playing")
	private String host;
	
	@Parameter(names = { "--port" }, description = "Port for use with network playing")
	private int port = 4242;
	
	public Random getRandom() {
		return (seed == null) ? new Random() : new Random(seed);
	}
	
	public String getScript() {
		return script;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
}
