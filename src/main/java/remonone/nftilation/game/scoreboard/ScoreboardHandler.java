package remonone.nftilation.game.scoreboard;

import org.apache.commons.lang.StringUtils;
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
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Iterator;
import java.util.Map;

public class ScoreboardHandler {
    
    public static void buildScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Nftilation", "dummy");
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
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(model.getReference().getUniqueId());
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        if(!team.isUILayoutExists()) return;
        objective.getScore("Level: " + playerParams.get(PropertyConstant.PLAYER_LEVEL_PARAM)).setScore(++counter);
        objective.getScore("Tokens: " + model.getTokens()).setScore(++counter);
        objective.getScore("Rune: " + Rune.getRuneByID((String)playerParams.get(PropertyConstant.PLAYER_RUNE_ID)).getName()).setScore(++counter);
        objective.getScore("Role: " + Role.getRoleByID((String)playerParams.get(PropertyConstant.PLAYER_ROLE_ID)).getName()).setScore(++counter);
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
            String teamInfo = t.getTeamInfo();
            if(StringUtils.isBlank(teamInfo)) continue;
            objective.getScore(teamInfo).setScore(++counter);
        }
        objective.getScore(ChatColor.GOLD + "= Teams =").setScore(++counter);
    }

    public static void updateScoreboard(PlayerModel model) {
        Scoreboard scoreboard = model.getReference().getScoreboard();
        if(scoreboard.getObjective("Nftilation") != null) {
            scoreboard.getObjective("Nftilation").unregister(); 
        }
        Objective newObjective = scoreboard.registerNewObjective("Nftilation", "dummy");
        newObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        fillObjective(model, newObjective);
    }
}
