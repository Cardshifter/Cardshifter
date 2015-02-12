package net.zomis.cardshifter.ecs;

import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.attack.TrampleSystem;
import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.base.*;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.HandComponent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.players.Players;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.cardshifter.modapi.resources.Resources;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import net.zomis.cardshifter.ecs.effects.Effects;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;
import net.zomis.cardshifter.ecs.usage.TestMod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestModTest extends GameTest {

	private final ComponentRetriever<BattlefieldComponent> field = ComponentRetriever.retreiverFor(BattlefieldComponent.class);
	private final ComponentRetriever<HandComponent> hand = ComponentRetriever.retreiverFor(HandComponent.class);

	private final Predicate<Entity> hasName(String str) {
		AttributeRetriever name = AttributeRetriever.forAttribute(Attributes.NAME);
		return e -> name.getOrDefault(e, "").equals(str);
	}

	@Override
	protected void setupGame(ECSGame game) {
		ECSMod mod = new TestMod();
		mod.declareConfiguration(game);
		mod.setupGame(game);
	}
	
	@Test
	public void toOpponentHand() {
		Entity entity = cardToHand(hasName("Test"));
		HandComponent oppHand = hand.get(opponent());
		HandComponent myHand = hand.get(currentPlayer());
		assertEquals(5, myHand.size());
		assertEquals(5, oppHand.size());
		useAction(entity, "2-Hand");
		assertEquals(4, myHand.size());
		assertEquals(6, oppHand.size());
	}
	
	private Entity opponent() {
		List<Entity> list = game.getEntitiesWithComponent(PlayerComponent.class).stream()
			.filter(entity -> entity != phase.getCurrentEntity())
			.collect(Collectors.toList());
		assertEquals("Found more than one opponent", 1, list.size());
		return list.get(0);
	}

	@Override
	protected void onAfterGameStart() {
		assertNotNull(phase.getCurrentEntity());
		List<Entity> list = new ArrayList<>(game.getEntitiesWithComponent(PlayerComponent.class));
		assertEquals(2, list.size());
	}

}
