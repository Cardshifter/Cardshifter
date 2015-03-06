package net.zomis.cardshifter.ecs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.players.Players;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import com.cardshifter.api.config.DeckConfig;
import net.zomis.cardshifter.ecs.effects.Effects;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

import org.junit.Test;

import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.attack.TrampleSystem;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.GameTest;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.cardshifter.modapi.resources.Resources;

public class PhrancisTest extends GameTest {

	private static final int originalLife = 30;
	private static final String NO_NAME = "No name";
	private final ResourceRetriever mana = ResourceRetriever.forResource(PhrancisResources.MANA);
	private final ResourceRetriever manaCost = ResourceRetriever.forResource(PhrancisResources.MANA_COST);
	private final ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
	private final ResourceRetriever attackPoints = ResourceRetriever.forResource(PhrancisResources.ATTACK_AVAILABLE);
	private final ResourceRetriever attack = ResourceRetriever.forResource(PhrancisResources.ATTACK);
	private final ResourceRetriever scrapCost = ResourceRetriever.forResource(PhrancisResources.SCRAP_COST);
	private final ResourceRetriever scrap = ResourceRetriever.forResource(PhrancisResources.SCRAP);
	
	private final ComponentRetriever<BattlefieldComponent> field = ComponentRetriever.retreiverFor(BattlefieldComponent.class);
	
	private final Predicate<Entity> isCreature = entity -> entity.hasComponent(CreatureTypeComponent.class);
	private final Predicate<Entity> hasName(String str) {
		AttributeRetriever name = AttributeRetriever.forAttribute(Attributes.NAME);
		return e -> name.getOrDefault(e, "").equals(str);
	}

	private final PhrancisGame mod = new PhrancisGame();
	
	@Override
	protected void setupGame(ECSGame game) {
		ECSMod mod = new PhrancisGame();
		mod.declareConfiguration(game);
		
		// Configure decks
		Set<Entity> configEntities = game.getEntitiesWithComponent(ConfigComponent.class);
		for (Entity configEntity : configEntities) {
			ConfigComponent config = configEntity.getComponent(ConfigComponent.class);
			DeckConfig deckConf = config.getConfig(DeckConfig.class);
			
			addCard(deckConf, isCreature.and(manaCost(1)));
			addCard(deckConf, e -> scrapCost.getFor(e) == 1);
			addCard(deckConf, isCreature.and(manaCost(1)));
			addCard(deckConf, isCreature.and(manaCost(3)));
			addCard(deckConf, isCreature.and(manaCost(1)));
			addCard(deckConf, isCreature.and(manaCost(1)));
			addCard(deckConf, isCreatureType("Bio").and(health(4)));
			addCard(deckConf, hasName("Field Medic"));
			addCard(deckConf, hasName("Supply Mech"));
			addCard(deckConf, e -> scrapCost.getFor(e) == 1 && health.getFor(e) == 1);
		}

		mod.setupGame(game);
	}
	
	@Test
	public void noAttackCard() {
		while (mana.getFor(currentPlayer()) < 5) {
			nextPhase();
		}

		Entity entity = cardToHand(hasName("Supply Mech"));
		useAction(entity, PhrancisGame.PLAY_ACTION);
		nextPhase();
		nextPhase();
		attack.resFor(entity).set(10);
		useFail(entity, PhrancisGame.ATTACK_ACTION);
	}

	@Test
	public void healEndOfTurn() {
		while (mana.getFor(currentPlayer()) < 5) {
			nextPhase();
		}

		Entity player = currentPlayer();
		Entity medic = cardToHand(hasName("Field Medic"));
		this.health.resFor(player).set(29);
		int health = this.health.getFor(player);
		useAction(medic, PhrancisGame.PLAY_ACTION);
		assertEquals(29, this.health.getFor(player));
		nextPhase();

		assertEquals(30, this.health.getFor(player));
		nextPhase();

		assertEquals(player, currentPlayer());
		assertEquals(30, this.health.getFor(player));
		nextPhase();

		// Health does not go above HEALTH_MAX
		assertEquals(30, this.health.getFor(player));
	}

	private void addCard(DeckConfig config, Predicate<Entity> condition) {
		Set<Entity> availableCards = game.getEntitiesWithComponent(ZoneComponent.class);
		ZoneComponent zone = availableCards.iterator().next().getComponent(ZoneComponent.class);
		Entity entity = zone.stream().filter(condition).findFirst().get();
		config.add(entity.getId());
	}

