package remonone.nftilation.game.meta;

import lombok.Data;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.utils.Logger;

import java.io.File;
import java.util.*;

public class MetaConfig {

    @Getter
    private final static MetaConfig instance = new MetaConfig();

    private File file;
    private YamlConfiguration configuration;
    
    @Getter
    private List<RoleInfo> roles = new ArrayList<>();
    
    @Getter
    private List<RuneInfo> runes = new ArrayList<>();
    
    @Getter
    private Map<String, Object> upgrades;
    
    public MetaConfig() {}
    
    public void Load() {
        Logger.log("Loading meta...");
        file = new File(Nftilation.getInstance().getDataFolder(), "meta.yml");
        if(!file.exists()) {
            Nftilation.getInstance().saveResource("meta.yml", false);
        }

        configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch(Exception e) {
            Logger.error("Error during loading meta: " + e.getMessage());
            return;
        }
        
        LoadMeta();
    }

    @SuppressWarnings("unchecked")
    private void LoadMeta() {
        List<RoleInfo> roles = (List<RoleInfo>) configuration.getList(MetaConstants.META_ROLES);
        if(roles != null) {
            this.roles = roles;
        }
        List<RuneInfo> runes = (List<RuneInfo>) configuration.getList(MetaConstants.META_RUNES);
        if(runes != null) {
            this.runes = runes;
        }
        UpgradesInfo list = (UpgradesInfo) configuration.get(MetaConstants.META_UPGRADES);
        this.upgrades = list.upgrades;
    }
    
    public Object getValue(String key) {
        if(configuration.contains(key)) {
            return configuration.get(key);
        }
        return null;
    }
    
    @Data
    @SerializableAs("UpgradesInfo")
    public static class UpgradesInfo implements ConfigurationSerializable {
        private Map<String, Object> upgrades = new HashMap<>();

        @Override
        public Map<String, Object> serialize() {
            return Collections.emptyMap();
        }
        
        public static UpgradesInfo deserialize(Map<String, Object> map) {
            UpgradesInfo info = new UpgradesInfo();
            if(map.containsKey("info")) {
                info.upgrades = (Map<String, Object>) map.get("info");
            }
            return info;
        }
    }
}
