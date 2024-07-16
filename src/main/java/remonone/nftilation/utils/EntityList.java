package remonone.nftilation.utils;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityList {
    private static final List<LivingEntity> livingEntities = new ArrayList<>();
    
    public static void addEntity(LivingEntity entity) {
        livingEntities.add(entity);
    }
    
    public static void removeEntity(LivingEntity entity) {
        entity.remove();
        livingEntities.remove(entity);
    }
    
    public static void clearEntities() {
        livingEntities.forEach(entity -> entity.setHealth(0));
        livingEntities.clear();
    }
}
