package remonone.nftilation.restore;

import org.bukkit.configuration.file.YamlConfiguration;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DumpConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DumpCollector {
    
    public static void GenerateDump() {
        File file = generateDumpFile();
        YamlConfiguration dump = new YamlConfiguration();
        try {
            fillDump(dump);
            dump.save(file);
        } catch (IOException e) {
            Logger.error("Cannot create dump file! " + e.getMessage());
        }
    }

    private static void fillDump(YamlConfiguration dump) {
        dump.set(DumpConstant.PLAYER_DUMP, getPlayers());
        dump.set(DumpConstant.TEAM_DUMP, getTeams());
        dump.set(DumpConstant.WORLD_DUMP, getWorld());
    }

    private static Object getWorld() {
        return null;
    }

    private static Object getTeams() {
        List<ITeam> teams = new ArrayList<>();
        GameInstance.getInstance().getTeamIterator().forEachRemaining(teams::add);
        return null;
    }

    private static File generateDumpFile() {
        String time = new Timestamp(System.currentTimeMillis()).toString()
                .replaceAll(" ", "_")
                .replaceAll("-", "_");
        int index = time.lastIndexOf(".");
        time = time.substring(0, index).replaceAll(":", ".");
        String fileName = "data_dump" + time + ".yml";
        File file = new File(Nftilation.getInstance().getDataFolder(), fileName);
        if(!file.exists()) {
            Nftilation.getInstance().saveResource(fileName, false);
        }
        return file;
    }

    public static List<PlayerCollection> getPlayers() {
        List<PlayerCollection> players = new ArrayList<>();
        Iterator<ITeam> teams = GameInstance.getInstance().getTeamIterator();
        while(teams.hasNext()) {
            ITeam team = teams.next();
            List<PlayerModel> models = team.getPlayers();
            List<PlayerCollection> collections = models.stream().map(model -> PlayerCollection.builder()
                    .currentHealth(model.getReference().getHealth())
                    .parameters(model.getParameters())
                    .tokens(model.getTokens())
                    .location(model.getReference().getLocation())
                    .build()).collect(Collectors.toList());
            players.addAll(collections);
        }
        return players;
    }
}
