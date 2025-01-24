package remonone.nftilation.game.damage;

import org.bukkit.event.entity.EntityDamageEvent;

public enum DamageType {
    BLUNT,
    PIERCING,
    MAGIC,
    ARCANE,
    EXPLOSIVE,
    VOID,
    CHAOTIC;
    
    public static DamageType getTypeByMinecraftType(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case VOID:
                return VOID;
            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION:
                return EXPLOSIVE;
            case WITHER:
            case THORNS:
                return ARCANE;
            case FIRE:
            case POISON:
                return MAGIC;
            case PROJECTILE:
                return PIERCING;
            default:
                return BLUNT;
        }
    }
}
