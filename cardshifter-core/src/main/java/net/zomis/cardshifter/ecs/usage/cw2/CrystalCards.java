package net.zomis.cardshifter.ecs.usage.cw2;

import static net.zomis.cardshifter.ecs.usage.CW2Game.CWars2Res.CASTLE;
import static net.zomis.cardshifter.ecs.usage.CW2Game.CWars2Res.WALL;
import static net.zomis.cardshifter.ecs.usage.CW2Game.Producers.WIZARDS;
import static net.zomis.cardshifter.ecs.usage.CW2Game.Resources.CRYSTALS;

import java.util.function.Consumer;

import net.zomis.cardshifter.ecs.usage.CW2Game;
import net.zomis.cardshifter.ecs.usage.CostComponent;
import net.zomis.cardshifter.ecs.usage.CW2Game.Resources;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.NameComponent;
import com.cardshifter.modapi.resources.ECSResource;

public class CrystalCards implements Consumer<ECSGame> {

	private static final CW2Effects ef = new CW2Effects();
	
	@Override
	public void accept(ECSGame game) {
		addCard(game, "Mage", cost(CRYSTALS, 8), ef.myRes(WIZARDS, 1));
		addCard(game, "Lightning", cost(CRYSTALS, 20), ef.damage(22));
		addCard(game, "Quake", cost(CRYSTALS, 24), ef.damage(27));
		addCard(game, "Pixies", cost(CRYSTALS, 18), ef.myRes(CASTLE, 22));
		addCard(game, "Magic Wall", cost(CRYSTALS, 14), ef.myRes(WALL, 20));
//		addCard("Magic Defense", cost(CRYSTALS, 10), ef.manipulateNextAttack***);
//		addCard("Magic Weapons", cost(CRYSTALS, 15), ef.manipulateNextAttack***); // new MagicAttackMultiply from old game
		
		for (ECSResource res : Resources.values()) {
			addCard(game, "Add " + res, cost(CRYSTALS, 5), ef.myRes(res, 8));
			addCard(game, "Remove " + res, cost(CRYSTALS, 5), ef.oppRes(res, -8));
		}
		
		addCard(game, "All Crystals", cost(CRYSTALS, 1), ef.allFocus(CRYSTALS));
	}

	private void addCard(ECSGame game, String name, Component... components) {
		Entity entity = game.newEntity();
		entity.addComponent(new NameComponent(name));
		
		for (Component comp : components) {
			entity.addComponent(comp);
			// TODO: Concatenate `CostComponent` and `EffectComponent`
		}
		
	}

	private Component cost(ECSResource resource, int cost) {
		return new CostComponent(resource, cost);
	}

}
