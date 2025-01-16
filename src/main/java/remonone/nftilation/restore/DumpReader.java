package remonone.nftilation.restore;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.services.SkinCache;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.DumpConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameConfiguration;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.*;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerNMSUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DumpReader {
    
    private static final DumpReader instance = new DumpReader();
    
    public static boolean readDump(String filename) {
        if(!Stage.IN_GAME.equals(Store.getInstance().getGameStage().getStage())) {
            Logger.error("Game can be restored within IN_GAME stage only!");
            return false;
        }
        File file = new File(Nftilation.getInstance().getDataFolder(), filename);
        if(!file.exists()) return false;
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch(Exception ex) {
            Logger.error(ex.getMessage());
            return false;
        }
        RuleManager.getInstance().setRule(RuleConstants.RULE_PLAYERS_ABLE_TO_MOVE, false);
        instance.restoreFromDump(config);
        return true;
    }

    private void restoreFromDump(YamlConfiguration config) {
        Object worldData = config.get(DumpConstant.WORLD_DUMP);
        restoreWorldInfo(worldData);
        Object teamData = config.get(DumpConstant.TEAM_DUMP);
        List<IModifiableTeam> teams = restoreTeams(teamData);
        Object playerData = config.get(DumpConstant.PLAYER_DUMP);
        restorePlayers(teams, playerData);
        GameInstance.getInstance().setTeamData(teams);
        Logger.log("Restoration completed successfully");
        RuleManager.getInstance().setRule(RuleConstants.RULE_PLAYERS_ABLE_TO_MOVE, true);
    }


    @SuppressWarnings("unchecked")
    private List<IModifiableTeam> restoreTeams(Object teamData) {
        List<IModifiableTeam> teams = new ArrayList<>();
        
        List<TeamCollection> collections = (List<TeamCollection>) teamData;
        for(TeamCollection collection : collections) {
            IModifiableTeam team;
            if(collection.isOriginalTeam()) {
                team = constructFullTeam(collection);
            } else {
                team = constructShallowTeam(collection);
            }
            teams.add(team);
        }
        return teams;
    }

    private IModifiableTeam constructFullTeam(TeamCollection collection) {
        TeamSpawnPoint point = ConfigManager.getInstance().getTeamSpawnList().stream().filter(teamSpawnPoint -> teamSpawnPoint.getId().equals(collection.getTeamPointId())).findFirst().orElseThrow(() -> new IllegalArgumentException("Team Position not found"));
        return TeamImpl.builder()
                .teamName(collection.getTeamName())
                .teamColor(ChatColor.getByChar(collection.getTeamColor()))
                .parameters(collection.getTeamParams())
                .players(constructPlayerPlaceholders(collection.getPlayerNames()))
                .spawnPoint(point)
                .core(GameConfiguration.setCore(point))
                .isTeamActive(collection.isTeamActive())
                .isCoreAlive(collection.getCoreHealth() > 0)
                .build();
    }

    private IModifiableTeam constructShallowTeam(TeamCollection collection) {
        return TruncatedTeam.builder()
                .teamName(collection.getTeamName())
                .players(constructPlayerPlaceholders(collection.getPlayerNames()))
                .build();
    }

    private List<PlayerModel> constructPlayerPlaceholders(List<String> playerNames) {
        List<PlayerModel> models = new ArrayList<>();
        for(String playerName : playerNames) {
            PlayerModel emptyModel = new PlayerModel(null, 0, new HashMap<String, Object>(){{
                put("holder", playerName);
            }});
            models.add(emptyModel);
        }
        return models;
    }

    @SuppressWarnings("unchecked")
    private void restorePlayers(List<IModifiableTeam> teams, Object playerData) {
        List<PlayerCollection> players = (List<PlayerCollection>) playerData;
        List<PlayerModel> models = new ArrayList<>();
        for (PlayerCollection collection : players) {
            String teamName = (String) collection.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
            ITeam team = teams.stream().filter(p -> p.getTeamName().equals(teamName)).findFirst().orElse(null);
            if(team == null) {
                Logger.error("Team " + teamName + " not found for player" + collection.getLogin());
                continue;
            }
            team.getPlayers().removeIf(model -> model.getParameters().get("holder").equals(collection.getLogin()));
            PlayerModel model = constructPlayerModel(collection);
            team.getPlayers().add(model);
            PlayerNMSUtil.changePlayerSkin(model.getReference(), SkinCache.getInstance().getTexture((String)model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID)), SkinCache.getInstance().getSignature((String)model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID)));
            Role.updatePlayerAbilities(model);
            model.getReference().getInventory().setContents(collection.getInventory());
            model.getReference().teleport(collection.getLocation());
            model.getReference().setHealth(collection.getCurrentHealth());
            model.getReference().setFoodLevel(collection.getCurrentFoodLevel());
            
            models.add(model);
        }
        models.forEach(ScoreboardHandler::updateScoreboard);
        
    }

    private PlayerModel constructPlayerModel(PlayerCollection collection) {
        Player player = Store.getInstance().getDataInstance().getPlayerByLogin(collection.getLogin());
        return new PlayerModel(player, collection.getTokens(), collection.getParameters());
    }

    private void restoreWorldInfo(Object worldData) {
        WorldCollection collection = (WorldCollection) worldData;
        GameInstance.getInstance().initTimer(collection.getPhase(), collection.getSeconds());
    }
}
