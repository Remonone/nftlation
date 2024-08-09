package remonone.nftilation.utils.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
@AllArgsConstructor
public class TaskContainer {
    private BukkitRunnable runnable;
    private long taskStartTime;
    private int ticksBeforeStart;
    @Setter
    private int ticksAfterStart;
}
