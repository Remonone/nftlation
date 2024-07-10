package remonone.nftilation.utils;

import lombok.AllArgsConstructor;

import java.util.Timer;
import java.util.TimerTask;

public class TimerUtils {

    public static LocalTimer SetTimerTask(final TimerTask task, long delay, long period) {
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, delay, period);
        return new LocalTimer(timer);
    }

    public static LocalTimer SetTimerTask(final TimerTask task, long delay) {
        final Timer timer = new Timer();
        timer.schedule(task, delay);
        return new LocalTimer(timer);
    }
    
    public static void CancelTimer(final LocalTimer localTimer) {
        localTimer.timer.cancel();
    }
    
    @AllArgsConstructor
    public static class LocalTimer {
        private Timer timer;
    }
}
