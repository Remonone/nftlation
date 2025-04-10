package remonone.nftilation.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import remonone.nftilation.utils.VectorUtils;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@SerializableAs("TeamPoint")
@AllArgsConstructor
public class TeamSpawnPoint implements Cloneable, ConfigurationSerializable {

    private String id;
    private Location position;
    private Location coreCenter;
    private Location shopKeeperPosition;
    private Location checkerChestPosition;
    private Location airDropPosition;
    
    @Override
    public String toString() {
        return "TeamSpawnPoint [id=" + id + ", position=" + VectorUtils.convertRoundVectorString(position.toVector()) + ", coreCenter=" + VectorUtils.convertRoundVectorString(coreCenter.toVector()) + "];";
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("position", position);
        result.put("coreCenter", coreCenter);
        result.put("shopKeeper", shopKeeperPosition);
        result.put("checkerChest", checkerChestPosition);
        result.put("airDropPosition", airDropPosition);

        return result;
    }

    public TeamSpawnPoint() {}

    @Override
    public TeamSpawnPoint clone() {
        try {
            return (TeamSpawnPoint) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public static TeamSpawnPoint deserialize(Map<String, Object> args) {
        String id = "";
        Location pos = new Location(null, 0, 0, 0);
        Location center = null;
        Location shopKeeper = new Location(null, 0, 0, 0);
        Location checkerChest = new Location(null, 0, 0, 0);
        Location airDropPosition = new Location(null, 0, 0, 0);

        if (args.containsKey("id")) {
            id = (String) args.get("id");
        }
        if (args.containsKey("position")) {
            pos = (Location) args.get("position");
        }
        if (args.containsKey("coreCenter")) {
            center = (Location) args.get("coreCenter");
        }
        if(args.containsKey("shopKeeper")) {
            shopKeeper = (Location) args.get("shopKeeper");
        }
        if(args.containsKey("checkerChest")) {
            checkerChest = (Location) args.get("checkerChest");
        }
        if(args.containsKey("airDropPosition")) {
            airDropPosition = (Location) args.get("airDropPosition");
        }

        return new TeamSpawnPoint(id, pos, center, shopKeeper, checkerChest, airDropPosition);
    }
}
