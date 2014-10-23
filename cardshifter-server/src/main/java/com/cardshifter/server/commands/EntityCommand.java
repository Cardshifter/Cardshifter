package com.cardshifter.server.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.cardshifter.core.game.ServerGame;
import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.server.commands.EntityCommand.EntityInspectParameters;
import com.cardshifter.server.model.CommandHandler.CommandHandle;

public class EntityCommand implements CommandHandle<EntityInspectParameters> {

	@Parameters(commandDescription = "Inspect entity")
	public static class EntityInspectParameters {
//		/ent -g 2 -e 42 -comp HandComponent -call getCards
		
		@Parameter(names = "-g", description = "Gameid")
		private int gameId;
		
		@Parameter(names = "-e", description = "Entity ID")
		private int entity;
		
		@Parameter(names = "-comp", description = "Component class")
		private String component;
		
		@Parameter(names = "-method", description = "Call a method and return the result")
		private String method;
		
	}
	
	@Override
	public void handle(CommandContext command, EntityInspectParameters parameters) {
		ServerGame game = command.getServer().getGames().get(parameters.gameId);
		Objects.requireNonNull(game, "No such game " + parameters.gameId);
		
		Entity entity = game.getGameModel().getEntity(parameters.entity);
		Objects.requireNonNull(entity, "No such entity " + parameters.entity);
		
		if (parameters.component == null) {
			Collection<Component> all = entity.getSuperComponents(Component.class);
			for (Component comp : all) {
				command.sendChatResponse(String.valueOf(comp));
			}
			return;
		}
		
		Component comp = getComponent(entity, parameters.component).orElse(null);
		if (parameters.method == null) {
			command.sendChatResponse(String.valueOf(comp));
			return;
		}
		
		if (comp != null) {
			try {
				Method method = comp.getClass().getMethod(parameters.method);
				Object result = method.invoke(comp);
				command.sendChatResponse(String.valueOf(result));
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				command.sendChatResponse(e.toString());
			}
		}
	}

	private Optional<Component> getComponent(Entity entity, String component) {
		return entity.getSuperComponents(Component.class).stream().filter(comp -> comp.getClass().getSimpleName().equals(component)).findAny();
	}

}
