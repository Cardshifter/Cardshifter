package com.cardshifter.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.ClientIO;
import com.cardshifter.api.IdObject;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.InviteRequest;
import com.cardshifter.server.main.FakeAIClientTCG;

public class GameInvite implements IdObject {
	private static final Logger logger = LogManager.getLogger(GameInvite.class);
	private static final Random random = new Random();
	
	private final int	id;
	private final ClientIO	host;
	private final ServerGame	game;
	private final List<ClientIO> invited;
	private final List<ClientIO> players;
	private final ChatArea chatArea;
	private final String gameType;
	private final ServerHandler<GameInvite> handler;

	public GameInvite(ServerHandler<GameInvite> handler, ChatArea chatlog, ClientIO host, ServerGame game, String gameType) {
		this.id = handler.newId();
		this.host = host;
		this.game = game;
		this.chatArea = chatlog;
		this.invited = Collections.synchronizedList(new ArrayList<>());
		this.players = Collections.synchronizedList(new ArrayList<>());
		this.gameType = gameType;
		this.handler = handler;
		players.add(host);
	}

	@Override
	public int getId() {
		return id;
	}
	
	public void sendInvite(ClientIO to) {
		to.sendToClient(new InviteRequest(this.id, this.host.getName(), gameType));
		this.invited.add(to);
		if (to instanceof FakeAIClientTCG) {
			inviteAccept(to);
		}
	}

	public boolean inviteAccept(ClientIO who) {
		logger.info(this + " Invite Accept: " + who + " contains? " + invited.contains(who));
		if (!invited.remove(who)) {
			return false;
		}
		addPlayer(who);
		
		return true;
	}

	public boolean inviteDecline(ClientIO who) {
		logger.info(this + " Invite Decline: " + who);
		host.sendToClient(new ChatMessage(1, "Server", who.getName() + " declined invite"));
		this.removeInvite();
		return invited.remove(who);
	}
	
	private void removeInvite() {
		handler.remove(this);
	}

	public boolean start() {
		logger.info(this + " Game Start! " + players);
		Collections.shuffle(players, random);
		game.start(players);
		chatArea.broadcast("Server", players.stream().map(io -> io.getName()).collect(Collectors.joining(" and ")) + " are now playing game " + game.getId());
		this.removeInvite();
		return true;
	}

	public void handleResponse(ClientIO client, boolean accepted) {
		if (accepted) {
			this.inviteAccept(client);
		}
		else {
			this.inviteDecline(client);
		}
	}

	public void addPlayer(ClientIO player) {
		this.players.add(player);
		if (players.size() == 2) {
			start();
		}
	}
	
}
