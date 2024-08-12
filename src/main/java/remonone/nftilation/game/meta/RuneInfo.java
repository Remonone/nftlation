package remonone.nftilation.game.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

@Data
@SerializableAs("RuneInfo")
@AllArgsConstructor
public class RuneInfo implements Cloneable, ConfigurationSerializable {
    private String runeId;
    private String runeName;
    private String runeMaterial;
    private List<String> description;
    private int runeIndex;
    private Map<String, Object> metaInfo;
    
    @Override
    public Map<String, Object> serialize() {
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public static RuneInfo deserialize(final Map<String, Object> args) {
        String id = "";
        String materialName = "";
        String runeName = "";
        List<String> description = new ArrayList<>();
        int roleIndex = -1;
        Map<String, Object> meta = new HashMap<>();
        if(args.containsKey("id")) {
            id = args.get("id").toString();
        }
        if(args.containsKey("material")) {
            materialName = args.get("material").toString();
        }
        if(args.containsKey("name")) {
            runeName = args.get("name").toString();
        }
        if(args.containsKey("description")) {
            description = (List<String>) args.get("description");
        }
        if(args.containsKey("index")) {
            roleIndex = (int)args.get("index");
        }
        if(args.containsKey("meta")) {
            meta = (Map<String, Object>) args.get("meta");
        }
        return new RuneInfo(id, runeName, materialName, description, roleIndex, meta);
    }

    @Override
    public RuneInfo clone() {
        try {
            return (RuneInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
