package remonone.nftilation.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnPhaseUpdateEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.game.ingame.actions.ActionType;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.rules.RuleManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PhaseUpdateHandler implements Listener {
    
    int taskId = -1;
    
    @EventHandler
    public void onPhaseUpdate(final OnPhaseUpdateEvent event) {
        int stage = event.getPhaseStage();
        
        switch (stage) {
            case 1: {
                new BukkitRunnable() {
                    public void run() {
                        ActionContainer.InitAction(ActionType.CHECKER, new HashMap<>());
                    }
                }.runTaskLater(Nftilation.getInstance(), 10 * DataConstants.TICKS_IN_MINUTE);
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.FIRST_PHASE_TITLE, MessageConstant.FIRST_PHASE_SUBTITLE, 10, 80, 10);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.LINE_SEPARATOR);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.FIRST_PHASE_DESCRIPTION_1);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.FIRST_PHASE_DESCRIPTION_2);
                }
                break;
            }
            case 2: {
                RuleManager.getInstance().setRule(PropertyConstant.RULE_AVAILABLE_TIER, 2);
                SummonDiamonds();
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.SECOND_PHASE_TITLE, MessageConstant.SECOND_PHASE_SUBTITLE, 10, 80, 10);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.LINE_SEPARATOR);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.SECOND_PHASE_DESCRIPTION_1);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.SECOND_PHASE_DESCRIPTION_2);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.SECOND_PHASE_DESCRIPTION_3);
                }
                break;
            }
            case 3: {
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_INVULNERABLE, false);
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.THIRD_PHASE_TITLE, MessageConstant.THIRD_PHASE_SUBTITLE, 10, 80, 10);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.LINE_SEPARATOR);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.THIRD_PHASE_DESCRIPTION_1);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.THIRD_PHASE_DESCRIPTION_2);
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ActionContainer.InitAction(ActionType.ROBOSYBIL_ATTACK, new HashMap<>());
                    }
                }.runTaskLater(Nftilation.getInstance(), 105);
                break;
            }
            case 4: {
                RuleManager.getInstance().setRule(PropertyConstant.RULE_AVAILABLE_TIER, 3);
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.FOURTH_PHASE_TITLE, MessageConstant.FOURTH_PHASE_SUBTITLE, 10, 80, 10);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.LINE_SEPARATOR);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.FOURTH_PHASE_DESCRIPTION_1);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.FOURTH_PHASE_DESCRIPTION_2);
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ActionContainer.InitAction(ActionType.HAMSTER, new HashMap<>());
                    }
                }.runTaskLater(Nftilation.getInstance(), 105);
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        ActionContainer.InitAction(ActionType.CRYPT_DROP, new HashMap<>());
                    }
                };
                runnable.runTaskLater(Nftilation.getInstance(), 10 * DataConstants.TICKS_IN_MINUTE);
                break;
            }
            case 5: {
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_SELF_DESTRUCTIVE, true);
                RuleManager.getInstance().setRule(PropertyConstant.RULE_INVENTORY_AUTO_CLEAR, false);
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_DAMAGE_INTAKE, 4);
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Iterator<ITeam> teamIterator = GameInstance.getInstance().getTeamIterator();
                        while(teamIterator.hasNext()) {
                            ITeam team = teamIterator.next();
                            if(GameInstance.getInstance().getTeam(team.getTeamName()).isCoreAlive()) {
                                GameInstance.getInstance().damageCore(team.getTeamName(), false);
                            }
                        }
                    }
                };
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.FIFTH_PHASE_TITLE, MessageConstant.FIFTH_PHASE_SUBTITLE, 10, 80, 10);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.LINE_SEPARATOR);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.FIFTH_PHASE_DESCRIPTION_1);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.FIFTH_PHASE_DESCRIPTION_2);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.FIFTH_PHASE_DESCRIPTION_3);
                    player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.FIFTH_PHASE_DESCRIPTION_4);
                }
                runnable.runTaskTimer(Nftilation.getInstance(), 0, (long) RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_CORE_HEALTH_LOST_PERIOD, 9 * 20));
                taskId = runnable.getTaskId();
                break;
            }
            case 6: {
                RuleManager.getInstance().setRule(PropertyConstant.RULE_IMMINENT_DEATH, true);
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_HEALTH_LOST_PERIOD, DataConstants.TICKS_IN_SECOND);
                Iterator<ITeam> teamIt = GameInstance.getInstance().getTeamIterator();
                while(teamIt.hasNext()) {
                    String teamName = teamIt.next().getTeamName();
                    if(!GameInstance.getInstance().getTeam(teamName).isCoreAlive()) {
                        GameInstance.getInstance().getTeam(teamName).getPlayers().forEach(playerModel -> OnEntityDieHandler.OnDeath(playerModel.getReference()));
                    }
                }
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<TeamData> teams = Store.getInstance().getDataInstance().getTeamData();
                        GameInstance instance = GameInstance.getInstance();
                        for(TeamData team : teams) {
                            if(instance.getTeam(team.getTeamName()).isCoreAlive()) {
                                instance.damageCore(team.getTeamName(), false);
                            }
                        }
                    }
                };
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.SIXTH_PHASE_TITLE, MessageConstant.SIXTH_PHASE_SUBTITLE, 10, 80, 10);
                }
                runnable.runTaskTimer(Nftilation.getInstance(), 0, (int) RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_CORE_HEALTH_LOST_PERIOD, 20));
            }
        }
    }

    private void SummonDiamonds() {
        List<Location> spawnPositions = ConfigManager.getInstance().getDiamondSpawnList();
        spawnPositions.forEach(position -> position.getBlock().setType(Material.DIAMOND_ORE));
    }
}
