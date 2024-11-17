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
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.events.OnCounterPauseEvent;
import remonone.nftilation.events.OnPhaseUpdateEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.game.ingame.actions.ActionType;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.tasks.TaskCache;
import remonone.nftilation.utils.tasks.TaskContainer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class PhaseUpdateHandler implements Listener {
    
    int taskId = -1;

    public final TaskCache cache = new TaskCache();
    
    @EventHandler
    public void onPhaseUpdate(final OnPhaseUpdateEvent event) {
        int stage = event.getPhaseStage();
        cache.clear();
        initiateStageActions(stage);
        switch (stage) {
            case 1: {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.FIRST_PHASE_TITLE, MessageConstant.FIRST_PHASE_SUBTITLE, 10, 80, 10);
                    sendMessagesToPlayer(player,
                            MessageConstant.FIRST_PHASE_DESCRIPTION_1,
                            MessageConstant.FIRST_PHASE_DESCRIPTION_2);
                }
                break;
            }
            case 2: {
                RuleManager.getInstance().setRule(RuleConstants.RULE_AVAILABLE_TIER, 2);
                SummonDiamonds();
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.SECOND_PHASE_TITLE, MessageConstant.SECOND_PHASE_SUBTITLE, 10, 80, 10);
                    sendMessagesToPlayer(player,
                            MessageConstant.SECOND_PHASE_DESCRIPTION_1,
                            MessageConstant.SECOND_PHASE_DESCRIPTION_2,
                            MessageConstant.SECOND_PHASE_DESCRIPTION_3);
                }
                break;
            }
            case 3: {
                RuleManager.getInstance().setRule(RuleConstants.RULE_CORE_INVULNERABLE, false);
                RuleManager.getInstance().setRule(RuleConstants.RULE_RESPAWN_TIMER, (long) 10 * DataConstants.TICKS_IN_SECOND);
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.THIRD_PHASE_TITLE, MessageConstant.THIRD_PHASE_SUBTITLE, 10, 80, 10);
                    sendMessagesToPlayer(player,
                            MessageConstant.THIRD_PHASE_DESCRIPTION_1,
                            MessageConstant.THIRD_PHASE_DESCRIPTION_2);
                }
                break;
            }
            case 4: {
                RuleManager.getInstance().setRule(RuleConstants.RULE_AVAILABLE_TIER, 3);
                RuleManager.getInstance().setRule(RuleConstants.RULE_RESPAWN_TIMER, (long) 15 * DataConstants.TICKS_IN_SECOND);
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(MessageConstant.FOURTH_PHASE_TITLE, MessageConstant.FOURTH_PHASE_SUBTITLE, 10, 80, 10);
                    sendMessagesToPlayer(player,
                            MessageConstant.FOURTH_PHASE_DESCRIPTION_1,
                            MessageConstant.FOURTH_PHASE_DESCRIPTION_2);
                }
                break;
            }
            case 5: {
                RuleManager instance = RuleManager.getInstance();
                instance.setRule(RuleConstants.RULE_CORE_SELF_DESTRUCTIVE, true);
                instance.setRule(RuleConstants.RULE_INVENTORY_AUTO_CLEAR, false);
                instance.setRule(RuleConstants.RULE_CORE_DAMAGE_INTAKE, 4);
                instance.setRule(RuleConstants.RULE_RESPAWN_TIMER, (long)20 * DataConstants.TICKS_IN_SECOND);
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(!(Boolean)instance.getRuleOrDefault(RuleConstants.RULE_GAME_IS_RUNNING, true)) return;
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
                    sendMessagesToPlayer(player,
                            MessageConstant.FIFTH_PHASE_DESCRIPTION_1,
                            MessageConstant.FIFTH_PHASE_DESCRIPTION_2,
                            MessageConstant.FIFTH_PHASE_DESCRIPTION_3,
                            MessageConstant.FIFTH_PHASE_DESCRIPTION_4);
                }
                runnable.runTaskTimer(Nftilation.getInstance(), 0, (long) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_CORE_HEALTH_LOST_PERIOD, 9 * 20));
                taskId = runnable.getTaskId();
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
                runnable.runTaskTimer(Nftilation.getInstance(), 0, (int) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_CORE_HEALTH_LOST_PERIOD, 20));
            }
        }
    }

    private void initiateStageActions(int stage) {
        List<MetaConfig.GlobalEvent> eventsToInitiate = MetaConfig.getInstance().getGlobalEvents().get(stage);
        if(eventsToInitiate == null) return;
        for(MetaConfig.GlobalEvent event : eventsToInitiate) {
            try {
                ActionType type = ActionType.valueOf(event.getName());
                int delay = event.getDelay() * DataConstants.TICKS_IN_SECOND;
                BukkitRunnable runnable = getInitiatedAction(type, event.getAdditionalParams(), delay);
                cache.add(new TaskContainer(runnable, System.currentTimeMillis(), delay, 0));
            } catch(IllegalArgumentException ex) {
                Logger.error("Event name is not defined! Skipping...");
            }
        }
    }

    private void SummonDiamonds() {
        List<Location> spawnPositions = ConfigManager.getInstance().getDiamondSpawnList();
        spawnPositions.forEach(position -> position.getBlock().setType(Material.DIAMOND_ORE));
    }

    @EventHandler
    public void onCounterPause(final OnCounterPauseEvent e) {
        List<TaskContainer> tasks = cache.getContainers();
        if(e.isStopped()) {
            tasks.forEach(task -> {
                int ticksAfterStart = getRemainingSeconds(task);
                if(ticksAfterStart < 0) {
                    cache.removeTask(task);
                    return;
                }
                getServer().getScheduler().cancelTask(task.getRunnable().getTaskId());
                task.setTicksAfterStart(ticksAfterStart);
            });
        } else {
            tasks.forEach(task -> task.getRunnable().runTaskLater(Nftilation.getInstance(), task.getTicksBeforeStart() - task.getTicksAfterStart()));
        }
    }

    private int getRemainingSeconds(TaskContainer task) {
        long currentTime = System.currentTimeMillis();
        long taskTime = task.getTaskStartTime();
        long diff = currentTime - taskTime;
        int diffInTicks = (int)(diff / DataConstants.ONE_SECOND) * DataConstants.TICKS_IN_SECOND;
        if(diffInTicks > task.getTicksBeforeStart()) return -1;
        return diffInTicks;
    }

    public void sendMessagesToPlayer(Player player, String... messages) {
        player.sendMessage(MessageConstant.LINE_STARTED + MessageConstant.LINE_SEPARATOR);
        for(String message : messages) {
            player.sendMessage(MessageConstant.LINE_STARTED + message);
        }
    }
    
    private BukkitRunnable getInitiatedAction(ActionType type, Map<String, Object> params, long delay) {
        BukkitRunnable runnable =  new BukkitRunnable() {
            @Override
            public void run() {
                ActionContainer.InitAction(type, params);
            }
        };
        runnable.runTaskLater(Nftilation.getInstance(), delay);
        return runnable;
    }
}