	@Test
	public void opponentCanNotChangeTurn() {
		useFail(opponent(), PhrancisGame.END_TURN_ACTION);
	}
	
	@Test
	public void manaIncrease() {
		assertEquals(1, mana.getFor(currentPlayer()));
		nextPhase();
		assertEquals(1, mana.getFor(currentPlayer()));
		nextPhase();
		assertEquals(2, mana.getFor(currentPlayer()));
	}
	
	@Test
	public void cannotPlayCardFromDeck() {
		Entity entity = mod.createCreature(0, deck.get(currentPlayer()), 1, 1, "B0T", 1);
		useFail(entity, PhrancisGame.PLAY_ACTION);
	}
	
	@Test
	public void cannotEnchantWithoutScrap() {
		assertEquals(0, scrap.getFor(currentPlayer()));
		Entity enchantment = cardToHand(e -> scrapCost.getFor(e) == 1);
		useFail(enchantment, PhrancisGame.ENCHANT_ACTION);
	}
	
	@Test
	public void canPlayCreatureFromHand() {
		Entity creature = mod.createCreature(0, hand.get(currentPlayer()), 1, 1, "B0T", 1);
		useAction(creature, PhrancisGame.PLAY_ACTION);
	}
	
	@Test
	public void canAttackWithCreatureNextTurn() {
		Entity creature = mod.createCreature(0, field.get(currentPlayer()), 1, 1, "B0T", 1);
		nextPhase();
		nextPhase();
		assertResource(getOpponent(), PhrancisResources.HEALTH, originalLife);
		useActionWithTarget(creature, PhrancisGame.ATTACK_ACTION, getOpponent());
		assertResource(getOpponent(), PhrancisResources.HEALTH, originalLife - 1);
	}
	
	@Test
	public void onlyAttackOncePerTurn() {
		Entity creature = mod.createCreature(0, field.get(currentPlayer()), 1, 1, "B0T", 1);
		nextPhase();
		nextPhase();
		assertEquals(1, attackPoints.getFor(creature));
		useActionWithTarget(creature, PhrancisGame.ATTACK_ACTION, getOpponent());
		assertResource(getOpponent(), PhrancisResources.HEALTH, 29);
		assertEquals(0, attackPoints.getFor(creature));
		useFail(creature, PhrancisGame.ATTACK_ACTION);
	}
	
	@Test
	public void summoningSicknessCannotAttack() {
		Entity attacker = mod.createCreature(0, field.get(currentPlayer()), 1, 1, "B0T", 1);
		assertResource(attacker, PhrancisResources.SICKNESS, 1);
		assertResource(attacker, PhrancisResources.ATTACK_AVAILABLE, 1);
		useFail(attacker, PhrancisGame.ATTACK_ACTION);
	}
	
	@Test
	public void canOnlyAttackOpponentPlayer() {
		Entity attacker = mod.createCreature(0, field.get(currentPlayer()), 1, 1, "B0T", 1);
		nextPhase();
		nextPhase();
		List<Entity> possibleTargets = findPossibleTargets(attacker, PhrancisGame.ATTACK_ACTION);
		assertEquals(1, possibleTargets.size());
	}
	
