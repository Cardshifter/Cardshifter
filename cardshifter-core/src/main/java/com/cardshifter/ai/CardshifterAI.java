package com.cardshifter.ai;

import com.cardshifter.core.Player;
import com.cardshifter.core.UsableAction;

public interface CardshifterAI {
	UsableAction getAction(Player player);
}
