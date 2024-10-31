package remonone.nftilation.game.meta;

import lombok.Data;
import lombok.Getter;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.utils.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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

    @Getter
    private Map<String, Object> events;
    
    @Getter
    private Map<Integer, List<GlobalEvent>> globalEvents = new HashMap<>();
    
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
        ContentInfo upgradeList = (ContentInfo) configuration.get(MetaConstants.META_UPGRADES);
        this.upgrades = upgradeList.content;
        ContentInfo eventList = (ContentInfo) configuration.get(MetaConstants.META_EVENTS);
        this.events = eventList.content;
        Map<String, Object> events = ((MemorySection)configuration.get(MetaConstants.META_GLOBAL)).getValues(true);
        this.globalEvents = events.entrySet().stream().collect(
                Collectors.toMap(value -> Integer.parseInt(value.getKey()),
                        e -> (List<GlobalEvent>) e.getValue()));
        Logger.log(this.globalEvents.toString());
    }
    
    public Object getValue(String key) {
        if(configuration.contains(key)) {
            return configuration.get(key);
        }
        return null;
    }
    
    @Data
    @SerializableAs("ContentInfo")
    public static class ContentInfo implements ConfigurationSerializable {
        private Map<String, Object> content = new HashMap<>();

        @Override
        public Map<String, Object> serialize() {
            return Collections.emptyMap();
        }
        
        public static ContentInfo deserialize(Map<String, Object> map) {
            ContentInfo info = new ContentInfo();
            if(map.containsKey("info")) {
                info.content = (Map<String, Object>) map.get("info");
            }
            return info;
        }
    }
    
    @Data
    @SerializableAs("GlobalEvent")
    public static class GlobalEvent implements ConfigurationSerializable {
        private int delay;
        private String name;
        private Map<String, Object> additionalParams;
        
        @Override
        public Map<String, Object> serialize() {
            return Collections.emptyMap();
        }
        
        public static GlobalEvent deserialize(Map<String, Object> map) {
            GlobalEvent event = new GlobalEvent();
            if(map.containsKey("delay")) {
                event.setDelay(Integer.parseInt(map.get("delay").toString()));
            }
            if(map.containsKey("name")) {
                event.setName(map.get("name").toString());
            }
            if(map.containsKey("params")) {
                event.setAdditionalParams((Map<String, Object>) map.get("params"));
            } else {
                event.setAdditionalParams(new HashMap<>());
            }
            return event;
        }
    }
}