	@Test
	public void attackGetKilled() {
		Entity attacker = mod.createCreature(0, field.get(currentPlayer()), 1, 1, "B0T", 0);
		nextPhase();
		Entity defender = mod.createCreature(0, field.get(currentPlayer()), 3, 3, "B0T", 0);
		nextPhase();
		assertResource(attacker, PhrancisResources.SICKNESS, 0);
		assertResource(attacker, PhrancisResources.ATTACK_AVAILABLE, 1);
		useActionWithFailedTarget(attacker, PhrancisGame.ATTACK_ACTION, getOpponent());
		assertResource(defender, PhrancisResources.HEALTH, 3);
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, defender);
		assertTrue(attacker.isRemoved());
		assertFalse(defender.isRemoved());
		assertResource(defender, PhrancisResources.HEALTH, 3);
	}
	
	@Test
	public void attackRanged() {
		Entity attacker = mod.createCreature(0, field.get(currentPlayer()), 4, 1, "B0T", 0);
		nextPhase();
		Entity defender = mod.createCreature(0, field.get(currentPlayer()), 3, 3, "B0T", 0);
		ResourceRetriever.forResource(PhrancisResources.DENY_COUNTERATTACK).resFor(attacker).set(1);
		nextPhase();

		assertResource(attacker, PhrancisResources.HEALTH, 1);
		assertResource(defender, PhrancisResources.HEALTH, 3);
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, defender);
		assertFalse(attacker.isRemoved());
		assertTrue(defender.isRemoved());
		assertResource(attacker, PhrancisResources.HEALTH, 1);
	}

	@Test
	public void scrap() {
		Entity scrapped = mod.createCreature(0, field.get(currentPlayer()), 1, 1, "B0T", 4);
		assertResource(currentPlayer(), PhrancisResources.SCRAP, 0);
		nextPhase();
		nextPhase();
		useFail(scrapped, PhrancisGame.SCRAP_ACTION, opponent());
		useAction(scrapped, PhrancisGame.SCRAP_ACTION);
		assertResource(currentPlayer(), PhrancisResources.SCRAP, 4);
	}

	@Test
	public void rangedCausesSickness() {
		Entity attacker = mod.createCreature(0, field.get(currentPlayer()), 1, 1, "B0T", 0);
		ResourceRetriever ranged = ResourceRetriever.forResource(PhrancisResources.DENY_COUNTERATTACK);
		ranged.set(attacker, 1);
		assertResource(attacker, PhrancisResources.SICKNESS, 1);
		nextPhase();
		nextPhase();
		assertResource(attacker, PhrancisResources.SICKNESS, 0);
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, opponent());
		assertResource(attacker, PhrancisResources.SICKNESS, 2);
		useFail(attacker, PhrancisGame.ATTACK_ACTION);
		nextPhase();
		assertResource(attacker, PhrancisResources.SICKNESS, 2);
		nextPhase();
		assertResource(attacker, PhrancisResources.SICKNESS, 1);
		useFail(attacker, PhrancisGame.ATTACK_ACTION);
		nextPhase();
		nextPhase();
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, opponent());
	}

    @Test
    public void noScrapOnSameTurn() {
        Entity scrapped = mod.createCreature(0, field.get(currentPlayer()), 1, 1, "B0T", 4);
        assertResource(currentPlayer(), PhrancisResources.SCRAP, 0);
        useFail(scrapped, PhrancisGame.SCRAP_ACTION, opponent());
        useFail(scrapped, PhrancisGame.SCRAP_ACTION);
    }

    @Test
    public void damageToRandomOpponent() {
        Entity megaman = mod.createCreature(0, hand.get(currentPlayer()), 1, 1, "B0T", 0);
        megaman.apply(mod.damageToRandomOpponentAtEndOfTurn(8));
        useAction(megaman, PhrancisGame.PLAY_ACTION);
        assertEquals(30, health.getFor(currentPlayer()));
        nextPhase();
        assertEquals(22, health.getFor(currentPlayer()));
    }

    @Test
	public void trample() {
		Entity attacker = mod.createCreature(0, field.get(currentPlayer()), 10, 1, "B0T", 0);
		Resources.retriever(TrampleSystem.trample).resFor(attacker).set(1);
		
		Entity defender = mod.createCreature(0, field.get(opponent()), 3, 3, "Bio", 0);
		nextPhase();
		nextPhase();
		assertResource(getOpponent(), PhrancisResources.HEALTH, originalLife);
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, defender);
		assertResource(getOpponent(), PhrancisResources.HEALTH, originalLife - 7);
	}
	
	@Test
	public void cannotEnchantPlayer() {
		Entity enchantment = mod.createEnchantment(hand.get(currentPlayer()), 4, 4, 0, NO_NAME);
		useActionWithFailedTarget(enchantment, PhrancisGame.ENCHANT_ACTION, currentPlayer());
	}

	@Test
	public void giveAbility() {
		Effects effects = new Effects();
		Entity enchantedCreature = mod.createCreature(0, field.get(currentPlayer()), 4, 4, "Bio", 0);
		Entity enchantment = mod.createEnchantment(hand.get(currentPlayer()), 0, 0, 0, "Adrenalin Injection")
			.addComponent(effects.giveTarget(PhrancisResources.SICKNESS, 0, i -> 0));

		Entity defender = opponent();
		int originalLife = health.getFor(defender);

		useFail(enchantedCreature, PhrancisGame.ATTACK_ACTION);
		useActionWithTarget(enchantment, PhrancisGame.ENCHANT_ACTION, enchantedCreature);
		assertResource(opponent(), PhrancisResources.HEALTH, originalLife);
		useActionWithTarget(enchantedCreature, PhrancisGame.ATTACK_ACTION, defender);
		assertResource(opponent(), PhrancisResources.HEALTH, originalLife - 4);
	}
	
	@Test
	public void summonWhenPlaying() {
		Effects effects = new Effects();
		Entity summon = mod.createCreature(0, hand.get(opponent()), 1, 2, "B0T", 0, "Summoned");
		Entity summoner = mod.createCreature(0, hand.get(currentPlayer()), 2, 6, "Bio", 0, "Summoner")
			.addComponent(effects.toSelf(e -> {
			Entity entity = Players.findOwnerFor(e);
			ZoneComponent field = entity.getComponent(BattlefieldComponent.class);
			field.addOnBottom(summon.copy());
			field.addOnBottom(summon.copy());
		}));

		assertEquals(0, field.get(currentPlayer()).size());
		useAction(summoner, PhrancisGame.PLAY_ACTION);
		assertEquals(3, field.get(currentPlayer()).size());
	}

	@Test
	public void enchantBio() {
		Entity attackerPlayer = currentPlayer();
		Entity enchantedCreature = mod.createCreature(0, field.get(currentPlayer()), 4, 4, "Bio", 0);
		Entity defender = mod.createCreature(0, field.get(opponent()), 3, 3, "Bio", 0);
		Entity enchantment = mod.createEnchantment(hand.get(currentPlayer()), 0, 1, 0, NO_NAME);
		
		useActionWithFailedTarget(enchantment, PhrancisGame.ENCHANT_ACTION, attackerPlayer);
		assertResource(enchantedCreature, PhrancisResources.ATTACK, 4);
		assertResource(enchantedCreature, PhrancisResources.HEALTH, 4);
		List<Entity> targets = getAction(enchantment, PhrancisGame.ENCHANT_ACTION).getTargetSets().get(0).findPossibleTargets();
		assertEquals(1, targets.size());
		useActionWithTarget(enchantment, PhrancisGame.ENCHANT_ACTION, enchantedCreature);
		assertResource(enchantedCreature, PhrancisResources.ATTACK, 4);
		assertResource(enchantedCreature, PhrancisResources.HEALTH, 5);
		
		nextPhase();
		nextPhase();
		
		assertFalse(defender.isRemoved());
		assertResource(defender, PhrancisResources.ATTACK, 3);
		assertResource(defender, PhrancisResources.HEALTH, 3);
		assertResource(enchantedCreature, PhrancisResources.ATTACK, 4);
		assertResource(opponent(), PhrancisResources.HEALTH, originalLife);
		useActionWithTarget(enchantedCreature, PhrancisGame.ATTACK_ACTION, defender);
		assertResource(opponent(), PhrancisResources.HEALTH, originalLife);
		assertTrue(defender.isRemoved());
	}

	private Entity opponent() {
		List<Entity> list = game.getEntitiesWithComponent(PlayerComponent.class).stream()
			.filter(entity -> entity != phase.getCurrentEntity())
			.collect(Collectors.toList());
		assertEquals("Found more than one opponent", 1, list.size());
		return list.get(0);
	}

	private Predicate<Entity> health(int value) {
		return entity -> health.getFor(entity) == value;
	}

	private Predicate<Entity> manaCost(int i) {
		return entity -> manaCost.getFor(entity) == i;
	}

	@Override
	protected void onAfterGameStart() {
		assertNull(phase.getCurrentEntity());
		List<Entity> list = new ArrayList<>(game.getEntitiesWithComponent(PlayerComponent.class));
		assertEquals(2, list.size());
		for (int i = 0; i < list.size(); i++) {
			Entity current = list.get(i);
			Entity other = list.get((i + 1) % list.size());
			
			ECSAction action = getAction(current, "Mulligan");
			assertFalse(action + "is allowed for " + other, action.perform(other));
		}
		
		for (Entity entity : game.getEntitiesWithComponent(PlayerComponent.class)) {
			ECSAction action = getAction(entity, "Mulligan");
			assertTrue(action + " not allowed for " + entity, action.perform(entity));
		}
	}

}
