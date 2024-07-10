package remonone.nftilation.game.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.roles.Role;

import java.util.Iterator;

public class ScoreboardHandler {
    
    public static void buildScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Nftlation", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(data.getTeam().getTeamName(), player);
        fillObjective(model, objective);
        player.setScoreboard(scoreboard);
    }

    private static void fillObjective(GameInstance.PlayerModel model, Objective objective) {
        GameInstance instance = GameInstance.getInstance();
        int counter = 0;
        objective.getScore("Level: " + model.getUpgradeLevel()).setScore(++counter);
        objective.getScore("Tokens: " + model.getTokens()).setScore(++counter);
        objective.getScore("Role: " + Role.getRoleByID(model.getRoleId()).getRoleName()).setScore(++counter);
        String data = Store.getInstance().getDataInstance().FindPlayerByName(model.getReference().getName()).getTeam().getTeamName();
        objective.getScore("Core Health: " + instance.getCoreHealth(data)).setScore(++counter);
        objective.getScore(ChatColor.RED + "" + ChatColor.BOLD + "= Info =").setScore(++counter);
        objective.getScore(ChatColor.RED + "").setScore(++counter);
        objective.getScore("Deaths: " + model.getDeathCounter()).setScore(++counter);
        objective.getScore("Kills: " + model.getKillCounter()).setScore(++counter);
        objective.getScore(ChatColor.RED + "" + ChatColor.BOLD + "= Stats =").setScore(++counter);
        objective.getScore(ChatColor.BLUE + "").setScore(++counter);
        Iterator<String> it = instance.getTeamIterator();
        while(it.hasNext()) {
            String teamName = it.next();
            String scoreName = teamName + "[";
            if(instance.isTeamAlive(teamName)) {
                scoreName += ChatColor.GREEN + "" + ChatColor.BOLD + "✓";
            } else if(!instance.isTeamActive(teamName)) {
                scoreName += ChatColor.DARK_RED + "" + ChatColor.BOLD +  "x";
            } else {
                scoreName += ChatColor.DARK_RED + "" + ChatColor.BOLD +  instance.getTeamPlayersAlive(teamName);
            }
            scoreName += ChatColor.RESET + "]";
            objective.getScore(scoreName).setScore(++counter);
        }
        objective.getScore(ChatColor.GOLD + "= Teams =").setScore(++counter);
    }

    public static void updateScoreboard(GameInstance.PlayerModel player) {
        Scoreboard scoreboard = player.getReference().getScoreboard();
        if(scoreboard.getObjective("Nftlation") != null) {
            scoreboard.getObjective("Nftlation").unregister(); 
        }
        Objective newObjective = scoreboard.registerNewObjective("Nftlation", "dummy");
        newObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        fillObjective(player, newObjective);
    }
    
    // TODO: Build scoreboard(based on player)
    // TODO: Update scoreboard(provide player)
    // TODO: Unregister scoreboard
}
