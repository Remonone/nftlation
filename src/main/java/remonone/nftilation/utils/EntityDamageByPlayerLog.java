package remonone.nftilation.utils;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import remonone.nftilation.constants.DataConstants;

import javax.annotation.Nullable;
import java.util.*;


public class EntityDamageByPlayerLog {
    
    private final static Map<UUID, DamageLog> queueDictionary = new HashMap<>();
    
    public static void insertLogEvent(LivingEntity target, Player damager) {
        
        UUID uuid = target.getUniqueId();
        if(!queueDictionary.containsKey(uuid)) {
            queueDictionary.put(uuid, null);
        }
        queueDictionary.put(uuid, new DamageLog(damager, System.currentTimeMillis() + (DataConstants.DAMAGE_REPORT_LIFETIME * DataConstants.ONE_SECOND)));
    }
    
    @Nullable
    public static Player getEventLogForLivingEntity(UUID livingEntity) {
        if(!queueDictionary.containsKey(livingEntity)) { return null; }
        DamageLog log = queueDictionary.get(livingEntity);
        if(log == null) { return null; }
        if(log.compareTo(System.currentTimeMillis()) >= 0) {
            queueDictionary.remove(livingEntity);
            return null;
        }
        return log.damager;
    }

    public static void removeLogEvent(UUID uuid) {
        if(!queueDictionary.containsKey(uuid)) { return; }
        queueDictionary.remove(uuid);
    }
    
    @Setter
    @AllArgsConstructor
    private static class DamageLog implements Comparable<Long>{
        Player damager;
        long insertionTime;

        @Override
        public int compareTo(Long time) {
            return Long.compare(time, insertionTime);
        }
    }
}
