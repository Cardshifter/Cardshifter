package com.cardshifter.client.views;

import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;

import javafx.scene.layout.Pane;

public abstract class CardView {
	
	public CardView() {
		
	}
	
	public abstract Pane getRootPane();
	
	public abstract void updateFields(UpdateMessage message);
	
    public abstract void setCardActive(UsableActionMessage message);
    
    public abstract void removeCardActive();

    public abstract void setCardTargetable();
	
	public abstract void setCardScrappable(UsableActionMessage message);
	
	public abstract void removeCardScrappable();   
}
