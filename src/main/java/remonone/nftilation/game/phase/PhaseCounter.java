package remonone.nftilation.game.phase;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.events.OnPhaseUpdateEvent;
import remonone.nftilation.game.rules.RuleManager;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PhaseCounter {
    
    public final List<Long> phases = new ArrayList<>();
    
    public BossBar bar;
    public int barTask;
    
    private int phaseCounter = 0;
    private int secondsCounter = 0;
    
    private int taskId = -1;
    
    public PhaseCounter() {
        phases.add((long)15 * DataConstants.TICKS_IN_MINUTE);
        phases.add((long)20 * DataConstants.TICKS_IN_MINUTE);
        phases.add((long)20 * DataConstants.TICKS_IN_MINUTE);
        phases.add((long)30 * DataConstants.TICKS_IN_MINUTE);
        phases.add((long)15 * DataConstants.TICKS_IN_MINUTE);
    }
    
    public void Init() {
        long delay = phases.get(phaseCounter);
        int delaySeconds = (int) (delay / DataConstants.TICKS_IN_MINUTE) * 60;
        bar = Bukkit.createBossBar(getBossBarTitle(phaseCounter + 1, delaySeconds), BarColor.RED, BarStyle.SEGMENTED_10);
        bar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bar::addPlayer);
        getServer().getPluginManager().callEvent(new OnPhaseUpdateEvent(phaseCounter + 1));
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Next();
            }
        };
        task.runTaskLater(Nftilation.getInstance(), delay);
        taskId = task.getTaskId();
        StartCounter(delaySeconds);
    }

    public void PauseCounter() {
        getServer().getScheduler().cancelTask(taskId);
        RuleManager.getInstance().setRule(RuleConstants.RULE_GAME_IS_RUNNING, false);
    }

    public void ResumeCounter() {
        RuleManager.getInstance().setRule(RuleConstants.RULE_GAME_IS_RUNNING, true);
        if(phaseCounter >= phases.size()) return;
        long delay = phases.get(phaseCounter);
        StartCounter((int)delay);
    }

    private void StartCounter(int delaySeconds) {
        BukkitRunnable barTask = new BukkitRunnable() {
            @Override
            public void run() {
                secondsCounter++;
                int totalSeconds = delaySeconds - secondsCounter;
                bar.setTitle(getBossBarTitle(phaseCounter + 1, totalSeconds));
                bar.setProgress((double) totalSeconds / (double) delaySeconds);
            }
        };
        barTask.runTaskTimer(Nftilation.getInstance(), 0, 20);
        this.barTask = barTask.getTaskId();
    }

    public String getBossBarTitle(int phase, int remainingSeconds) {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        return String.format("Phase: " + phase + ". Next phase: " + mins + ":" + String.format("%02d", secs)); 
    }
    
    private void Next() {
        Bukkit.getScheduler().cancelTask(this.barTask);
        secondsCounter = 0;
        if(++phaseCounter >= phases.size()) {
            getServer().getPluginManager().callEvent(new OnPhaseUpdateEvent(phaseCounter + 1));
            return;
        }
        getServer().getPluginManager().callEvent(new OnPhaseUpdateEvent(phaseCounter + 1));
        long delay = phases.get(phaseCounter);
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                Next();
            }
        };
        task.runTaskLater(Nftilation.getInstance(), delay);
        taskId = task.getTaskId();
        int delaySeconds = (int) (delay / DataConstants.TICKS_IN_MINUTE) * 60;
        StartCounter(delaySeconds);
    }
    
    public void SkipPhase() {
        getServer().getScheduler().cancelTask(taskId);
        Next();
    }
}
