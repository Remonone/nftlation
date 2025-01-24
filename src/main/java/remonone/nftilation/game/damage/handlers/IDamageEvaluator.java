package remonone.nftilation.game.damage.handlers;

import org.bukkit.entity.LivingEntity;
import remonone.nftilation.game.models.PlayerModel;

public interface IDamageEvaluator {
    double calculateDamage(LivingEntity target, double damage);
}
