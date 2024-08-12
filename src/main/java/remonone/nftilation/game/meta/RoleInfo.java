package remonone.nftilation.game.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

@Data
@SerializableAs("RoleInfo")
@AllArgsConstructor
public class RoleInfo implements Cloneable, ConfigurationSerializable {
    private String roleId;
    private String materialName;
    private String roleName;
    private List<String> description;
    private int roleIndex;
    private Map<String, Object> metaInfo;

    @Override
    public Map<String, Object> serialize() {
        return Collections.emptyMap();
    }
    
    @SuppressWarnings("unchecked")
    public static RoleInfo deserialize(final Map<String, Object> args) {
        String id = "";
        String materialName = "";
        String roleName = "";
        List<String> description = new ArrayList<>();
        int roleIndex = -1;
        Map<String, Object> meta = new HashMap<>();
        if(args.containsKey("id")) {
            id = args.get("id").toString();
        }
        if(args.containsKey("materialName")) {
            materialName = args.get("materialName").toString();
        }
        if(args.containsKey("roleName")) {
            roleName = args.get("roleName").toString();
        }
        if(args.containsKey("description")) {
            description = (List<String>) args.get("description");
        }
        if(args.containsKey("roleIndex")) {
            roleIndex = (int)args.get("roleIndex");
        }
        if(args.containsKey("meta")) {
            meta = (Map<String, Object>) args.get("meta");
        }
        return new RoleInfo(id, materialName, roleName, description, roleIndex, meta);
    }

    @Override
    public RoleInfo clone() {
        try {
            return (RoleInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
