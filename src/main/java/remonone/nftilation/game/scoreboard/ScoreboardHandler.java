package remonone.nftilation.game.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import remonone.nftilation.Store;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class ScoreboardHandler {
    
    public static void buildScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Nftlation", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        fillObjective(model, objective);
        player.setScoreboard(scoreboard);
    }

    private static void fillObjective(PlayerModel model, Objective objective) {
        GameInstance instance = GameInstance.getInstance();
        int counter = 0;
        Map<String, Object> playerParams = model.getParameters();
        if(!PlayerUtils.validateParams(playerParams)) {
            Logger.error("Params for player: " + model.getReference().getDisplayName() + " were not set properly! Skipping...");
            return;
        }
        objective.getScore("Level: " + playerParams.get(PropertyConstant.PLAYER_LEVEL_PARAM)).setScore(++counter);
        objective.getScore("Tokens: " + model.getTokens()).setScore(++counter);
        objective.getScore("Role: " + Role.getRoleByID((String)playerParams.get(PropertyConstant.PLAYER_ROLE_ID)).getRoleName()).setScore(++counter);
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(model.getReference().getUniqueId());
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        objective.getScore("Core Health: " + team.getCoreData().getHealth()).setScore(++counter);
        objective.getScore(ChatColor.RED + "" + ChatColor.BOLD + "= Info =").setScore(++counter);
        objective.getScore(ChatColor.RED + "").setScore(++counter);
        objective.getScore("Deaths: " + playerParams.get(PropertyConstant.PLAYER_DEATH_COUNT)).setScore(++counter);
        objective.getScore("Kills: " + playerParams.get(PropertyConstant.PLAYER_KILL_COUNT)).setScore(++counter);
        objective.getScore(ChatColor.RED + "" + ChatColor.BOLD + "= Stats =").setScore(++counter);
        objective.getScore(ChatColor.BLUE + "").setScore(++counter);
        Iterator<ITeam> it = instance.getTeamIterator();
        while(it.hasNext()) {
            ITeam t = it.next();
            String scoreName = teamName + "[";
            if(t.isCoreAlive()) {
                scoreName += ChatColor.GREEN + "" + ChatColor.BOLD + "✓";
            } else if(t.isTeamActive()) {
                scoreName += ChatColor.DARK_RED + "" + ChatColor.BOLD +  "x";
            } else {
                scoreName += ChatColor.DARK_RED + "" + ChatColor.BOLD + getTeamMembersAlive(teamName);
            }
            scoreName += ChatColor.RESET + "]";
            objective.getScore(scoreName).setScore(++counter);
        }
        objective.getScore(ChatColor.GOLD + "= Teams =").setScore(++counter);
    }
    
    private static int getTeamMembersAlive(String teamName) {
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        if(team == null) return 0;
        if(team.isCoreAlive()) return team.getPlayers().size();
        Collection<PlayerModel> alivePlayers = team.getPlayers().stream().filter(playerModel -> (Boolean)playerModel.getParameters().getOrDefault(PropertyConstant.PLAYER_IS_ALIVE_PARAM, false)).collect(Collectors.toList());
        return alivePlayers.size();
    }

    public static void updateScoreboard(PlayerModel model) {
        Scoreboard scoreboard = model.getReference().getScoreboard();
        if(scoreboard.getObjective("Nftlation") != null) {
            scoreboard.getObjective("Nftlation").unregister(); 
        }
        Objective newObjective = scoreboard.registerNewObjective("Nftlation", "dummy");
        newObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        fillObjective(model, newObjective);
    }
}
