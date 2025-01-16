package remonone.nftilation.game.phase;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.events.OnCounterTickEvent;
import remonone.nftilation.events.OnPhaseUpdateEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PhaseProps;

@Getter
public class BarWorker implements Listener {
    
    private BossBar bar;

    @EventHandler
    public void onTick(final OnCounterTickEvent event) {
        PhaseProps props = GameInstance.getInstance().getCounter().getPhase(event.getPhase());
        int currentSecond = props.getLength() - event.getTimeInSeconds();
        bar.setTitle(getBossBarTitle(event.getPhase() + 1, currentSecond));
        bar.setProgress((double) currentSecond / (double) props.getLength());
    }
    
    @EventHandler
    public void onPhase(final OnPhaseUpdateEvent event) {
        PhaseProps props = GameInstance.getInstance().getCounter().getPhase(event.getPhaseStage());
        bar.setColor(props.getBarColor());
        bar.setStyle(props.getBarStyle());
    }
    
    public void initBar(int phase, int phaseLength, BarColor color, BarStyle style) {
        bar = Bukkit.createBossBar(getBossBarTitle(phase + 1, phaseLength), color, style);
        bar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bar::addPlayer);
    }

    private String getBossBarTitle(int phase, int remainingSeconds) {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        return String.format("Phase: " + phase + ". Next phase: " + mins + ":" + String.format("%02d", secs));
    }
    
    public void stopWorker() {
        bar.setVisible(false);
        bar = null;
        OnCounterTickEvent.getHandlerList().unregister(this);
        OnPhaseUpdateEvent.getHandlerList().unregister(this);
    }
}
