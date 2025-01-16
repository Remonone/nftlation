package remonone.nftilation.game.phase;

import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.events.OnCounterTickEvent;
import remonone.nftilation.events.OnPhaseUpdateEvent;
import remonone.nftilation.game.models.PhaseProps;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PhaseCounter {
    
    private final List<PhaseProps> phases;
    
    @Getter
    private BarWorker barWorker;
    @Getter
    private int phase;
    @Getter
    private int seconds;
    private BukkitRunnable task;
    
    public PhaseCounter() {
        phases = ConfigManager.getInstance().getPhaseProps();
    }
    
    public void init() {
        PhaseProps props = getPhase(0);
        initBar(0, props);
        initTimer(0, 0);
        getServer().getPluginManager().callEvent(new OnPhaseUpdateEvent(0));
    }
    
    public void init(int phase, int delayInSeconds) {
        PhaseProps props = getPhase(phase);
        initBar(phase, props);
        initTimer(phase, delayInSeconds);
    }

    private void initTimer(int phase, int delayInSeconds) {
        final int[] timeInfo = {delayInSeconds, phase};
        PhaseProps props = getPhase(phase);
        task = new BukkitRunnable() {
            @Override
            public void run() {
                timeInfo[0]++;
                if(timeInfo[0] >= props.getLength()) {
                    timeInfo[1]++;
                    timeInfo[0] = 0;
                    getServer().getPluginManager().callEvent(new OnPhaseUpdateEvent(timeInfo[1]));
                }
                setTime(timeInfo);
                getServer().getPluginManager().callEvent(new OnCounterTickEvent(timeInfo[0], timeInfo[1]));
            }
        };
        task.runTaskTimer(Nftilation.getInstance(), 0, 20);
        
    }

    private void setTime(int[] timeInfo) {
        this.seconds = timeInfo[0];
        this.phase = timeInfo[1];
    }

    private void initBar(int phase, PhaseProps props) {
        this.barWorker = new BarWorker();
        this.barWorker.initBar(phase, props.getLength(), props.getBarColor(), props.getBarStyle());
        getServer().getPluginManager().registerEvents(barWorker, Nftilation.getInstance());
    }
    
    public void setPhase(int phase) {
        if(task == null) return;
        getServer().getScheduler().cancelTask(task.getTaskId());
        getServer().getPluginManager().callEvent(new OnPhaseUpdateEvent(phase - 1));
        initTimer(phase - 1, 0);
    }
    
    public void pauseCounter() {
        if(task == null) return;
        Logger.log("Game has been paused!");
        getServer().getScheduler().cancelTask(task.getTaskId());
        RuleManager.getInstance().setRule(RuleConstants.RULE_GAME_IS_RUNNING, false);
    }

    public void resumeCounter() {
        Logger.log("Game has been resumed!");
        RuleManager.getInstance().setRule(RuleConstants.RULE_GAME_IS_RUNNING, true);
        initTimer(phase, seconds);
    }
    
    public PhaseProps getPhase(int phase)  {
        return phases.get(phase);
    }
    
    public void skipPhase() {
        if(task == null) return;
        getServer().getScheduler().cancelTask(task.getTaskId());
        getServer().getPluginManager().callEvent(new OnPhaseUpdateEvent(++phase));
        initTimer(phase, 0);
    }

    public void stop() {
        if(task != null) {
            getServer().getScheduler().cancelTask(task.getTaskId());
        }
        barWorker.stopWorker();
    }
}
