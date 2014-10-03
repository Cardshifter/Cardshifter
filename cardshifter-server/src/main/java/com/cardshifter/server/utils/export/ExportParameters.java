package com.cardshifter.server.utils.export;

import com.beust.jcommander.Parameter;

public class ExportParameters {

	@Parameter(names = { "--gameid", "-g" }, description = "Gameid")
	private int gameid;
	
	// export VANILLA --components ResourceComponent --resources Health --entity card:deck|card:hand --if player:0 --unique
	
	public int getGameid() {
		return gameid;
	}
	
}
