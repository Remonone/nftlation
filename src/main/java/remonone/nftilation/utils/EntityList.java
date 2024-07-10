package remonone.nftilation.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityList {
    private static List<LivingEntity> livingEntities = new ArrayList<LivingEntity>();
    
    public EntityList() {}
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
