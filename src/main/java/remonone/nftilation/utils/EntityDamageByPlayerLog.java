package remonone.nftilation.utils;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import remonone.nftilation.constants.DataConstants;

import javax.annotation.Nullable;
import java.util.*;


public class EntityDamageByPlayerLog {
    
    private final static long LIFE_TIME = 10 * DataConstants.ONE_SECOND;
    
    private final static Map<UUID, DamageLog> queueDictionary = new HashMap<>();
    
    public static void insertLogEvent(LivingEntity target, Player damager) {
        
        UUID uuid = target.getUniqueId();
        if(!queueDictionary.containsKey(uuid)) {
            queueDictionary.put(uuid, null);
        }
        queueDictionary.put(uuid, new DamageLog(damager, System.currentTimeMillis() + LIFE_TIME));
    }
    
    @Nullable
    public static Player getEventLogForLivingEntity(UUID livingEntity) {
        if(!queueDictionary.containsKey(livingEntity)) { return null; }
        DamageLog log = queueDictionary.get(livingEntity);
        if(log == null) { return null; }
        if(log.insertionTime < System.currentTimeMillis()) {
            queueDictionary.remove(livingEntity);
            return null;
        }
        return log.damager;
    }
    
    @Setter
    @AllArgsConstructor
    private static class DamageLog implements Comparable<DamageLog>{
        Player damager;
        long insertionTime;

        @Override
        public int compareTo(DamageLog log) {
            return Long.compare(log.insertionTime, insertionTime);
        }
    }
}
