package remonone.nftilation.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;
import remonone.nftilation.utils.VectorUtils;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@SerializableAs("TeamPoint")
@AllArgsConstructor
public class TeamSpawnPoint implements Cloneable, ConfigurationSerializable {

    private String id;
    private Vector position;
    private Vector coreCenter;
    private Vector shopKeeperPosition;
    
    @Override
    public String toString() {
        return "TeamSpawnPoint [id=" + id + ", position=" + VectorUtils.convertRoundVectorString(position) + ", coreCenter=" + VectorUtils.convertRoundVectorString(coreCenter) + "];";
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("position", position);
        result.put("coreCenter", coreCenter);
        result.put("shopKeeper", shopKeeperPosition);

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
        Vector pos = new Vector();
        Vector center = new Vector();
        Vector shopKeeper = new Vector();

        if (args.containsKey("id")) {
            id = (String) args.get("id");
        }
        if (args.containsKey("position")) {
            pos = (Vector) args.get("position");
        }
        if (args.containsKey("coreCenter")) {
            center = (Vector) args.get("coreCenter");
        }
        if(args.containsKey("shopKeeper")) {
            shopKeeper = (Vector) args.get("shopKeeper");
        }

        return new TeamSpawnPoint(id, pos, center, shopKeeper);
    }
}
