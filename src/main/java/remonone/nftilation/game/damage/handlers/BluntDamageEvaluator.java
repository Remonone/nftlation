package remonone.nftilation.game.damage.handlers;

import org.bukkit.entity.LivingEntity;

public class BluntDamageEvaluator implements IDamageEvaluator {
    @Override
    public double calculateDamage(LivingEntity target, double damage) {
        return 0;
    }
}
