package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ZoomCardCallback;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.res.ResourceView;
import com.cardshifter.gdx.ui.res.ResViewFactory;

import java.util.*;

public class CardViewSmall extends DefaultCardView {

    private final Table table;
    //private final Label effect;
    private final Label namedEffect;
    private final Label complexEffect;
    private final Label name;
    private final ResourceView cost;
    private final ResourceView stats;
    private final Map<String, Object> properties;
    private final int id;
    private final CardshifterClientContext context;
    private TargetableCallback callback;
    private ZoomCardCallback zoomCallback;
    private final NinePatch patch = new NinePatch(new Texture(Gdx.files.internal("cardbg.png")), 3, 3, 3, 3);
    private final List<UsableActionMessage> actions = new ArrayList<UsableActionMessage>(5);
    public boolean isZoomed = false;
    public final CardInfoMessage cardInfo;

    public CardViewSmall(CardshifterClientContext context, CardInfoMessage cardInfo, ZoomCardCallback zoomCallback) {
        this.context = context;
        this.cardInfo = cardInfo;
        this.properties = new HashMap<String, Object>(cardInfo.getProperties());
        this.id = cardInfo.getId();
        this.zoomCallback = zoomCallback;
        table = new Table(context.getSkin());

        table.setBackground(new NinePatchDrawable(patch));
        Gdx.app.log("CardView", "Creating for " + cardInfo.getProperties());
        table.defaults().expand();
        name = label(context, cardInfo, "name");
        table.add(name).colspan(2).width(100).left().row();
        // table.add(image);
        //effect = label(context, cardInfo, "effect");
        this.namedEffect = new Label(" ", context.getSkin());
        String resourceName = this.stringResources(cardInfo);
        if (resourceName != null && !resourceName.equals("")) {
        	this.namedEffect.setText(this.stringResources(cardInfo));
        }
        //effect.setText(effect.getText() + stringResources(cardInfo));
        table.add(namedEffect).colspan(2).width(100).left().row();
        this.complexEffect = label(context, cardInfo, "effect");
        table.add(complexEffect).colspan(2).width(100).left().row();
        ResViewFactory rvf = new ResViewFactory(context.getSkin());
        cost = rvf.forFormat(rvf.res("MANA_COST"), rvf.res("SCRAP_COST"));
        table.add(cost.getActor()).colspan(2).right().row();

        stats = rvf.forFormat(rvf.coloredRes("ATTACK", properties), rvf.str("/"), rvf.coloredRes("HEALTH", "MAX_HEALTH"));
        table.add(label(context, cardInfo, "creatureType")).left();
        table.add(stats.getActor()).right();

        cost.update(properties);
        stats.update(properties);

        table.setTouchable(Touchable.enabled);
        table.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	if (this.getTapCount() > 1) {
            		CardViewSmall.this.zoomed();
            	} else {
            		CardViewSmall.this.clicked();
            	} 
            }
        });
        table.addListener(new ActorGestureListener(){
            @Override
            public boolean longPress(Actor actor, float x, float y) {
            	CardViewSmall.this.zoomed();
                return true;
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
    
    private void zoomed() {
    	if (this.zoomCallback != null) {
    		this.zoomCallback.zoomCard(this);
    	}
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
        //by changing the following to " ", a row will always be added
        //also need to replace newline characters because of some bug on server side
        String labelString = String.valueOf(value == null ? " " : value);
        labelString = labelString.replace("\n", "").replace("\r", "");
        Label label = new Label(labelString, context.getSkin());
        label.setEllipsis(true);
        //label.setWrap(false);
        return label;
    }

    @Override
    public void set(Object key, Object value) {
        if ("HEALTH".equals(key)) {
            Integer health = (Integer) value;
            Integer oldHealth = (Integer) properties.get(key);
            int diff = health - oldHealth;
            WidgetGroup grp = (WidgetGroup) table.getParent();
            grp.layout();

            Vector2 pos = new Vector2(table.getWidth() / 2, table.getHeight() / 2);
            table.localToStageCoordinates(pos);
            final Label changeLabel = new Label(String.valueOf(diff), context.getSkin());
            Gdx.app.log("Anim", "Create health animation at " + pos.x + ", " + pos.y);
            changeLabel.setPosition(pos.x, pos.y);
            changeLabel.addAction(Actions.sequence(Actions.moveBy(0, 25, 2), Actions.run(new Runnable() {
                @Override
                public void run() {
                    changeLabel.remove();
                }
            })));
            context.getStage().addActor(changeLabel);
        }
        properties.put((String) key, value);
        cost.update(properties);
        stats.update(properties);
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

    @Override
    public Actor getActor() {
        return table;
    }
    
    public void zoom() {
    	this.isZoomed = true;
    	this.name.setEllipsis(false);
    	this.name.layout();
    	this.complexEffect.setEllipsis(false);
    	this.complexEffect.layout();
    }
    
    public void endZoom() {
    	this.isZoomed = false;
    	this.name.setEllipsis(true);
    	this.name.layout();
    	this.complexEffect.setEllipsis(true);
    	this.complexEffect.layout();
    }
}
