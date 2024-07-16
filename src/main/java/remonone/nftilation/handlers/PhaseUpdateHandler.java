package remonone.nftilation.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnPhaseUpdateEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.game.ingame.actions.ActionType;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;

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
                Logger.broadcast("Stage 1 has begun!");
                new BukkitRunnable() {
                    public void run() {
                        ActionContainer.InitAction(ActionType.CHECKER, new HashMap<String, Object>() {{
                            put("global", true);
                        }});
                    }
                }.runTaskLater(Nftilation.getInstance(), 10 * DataConstants.TICKS_IN_MINUTE);
                break;
            }
            case 2: {
                Logger.broadcast("Stage 2 has begun!");
                RuleManager.getInstance().setRule(PropertyConstant.RULE_AVAILABLE_TIER, 2);
                SummonDiamonds();
                break;
            }
            case 3: {
                Logger.broadcast("Stage 3 has begun!");
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_INVULNERABLE, false);
                ActionContainer.InitAction(ActionType.ROBOSYBIL_ATTACK, new HashMap<String, Object>() {{
                    put("global", true);
                }});
                break;
            }
            case 4: {
                Logger.broadcast("Stage 4 has begun!");
                RuleManager.getInstance().setRule(PropertyConstant.RULE_AVAILABLE_TIER, 3);
                ActionContainer.InitAction(ActionType.HAMSTER, new HashMap<>());
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        ActionContainer.InitAction(ActionType.CRYPT_DROP, new HashMap<String, Object>() {{
                            put("global", true);
                        }});
                    }
                };
                runnable.runTaskLater(Nftilation.getInstance(), 10 * DataConstants.TICKS_IN_MINUTE);
                break;
            }
            case 5: {
                Logger.broadcast("Stage 5 has begun!");
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_SELF_DESTRUCTIVE, true);
                RuleManager.getInstance().setRule(PropertyConstant.RULE_INVENTORY_AUTO_CLEAR, false);
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_DAMAGE_INTAKE, 4);
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<TeamData> teams = Store.getInstance().getDataInstance().getTeamData();
                        for(TeamData team : teams) {
                            if(GameInstance.getInstance().isTeamAlive(team.getTeamName())) {
                                GameInstance.getInstance().damageCore(team.getTeamName(), false);
                            }
                        }
                    }
                };
                
                runnable.runTaskTimer(Nftilation.getInstance(), 0, (long) RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_CORE_HEALTH_LOST_PERIOD, 9 * 20));
                taskId = runnable.getTaskId();
                break;
            }
            case 6: {
                RuleManager.getInstance().setRule(PropertyConstant.RULE_IMMINENT_DEATH, true);
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_HEALTH_LOST_PERIOD, DataConstants.TICKS_IN_SECOND);
                Iterator<String> teamIt = GameInstance.getInstance().getTeamIterator();
                while(teamIt.hasNext()) {
                    String team = teamIt.next();
                    if(!GameInstance.getInstance().isTeamAlive(team)) {
                        GameInstance.getInstance().getTeamPlayers(team).forEach(playerModel -> OnPlayerDieHandler.OnDeath(playerModel.getReference()));
                    }
                }
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<TeamData> teams = Store.getInstance().getDataInstance().getTeamData();
                        for(TeamData team : teams) {
                            if(GameInstance.getInstance().isTeamAlive(team.getTeamName())) {
                                GameInstance.getInstance().damageCore(team.getTeamName(), false);
                            }
                        }
                    }
                };

                runnable.runTaskTimer(Nftilation.getInstance(), 0, (int) RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_CORE_HEALTH_LOST_PERIOD, 20));
            }
        }
    }

    private void SummonDiamonds() {
        List<Location> spawnPositions = ConfigManager.getInstance().getDiamondSpawnList();
        spawnPositions.forEach(position -> position.getBlock().setType(Material.DIAMOND_ORE));
    }
}
