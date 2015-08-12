package com.cardshifter.api.messages;

import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.InviteRequest;
import com.cardshifter.api.both.InviteResponse;
import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.ServerQueryMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.TransformerMessage;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.AvailableModsMessage;
import com.cardshifter.api.outgoing.AvailableTargetsMessage;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ClientDisconnectedMessage;
import com.cardshifter.api.outgoing.EntityRemoveMessage;
import com.cardshifter.api.outgoing.GameMessage;
import com.cardshifter.api.outgoing.GameOverMessage;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.PlayerMessage;
import com.cardshifter.api.outgoing.ResetAvailableActionsMessage;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.api.outgoing.UserStatusMessage;
import com.cardshifter.api.outgoing.WaitMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.cardshifter.api.outgoing.ZoneChangeMessage;
import com.cardshifter.api.outgoing.ZoneMessage;

/**
 * Message Type ID Resolver.
 * <p>
 * Resolves all messages' type ID to the correct class corresponding to this particular type.
 * Please note the use of the names <code>clazz / clazzes</code> in lieu of <code>class / classes</code> 
 * because <code>class</code> is a reserved Java keyword.
 */
public class MessageTypeIdResolver {
	
	/** This value is used to map a String message ID to its corresponding message class */
	private static final Map<String, Class<? extends Message>> clazzes = new HashMap<String, Class<? extends Message>>();
	
	/** Array used to map this message ID key to its corresponding class */
	static {
		// Serialize message (used principally for libGDX client)
		clazzes.put("serial", TransformerMessage.class);
		// Incoming messages to server
		clazzes.put("chat", ChatMessage.class);
		clazzes.put("login", LoginMessage.class);
		clazzes.put("startgame", StartGameRequest.class);
		clazzes.put("use", UseAbilityMessage.class);
		clazzes.put("requestTargets", RequestTargetsMessage.class);
		clazzes.put("zoneChange", ZoneChangeMessage.class);
		clazzes.put("entityRemoved", EntityRemoveMessage.class);
		clazzes.put("disconnect", ClientDisconnectedMessage.class);
		// Outgoing messages from server
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
		clazzes.put("useable", UsableActionMessage.class);
		clazzes.put("targets", AvailableTargetsMessage.class);
		clazzes.put("availableMods", AvailableModsMessage.class);
		// Messages both incoming and outgoing
		clazzes.put("inviteRequest", InviteRequest.class);
		clazzes.put("inviteResponse", InviteResponse.class);
		clazzes.put("error", ServerErrorMessage.class);
		clazzes.put("userstatus", UserStatusMessage.class);
		clazzes.put("query", ServerQueryMessage.class);
		clazzes.put("playerconfig", PlayerConfigMessage.class);
	}
        /** @return message ID key */
	public static Class<? extends Message> get(String key) {
	        return clazzes.get(key);
	}
	
}
