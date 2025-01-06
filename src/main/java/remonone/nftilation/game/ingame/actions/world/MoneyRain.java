package remonone.nftilation.game.ingame.actions.world;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.VectorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class MoneyRain implements IAction {
    @Override
    public void Init(Map<String, Object> params) {
        Logger.log("Starting an " + getClass().getSimpleName() + " event...");
        List<Location> positions = new ArrayList<>();
        GameInstance.getInstance().getTeamIterator().forEachRemaining(team -> positions.add(team.getTeamSpawnPoint().getCheckerChestPosition().add(0, 40, 0)));
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location position : positions) {
                    Vector newPos = VectorUtils.getRandomPosInCircle(position.toVector(), 15);
                    World world = position.getWorld();
                    ItemStack stack = new ItemStack(Material.GOLD_NUGGET);
                    NBT.modify(stack, (nbt) -> {
                        nbt.setString("event", "moneydrop");
                        nbt.setFloat("money-rain", 2.0F);
                    });
                    Location newLoc = new Location(world, newPos.getX(), newPos.getY(), newPos.getZ());
                    world.dropItem(newLoc, stack);
                }
            }
        };
        runnable.runTaskTimer(Nftilation.getInstance(), 3 * DataConstants.TICKS_IN_SECOND, 5);
        int id = runnable.getTaskId();
        new BukkitRunnable() {
            @Override
            public void run() {
                getServer().getScheduler().cancelTask(id);
            }
        }.runTaskLater(Nftilation.getInstance(), 3 * DataConstants.TICKS_IN_MINUTE);
    }

    @Override
    public String getTitle() {
        return "Денежный дождь";
    }

    @Override
    public String getDescription() {
        return "Это денежный дождь, аллилуйя, дождь из денег";
    }

    @Override
    public Sound getSound() {
        return Sound.WEATHER_RAIN;
    }
}
