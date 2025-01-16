package remonone.nftilation.restore;

import org.bukkit.configuration.file.YamlConfiguration;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DumpConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.phase.PhaseCounter;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DumpCollector {
    
    public static void generateDump() {
        File file = generateDumpFile();
        YamlConfiguration dump = new YamlConfiguration();
        try {
            fillDump(dump);
            dump.save(file);
            Logger.log("Backup was successfully created!");
        } catch (IOException e) {
            Logger.error("Cannot create dump file! " + e.getMessage());
        }
    }

    private static File generateDumpFile() {
        String time = new Timestamp(System.currentTimeMillis()).toString()
                .replaceAll(" ", "_")
                .replaceAll("-", "_");
        int index = time.lastIndexOf(".");
        time = time.substring(0, index).replaceAll(":", ".");
        String fileName = "data_dump" + time + ".yml";
        File file = new File(Nftilation.getInstance().getDataFolder() + "/dumps", fileName);
        try {
            if(!file.exists()) {
                if(!file.createNewFile()) {
                    Logger.error("Cannot create dump file! " + fileName);
                }
            }
        } catch (IOException e) {
            Logger.error("Cannot create dump file! " + e.getMessage());
        }
        return file;
    }

    private static void fillDump(YamlConfiguration dump) {
        dump.set(DumpConstant.PLAYER_DUMP, getPlayers());
        dump.set(DumpConstant.TEAM_DUMP, getTeams());
        dump.set(DumpConstant.WORLD_DUMP, getWorld());
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
                    .inventory(model.getReference().getInventory().getContents())
                    .login(PlayerUtils.getOriginalPlayerName(model.getReference()))
                    .build()).collect(Collectors.toList());
            players.addAll(collections);
        }
        return players;
    }

    private static Object getWorld() {
        PhaseCounter counter = GameInstance.getInstance().getCounter();
        int seconds = counter.getSeconds();
        int phase = counter.getPhase();
        return new WorldCollection(seconds, phase);
    }

    private static Object getTeams() {
        List<TeamCollection> teams = new ArrayList<>();
        GameInstance.getInstance().getTeamIterator().forEachRemaining(team -> {
            TeamCollection collection = TeamCollection.getCollectionFromTeam(team);
            teams.add(collection);
        });
        return teams;
    }
}
