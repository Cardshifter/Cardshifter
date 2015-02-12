package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;

/**
 * Created by Simon on 2/9/2015.
 */
public class ActionButton {

    private final TextButton button;

    public ActionButton(final CardshifterClientContext context, final UsableActionMessage message) {
        this.button = new TextButton(message.getAction(), context.getSkin());
        this.button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (message.isTargetRequired()) {
                    context.send(new RequestTargetsMessage(context.getGameId(), message.getId(), message.getAction()));
                }
                else {
                    context.send(new UseAbilityMessage(context.getGameId(), message.getId(), message.getAction(), 0));
                }
            }
        });
    }

    public TextButton getButton() {
        return button;
    }
}
