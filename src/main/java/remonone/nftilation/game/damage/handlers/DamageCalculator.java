package remonone.nftilation.game.damage.handlers;

import org.bukkit.entity.LivingEntity;
import remonone.nftilation.game.damage.DamageType;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;
import java.util.Map;

public class DamageCalculator {
    private final static Map<DamageType, IDamageEvaluator> evaluators = new HashMap<>();
    
    static {
        evaluators.put(DamageType.BLUNT, new BluntDamageEvaluator());
        evaluators.put(DamageType.PIERCING, new PiercingDamageEvaluator());
        evaluators.put(DamageType.MAGIC, new MagicDamageEvaluator());
        evaluators.put(DamageType.ARCANE, new ArcaneDamageEvaluator());
        evaluators.put(DamageType.EXPLOSIVE, new ExplosiveDamageEvaluator());
        evaluators.put(DamageType.VOID, new VoidDamageEvaluator());
        evaluators.put(DamageType.CHAOTIC, new ChaoticDamageEvaluator());
    }

    public static double calculateDamage(LivingEntity target, double damage, DamageType type) {
        IDamageEvaluator evaluator = evaluators.get(type);
        if (evaluator == null) {
            Logger.warn(type.name() + " not registered!");
            return damage;
        }
        return evaluator.calculateDamage(target, damage);
    }
}
