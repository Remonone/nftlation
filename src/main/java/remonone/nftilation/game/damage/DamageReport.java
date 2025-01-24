package remonone.nftilation.game.damage;

import lombok.AllArgsConstructor;
import org.bukkit.entity.LivingEntity;
import remonone.nftilation.game.models.PlayerModel;

@AllArgsConstructor
public class DamageReport {
    private final LivingEntity target;
    private final PlayerModel attacker;
    private final DamageType type;
    private final double damage;
}
