package com.cardshifter.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.both.InviteRequest;
import com.cardshifter.server.clients.ClientIO;

public class GameInvite implements IdObject {
	private static final Logger logger = LogManager.getLogger(GameInvite.class);
	
	private final int	id;
	private final ClientIO	host;
	private final ServerGame	game;
	private final List<ClientIO> invited;
	private final List<ClientIO> players;

	public GameInvite(Server server, int id, ClientIO host, ServerGame game) {
		this.id = id;
		this.host = host;
		this.game = game;
		this.invited = Collections.synchronizedList(new ArrayList<>());
		this.players = Collections.synchronizedList(new ArrayList<>());
		players.add(host);
	}

	@Override
	public int getId() {
		return id;
	}
	
	public void sendInvite(ClientIO to) {
		to.sendToClient(new InviteRequest(this.id, this.host.getName()));
		this.invited.add(to);
	}

	public boolean inviteAccept(ClientIO who) {
		logger.info(this + " Invite Accept: " + who + " contains? " + invited.contains(who));
		if (!invited.remove(who)) {
			return false;
		}
		players.add(who);
		
		if (players.size() == 2) {
			return start();
		}
		return true;
	}

	public boolean inviteDecline(ClientIO who) {
		logger.info(this + " Invite Decline: " + who);
		return invited.remove(who);
	}
	
	public boolean start() {
		logger.info(this + " Game Start! " + players);
		game.start(players);
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
	
}
