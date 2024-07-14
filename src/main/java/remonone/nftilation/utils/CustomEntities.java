package remonone.nftilation.utils;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.entity.EntityType;
import remonone.nftilation.game.mob.AngryGolem;
import remonone.nftilation.game.mob.RuslanBlaze;


public enum CustomEntities {
    ANGRY_GOLEM("AngryGolem", 74, EntityType.IRON_GOLEM, EntityIronGolem.class, AngryGolem.class),
    RUSLAN_BLAZE("RuslanBlaze", 34,EntityType.BLAZE, EntityBlaze.class, RuslanBlaze.class);
    @Getter
    private final String name;
    private final int id;
    @Getter
    private final EntityType entityType;
    private final Class<? extends Entity> nmsClass;
    private final Class<? extends Entity> customClass;
    private final MinecraftKey key;
    private final MinecraftKey oldKey;

    CustomEntities(String name, int id, EntityType entityType, Class<? extends Entity> nmsClass, Class<? extends Entity> customClass) {
        this.name = name;
        this.id = id;
        this.entityType = entityType;
        this.nmsClass = nmsClass;
        this.customClass = customClass;
        this.key = new MinecraftKey(name);
        this.oldKey = EntityTypes.b.b(nmsClass);
    }

    public static void registerEntities() { for (CustomEntities ce : CustomEntities.values()) ce.register(); }
    public static void unregisterEntities() { for (CustomEntities ce : CustomEntities.values()) ce.unregister(); }

    private void register() {
        EntityTypes.d.add(key);
        EntityTypes.b.a(EntityTypes.b.a(nmsClass), key, customClass);
    }

    private void unregister() {
        EntityTypes.d.remove(key);
        EntityTypes.b.a(EntityTypes.b.a(nmsClass), oldKey, nmsClass);
    }

    public int getID() {
        return id;
    }

    public Class<?> getCustomClass() {
        return customClass;
    }
}
