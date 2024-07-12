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

    public static boolean isEntityHostile(Entity entity) {
        List<MetadataValue> values = entity.getMetadata("hostile");
        if(values.isEmpty()) return false;
        return (Boolean) entity.getMetadata("hostile").get(0).value();
    }
    
}
