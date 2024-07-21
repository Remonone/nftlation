package remonone.nftilation.components;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import remonone.nftilation.Nftilation;

import java.util.List;


public class EntityHandleComponent {
    
    public static void setEntityOwner(Entity entityToImpl, Player owner) {
        entityToImpl.setMetadata("owner", new FixedMetadataValue(Nftilation.getInstance(), owner));
    }
    
    public static Player getEntityOwner(Entity entity) {
        List<MetadataValue> values = entity.getMetadata("owner");
        if(values.isEmpty()) return null;
        return (Player) entity.getMetadata("owner").get(0).value();
    }

    public static void setEntityHostile(Entity entityToHostile) {
        entityToHostile.setMetadata("hostile", new FixedMetadataValue(Nftilation.getInstance(), true));
    }
    
    public static void setEntityBounty(Entity entityToBounty, int award) {
        entityToBounty.setMetadata("bounty", new FixedMetadataValue(Nftilation.getInstance(), award));
    }
    
    public static void setEntityUnloadLocked(Entity entityToLock) {
        entityToLock.setMetadata("locked", new FixedMetadataValue(Nftilation.getInstance(), true));
    }
    
    public static boolean isEntityLockedForUnload(Entity entity) {
        List<MetadataValue> values = entity.getMetadata("locked");
        if(values.isEmpty()) return false;
        return (Boolean) entity.getMetadata("locked").get(0).value();
    }

    public static boolean isEntityHostile(Entity entity) {
        List<MetadataValue> values = entity.getMetadata("hostile");
        if(values.isEmpty()) return false;
        return (Boolean) entity.getMetadata("hostile").get(0).value();
    }
    
    public static int getEntityBounty(Entity entity) {
        List<MetadataValue> values = entity.getMetadata("bounty");
        if(values.isEmpty()) return 0;
        return (Integer) entity.getMetadata("bounty").get(0).value();
    }
    
}
