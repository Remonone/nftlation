package remonone.nftilation.game.meta;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MetaConfig {

    @Getter
    private final static MetaConfig instance = new MetaConfig();

    private File file;
    private YamlConfiguration configuration;
    
    @Getter
    private List<RoleInfo> roles = new ArrayList<>();
    
    @Getter
    private List<RuneInfo> runes = new ArrayList<>();
    
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
    }
    
    public Object getValue(String key) {
        if(configuration.contains(key)) {
            return configuration.get(key);
        }
        return null;
    }
}
