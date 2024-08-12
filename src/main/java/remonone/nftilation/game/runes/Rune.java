package remonone.nftilation.game.runes;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import remonone.nftilation.Nftilation;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.meta.RuneInfo;
import remonone.nftilation.game.models.IInventoryHelder;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

@Getter
public abstract class Rune implements Listener, IInventoryHelder {
    
    @Getter
    private final static List<Rune> runes = new ArrayList<>();
    
    private final String name;
    private final Material material;
    private final List<String> description;
    private final int index;
    private final Map<String, Object> meta;
    
    protected Rune(String id) {
        RuneInfo info = MetaConfig.getInstance().getRunes().stream().filter(runeInfo -> runeInfo.getRuneId().equals(id)).findFirst().orElse(null);
        if(info == null) {
            Logger.error("Cannot load rune with id: " + id);
            this.name = "";
            this.material = Material.AIR;
            this.description = new ArrayList<>();
            this.index = 0;
            this.meta = new HashMap<>();
            return;
        }
        this.name = info.getRuneName();
        this.material = Material.getMaterial(info.getRuneMaterial());
        this.description = info.getDescription();
        this.index = info.getRuneIndex();
        this.meta = info.getMetaInfo();
    }

    public static void registerRune(Class<? extends Rune> rune) {
        try {
            Rune runeToAdd = rune.getDeclaredConstructor().newInstance();
            if(runes.contains(runeToAdd)) return;
            getServer().getPluginManager().registerEvents(runeToAdd, Nftilation.getInstance());
            runes.add(runeToAdd);
        } catch(Exception e) {
            Logger.error("Error during registering the rune: " + e.getLocalizedMessage());
        }
    }
    
    public abstract String getRuneID();

    public static Rune getRuneByID(String runeID) {
        return runes.stream().filter(role -> role.getRuneID().equals(runeID)).findFirst().orElse(null);
    }
    
    protected void setPlayer(PlayerModel model) {}
    
}
