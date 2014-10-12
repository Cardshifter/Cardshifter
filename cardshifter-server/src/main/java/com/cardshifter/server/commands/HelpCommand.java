package com.cardshifter.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.cardshifter.server.commands.HelpCommand.HelpParameters;
import com.cardshifter.server.model.CommandHandler;
import com.cardshifter.server.model.CommandHandler.CommandHandle;
import com.cardshifter.server.model.CommandHandler.CommandInfo;

public class HelpCommand implements CommandHandle<HelpParameters> {

	@Parameters(commandDescription = "Get information about what the `/`-commands is doing")
	public static class HelpParameters {
		
		@Parameter()
		private List<String> commands = new ArrayList<>();
		
	}

	private final CommandHandler handler;
	
	public HelpCommand(CommandHandler commandHandler) {
		this.handler = commandHandler;
	}

	@Override
	public void handle(CommandContext command, HelpParameters parameters) {
		String helpCommand = command.getCommand().getParameter(1);
		StringBuilder out = new StringBuilder();
		
		if (!helpCommand.isEmpty()) {
			CommandInfo<?> cmd = handler.getCommands().get(helpCommand);
			if (cmd != null) {
				command.sendChatResponse("USAGE FOR " + helpCommand);
				cmd.getCommander().usage(out);
				command.sendChatResponse(out.toString());
				return;
			}
			command.sendChatResponse("Command " + helpCommand + " not found");
		}
		
		for (Entry<String, CommandInfo<?>> entry : handler.getCommands().entrySet()) {
			String description = entry.getValue().getDescription();
			command.sendChatResponse(entry.getKey() + ": " + description);
		}
		
		command.sendChatResponse(out.toString());
	}

}
