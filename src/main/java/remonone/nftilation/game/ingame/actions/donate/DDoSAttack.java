package remonone.nftilation.game.ingame.actions.donate;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.game.models.PlayerModel;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class DDoSAttack implements IAction {
    
    public static Random random = new Random();
    
    @Override
    public void Init(Map<String, Object> params) {
        if(!params.containsKey(PropertyConstant.ACTION_TEAM)) {
            throw new NullPointerException("Couldn't initiate DDoSAttack action. Team is missing!");
        }
        String teamName = (String) params.get(PropertyConstant.ACTION_TEAM);
        if(teamName == null) throw new NullPointerException("Team name is empty");
        List<PlayerModel> models = GameInstance.getInstance().getTeam(teamName).getPlayers();
        if(models.isEmpty()) throw new NullPointerException("Wrong team name");
        BukkitRunnable runnable = new BukkitRunnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                PlayerModel model = models.get(random.nextInt(models.size()));
                Player target = model.getReference();
                Location loc = target.getLocation();
                target.getLocation().getWorld().strikeLightningEffect(loc);
                EntityDamageEvent e = new EntityDamageEvent(target, EntityDamageEvent.DamageCause.LIGHTNING, 3);
                getServer().getPluginManager().callEvent(e);
                if(!e.isCancelled()) {
                    target.setHealth(target.getHealth() - e.getFinalDamage());
                }
            }
        };
        runnable.runTaskTimer(Nftilation.getInstance(), 0, 2 * DataConstants.TICKS_IN_SECOND);
        int taskId = runnable.getTaskId();
        new BukkitRunnable() {
            @Override
            public void run() {
                getServer().getScheduler().cancelTask(taskId);
            }
        }.runTaskLater(Nftilation.getInstance(), DataConstants.TICKS_IN_MINUTE);
    }

    @Override
    public String getTitle() {
        return "DDoS Атака";
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
