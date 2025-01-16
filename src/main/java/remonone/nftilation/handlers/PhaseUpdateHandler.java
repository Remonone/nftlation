package remonone.nftilation.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.events.OnPhaseUpdateEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PhaseProps;
import remonone.nftilation.game.rules.RuleManager;

import java.util.Iterator;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PhaseUpdateHandler implements Listener {
    
    int taskId = -1;
    
    @EventHandler
    public void onPhaseUpdate(final OnPhaseUpdateEvent event) {
        int stage = event.getPhaseStage() + 1;
        PhaseProps props = GameInstance.getInstance().getCounter().getPhase(stage);
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(props.getPhaseTitleColor() + props.getPhaseTitle(), props.getPhaseDescriptionColor() + props.getPhaseDescription(), 10, 80, 10);
            if(props.getMessagesToSendPlayers().isEmpty()) continue;
            sendMessagesToPlayer(player, props.getMessagesToSendPlayers().toArray(new String[0]));
        }
        switch (stage) {
            case 2: {
                RuleManager.getInstance().setRule(RuleConstants.RULE_AVAILABLE_TIER, 2);
                SummonDiamonds();
                break;
            }
            case 3: {
                RuleManager.getInstance().setRule(RuleConstants.RULE_CORE_INVULNERABLE, false);
                RuleManager.getInstance().setRule(RuleConstants.RULE_RESPAWN_TIMER, (long) 10 * DataConstants.TICKS_IN_SECOND);
                break;
            }
            case 4: {
                RuleManager.getInstance().setRule(RuleConstants.RULE_AVAILABLE_TIER, 3);
                RuleManager.getInstance().setRule(RuleConstants.RULE_RESPAWN_TIMER, (long) 15 * DataConstants.TICKS_IN_SECOND);
                break;
            }
            case 5: {
                RuleManager instance = RuleManager.getInstance();
                instance.setRule(RuleConstants.RULE_CORE_SELF_DESTRUCTIVE, true);
                instance.setRule(RuleConstants.RULE_INVENTORY_AUTO_CLEAR, false);
                instance.setRule(RuleConstants.RULE_CORE_DAMAGE_INTAKE, 4);
                instance.setRule(RuleConstants.RULE_RESPAWN_TIMER, (long)20 * DataConstants.TICKS_IN_SECOND);
                startCoreLooseEvent();
                break;
            }
            case 6: {
                RuleManager.getInstance().setRule(RuleConstants.RULE_IMMINENT_DEATH, true);
                RuleManager.getInstance().setRule(RuleConstants.RULE_CORE_HEALTH_LOST_PERIOD, DataConstants.TICKS_IN_SECOND);
                Iterator<ITeam> teamIt = GameInstance.getInstance().getTeamIterator();
                while(teamIt.hasNext()) {
                    String teamName = teamIt.next().getTeamName();
                    if(!GameInstance.getInstance().getTeam(teamName).isCoreAlive()) {
                        GameInstance.getInstance().getTeam(teamName).getPlayers().forEach(playerModel -> OnEntityDieHandler.OnDeath(playerModel.getReference()));
                    }
                }
                startCoreLooseEvent();
            }
            default: {}
        }
    }
    
    public void startCoreLooseEvent() {
        if(taskId != -1) getServer().getScheduler().cancelTask(taskId);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if(!(Boolean)RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_GAME_IS_RUNNING, true)) return;
                Iterator<ITeam> teamIterator = GameInstance.getInstance().getTeamIterator();
                while(teamIterator.hasNext()) {
                    ITeam team = teamIterator.next();
                    if(GameInstance.getInstance().getTeam(team.getTeamName()).isCoreAlive()) {
                        GameInstance.getInstance().damageCore(team.getTeamName(), false);
                    }
                }
            }
        };
        runnable.runTaskTimer(Nftilation.getInstance(), 0, (int) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_CORE_HEALTH_LOST_PERIOD, 20));
        taskId = runnable.getTaskId();
    }

    private void SummonDiamonds() {
        List<Location> spawnPositions = ConfigManager.getInstance().getDiamondSpawnList();
        spawnPositions.forEach(position -> position.getBlock().setType(Material.DIAMOND_ORE));
    }

    public void sendMessagesToPlayer(Player player, String... messages) {
        player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.LINE_SEPARATOR);
        for(String message : messages) {
            player.sendMessage(MessageConstant.LINE_STARTED + message);
        }
    }
}
