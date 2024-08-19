package remonone.nftilation.restore;

import org.bukkit.configuration.file.YamlConfiguration;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DumpConstant;
import remonone.nftilation.utils.Logger;

import java.io.File;

public class DumpReader {

    public static boolean readDump(String filename) {
        File file = new File(Nftilation.getInstance().getDataFolder(), filename);
        if(!file.exists()) return false;
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch(Exception ex) {
            Logger.error(ex.getMessage());
            return false;
        }
        restoreFromDump(config);
        return true;
    }

    private static void restoreFromDump(YamlConfiguration config) {
        Object worldData = config.get(DumpConstant.WORLD_DUMP);
        restoreWorldInfo(worldData);
        Object teamData = config.get(DumpConstant.TEAM_DUMP);
        restoreTeams(teamData);
        Object playerData = config.get(DumpConstant.PLAYER_DUMP);
        restorePlayers(playerData);
    }

    private static void restoreTeams(Object teamData) {

    }

    private static void restorePlayers(Object playerData) {
    }

    private static void restoreWorldInfo(Object worldData) {
        
    }
}
