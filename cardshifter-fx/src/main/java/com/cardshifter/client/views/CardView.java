package com.cardshifter.client.views;

import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UseableActionMessage;

import javafx.scene.layout.Pane;

public abstract class CardView {
	
	public CardView() {
		
	}
	
	public abstract Pane getRootPane();
	
	public abstract void updateFields(UpdateMessage message);
	
    public abstract void setCardActive(UseableActionMessage message);
    
    public abstract void removeCardActive();

    public abstract void setCardTargetable();
    
}
