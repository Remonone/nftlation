package remonone.nftilation.utils;

import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntityList {
    private static final List<Entity> livingEntities = new ArrayList<>();
    
    public static void addEntity(Entity entity) {
        livingEntities.add(entity);
    }
    
    public static void removeEntity(Entity entity) {
        entity.remove();
        livingEntities.remove(entity);
    }
    
    public static void clearEntities() {
        livingEntities.forEach(Entity::remove);
        livingEntities.clear();
    }
}
