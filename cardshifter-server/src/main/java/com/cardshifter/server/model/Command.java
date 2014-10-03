package com.cardshifter.server.model;


public class Command {

	private final String[] params;
	private final String fullCommand;
	private final ClientIO	sender;

	public Command(ClientIO sender, String commandString) {
		this.params = commandString.split(" ");
		this.fullCommand = commandString;
		this.sender = sender;
	}
	
	public String getFullCommand() {
		return fullCommand;
	}

	public String getCommand() {
		return params[0];
	}

	public ClientIO getSender() {
		return sender;
	}
	
	@Override
	public String toString() {
		return "Command:" + getFullCommand();
	}
	
	public String getParameter(int i) {
		if (params.length > i)
			return params[i];
		return "";
	}
	
	public int getParameterInt(int i) {
		try {
			if (params.length > i)
				return Integer.parseInt(params[i]);
		}
		catch (NumberFormatException ex) { }
		return -1;
	}

	public String getFullCommand(int startingFrom) {
		StringBuilder str = new StringBuilder();
		for (int i = startingFrom; i < params.length; i++) {
			if (str.length() > 0)
				str.append(" ");
			str.append(params[i]);
		}
		return str.toString();
	}
	
}
