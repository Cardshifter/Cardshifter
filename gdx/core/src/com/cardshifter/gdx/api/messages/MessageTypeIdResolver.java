package com.cardshifter.gdx.api.messages;

import com.cardshifter.gdx.api.both.ChatMessage;
import com.cardshifter.gdx.api.both.InviteRequest;
import com.cardshifter.gdx.api.both.InviteResponse;
import com.cardshifter.gdx.api.both.PlayerConfigMessage;
import com.cardshifter.gdx.api.incoming.*;
import com.cardshifter.gdx.api.messages.Message;
import com.cardshifter.gdx.api.outgoing.*;

import java.util.HashMap;
import java.util.Map;

public class MessageTypeIdResolver {
	
	private static final Map<String, Class<? extends Message>> clazzes = new HashMap<String, Class<? extends Message>>();
	
	static {
		clazzes.put("chat", ChatMessage.class);
		clazzes.put("login", LoginMessage.class);
		clazzes.put("startgame", StartGameRequest.class);
		clazzes.put("use", UseAbilityMessage.class);
		clazzes.put("requestTargets", RequestTargetsMessage.class);
		clazzes.put("zoneChange", ZoneChangeMessage.class);
		clazzes.put("entityRemoved", EntityRemoveMessage.class);
		clazzes.put("disconnect", ClientDisconnectedMessage.class);
		
		clazzes.put("resetActions", ResetAvailableActionsMessage.class);
		clazzes.put("game", GameMessage.class);
		clazzes.put("gameover", GameOverMessage.class);
		clazzes.put("wait", WaitMessage.class);
		clazzes.put("loginresponse", WelcomeMessage.class);
		clazzes.put("newgame", NewGameMessage.class);
		clazzes.put("player", PlayerMessage.class);
		clazzes.put("card", CardInfoMessage.class);
		clazzes.put("zone", ZoneMessage.class);
		clazzes.put("update", UpdateMessage.class);
		clazzes.put("useable", UseableActionMessage.class);
		clazzes.put("targets", AvailableTargetsMessage.class);
		clazzes.put("availableMods", AvailableModsMessage.class);
		
		clazzes.put("inviteRequest", InviteRequest.class);
		clazzes.put("inviteResponse", InviteResponse.class);
		clazzes.put("error", ServerErrorMessage.class);
		clazzes.put("userstatus", UserStatusMessage.class);
		clazzes.put("query", ServerQueryMessage.class);
		clazzes.put("playerconfig", PlayerConfigMessage.class);
	}

    public static Class<?> typeFor(String str) {
        return clazzes.get(str);
    }
}
