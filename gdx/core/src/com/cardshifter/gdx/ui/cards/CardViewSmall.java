package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.gdx.CardshifterConstants;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.res.ResourceView;
import com.cardshifter.gdx.ui.res.ResViewFactory;

import java.util.*;

public class CardViewSmall implements CardView {

    private final Table table;
    private final Label effect;
    private final Label name;
    private final ResourceView cost;
    private final ResourceView stats;
    private final Map<String, Object> properties;
    private final int id;
    private final CardshifterClientContext context;
    private TargetableCallback callback;
    private final NinePatch patch = new NinePatch(new Texture(Gdx.files.internal("cardbg.png")), 3, 3, 3, 3);
    private final List<UsableActionMessage> actions = new ArrayList<UsableActionMessage>(5);

    public CardViewSmall(CardshifterClientContext context, CardInfoMessage cardInfo) {
        this.context = context;
        this.properties = new HashMap<String, Object>(cardInfo.getProperties());
        this.id = cardInfo.getId();
        table = new Table(context.getSkin());
        table.setBackground(new NinePatchDrawable(patch));
        Gdx.app.log("CardView", "Creating for " + cardInfo.getProperties());
        table.defaults().expand();
        name = label(context, cardInfo, "name");
        table.add(name).colspan(2).width(100).row();
        // table.add(image);
        effect = label(context, cardInfo, "effect");
        effect.setText(effect.getText() + stringResources(cardInfo));
        table.add(effect).colspan(2).width(100).row();
        ResViewFactory rvf = new ResViewFactory(context.getSkin());
        cost = rvf.forFormat(rvf.res("MANA_COST"), rvf.res("SCRAP_COST"));
        table.add(cost.getActor()).colspan(2).right().row();

        stats = rvf.forFormat(rvf.coloredRes("ATTACK", properties), rvf.str("/"), rvf.coloredRes("HEALTH", "MAX_HEALTH"));
        table.add(label(context, cardInfo, "creatureType")).left();
        table.add(stats.getActor()).right();
        table.setDebug(CardshifterConstants.DEBUG_TABLES, true);

        cost.update(properties);
        stats.update(properties);

        table.setTouchable(Touchable.enabled);
        table.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CardViewSmall.this.clicked();
            }
        });
    }

    private String stringResources(CardInfoMessage cardInfo) {
        StringBuilder str = new StringBuilder();
        Map<String, Object> props = cardInfo.getProperties();
        if (Integer.valueOf(0).equals(props.get("SICKNESS"))) {
            str.append(" Rush");
        }
        if (Integer.valueOf(1).equals(props.get("DENY_COUNTERATTACK"))) {
            str.append(" Ranged");
        }
        return str.toString();
    }

    private void clicked() {
        Gdx.app.log("CardView", "clicked on " + id);
        if (callback != null) {
            callback.addEntity(this);
        }
        else if (!actions.isEmpty()) {
            if (actions.size() == 1) {
                UsableActionMessage action = actions.get(0);
                context.sendAction(action);
            }
            else {
                Dialog dialog = new Dialog("Choose Action", context.getSkin()) {
                    @Override
                    protected void result(Object object) {
                        context.sendAction(actions.get((Integer) object));
                    }
                };
                dialog.text("Which action?");
                ListIterator<UsableActionMessage> it = actions.listIterator();
                while (it.hasNext()) {
                    int i = it.nextIndex();
                    UsableActionMessage action = it.next();
                    dialog.button(action.getAction(), i);
                }
                dialog.show(context.getStage());

            }
        }
    }

    public static Label label(CardshifterClientContext context, CardInfoMessage message, String key) {
        Object value = message.getProperties().get(key);
        Label label = new Label(String.valueOf(value == null ? "" : value), context.getSkin());
        label.setEllipsis(true);
        return label;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public void set(Object key, Object value) {
        properties.put((String) key, value);
        cost.update(properties);
        stats.update(properties);
    }

    @Override
    public void remove() {
        table.remove();
    }

    @Override
    public void setTargetable(TargetStatus targetable, TargetableCallback callback) {
        table.setColor(targetable.getColor());
        this.callback = callback;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void usableAction(UsableActionMessage message) {
        table.setColor(1, 1, 1, 1f);
        actions.add(message);
    }

    @Override
    public void clearUsableActions() {
        table.setColor(1, 1, 1, 0.5f);
        actions.clear();
    }

    @Override
    public Map<String, Object> getInfo() {
        return new HashMap<String, Object>(this.properties);
    }
}
