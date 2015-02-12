package com.cardshifter.gdx.ui;

import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;

public interface EntityView {
    void set(Object key, Object value);
    void remove();

    void setTargetable(TargetStatus targetable, TargetableCallback callback);
    int getId();

    void usableAction(UsableActionMessage message);
    void clearUsableActions();

}
