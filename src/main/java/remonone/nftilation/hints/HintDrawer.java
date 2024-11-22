package remonone.nftilation.hints;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import remonone.nftilation.utils.VectorUtils;

public class HintDrawer {
    
    public static Entity printHint(Hint hint) {
        Location location = hint.getLocation();
        location.add(VectorUtils.DOWN);
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setVisible(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(hint.getData());
        stand.setGravity(false);
        stand.setSmall(true);
        return stand;
    }
}
