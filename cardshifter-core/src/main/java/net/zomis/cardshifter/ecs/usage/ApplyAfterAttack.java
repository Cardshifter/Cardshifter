package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.Entity;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Apply something to an entity after it attacks, if it has survived and fulfills a criteria
 */
public class ApplyAfterAttack extends SpecificActionSystem {
    private final Predicate<Entity> condition;
    private final Consumer<Entity> apply;

    public ApplyAfterAttack(Predicate<Entity> condition, Consumer<Entity> apply) {
        super("Attack");
        this.condition = condition;
        this.apply = apply;
    }

    @Override
    protected void onPerform(ActionPerformEvent event) {
        if (event.getEntity().isRemoved()) {
            return;
        }
        if (condition.test(event.getEntity())) {
            apply.accept(event.getEntity());
        }
    }
}
