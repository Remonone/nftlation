package remonone.nftilation.game.ingame.actions.donate;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.game.ingame.actions.IAction;

import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class AirStrike implements IAction {
    
    private static final Random random = new Random();
    
    @Override
    public void Init(Map<String, Object> params) {
        double x = (Double) params.get("x");
        double z = (Double) params.get("z");
        double y = 130;
        Vector position = new Vector(x,y,z);
        StartAirstrike(position);
    }

    private void StartAirstrike(Vector pos) {
        World world = Store.getInstance().getDataInstance().getMainWorld();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Vector randomPos = getRandomPosInCircle(pos, 15);
                world.spawn(new Location(world, randomPos.getX(), randomPos.getY(), randomPos.getZ()), TNTPrimed.class);
            }
        };
        runnable.runTaskTimer(Nftilation.getInstance(), 0, 3);
        int id = runnable.getTaskId();
        new BukkitRunnable() {
            @Override
            public void run() {
                getServer().getScheduler().cancelTask(id);
            }
        }.runTaskLater(Nftilation.getInstance(), 15 * DataConstants.TICKS_IN_SECOND);
    }
    
    private Vector getRandomPosInCircle(Vector vec, float range) {
        double x = vec.getX() + random.nextFloat() * range * (random .nextBoolean() ? -1 : 1);
        double z = vec.getZ() + random.nextFloat() * range * (random .nextBoolean() ? -1 : 1);
        return new Vector(x, vec.getY(), z);
    }

    @Override
    public String getTitle() {
        return "Воздушная бомбардировка";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Sound getSound() {
        return null;
    }
}
