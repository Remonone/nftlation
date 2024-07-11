package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import remonone.nftilation.Nftilation;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.game.ingame.actions.IAction;

import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class Hamster implements IAction, Listener {
    @Override
    public void Init(Map<String, Object> params) {
        Location location = ConfigManager.getInstance().getCenterLocation();
        Zombie zombie = location.getWorld().spawn(location, Zombie.class);
        zombie.setCustomName(ChatColor.BLUE + "" + ChatColor.BOLD + "Hamster");
        zombie.setCustomNameVisible(true);
        zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0D);
        zombie.setHealth(20.0D);
        getServer().getPluginManager().registerEvents(this, Nftilation.getInstance());
    }
    
    @EventHandler
    public void OnHamsterDamage(EntityDamageByEntityEvent e) {
        if(!(e.getEntity() instanceof Zombie)) return;
        Zombie zombie = (Zombie) e.getEntity();
        if(!zombie.getCustomName().equals(ChatColor.BLUE + "" + ChatColor.BOLD + "Hamster")) return;
        if(!(e.getDamager() instanceof Player)) {
            e.setCancelled(true);
            return;
        }
        if(zombie.getHealth() - e.getFinalDamage() <= 0.0D) {
            for(int i = 0; i < 4; i++) {
                ItemStack stack = new Dye(DyeColor.BLUE).toItemStack(64);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName("Шу-шу бана!");
                stack.setItemMeta(meta);
                zombie.getWorld().dropItemNaturally(zombie.getLocation(), stack);
            }
        }
    }

    @Override
    public String getTitle() {
        return "Hamster Combat";
    }

    @Override
    public String getDescription() {
        return "Приходите потапать в центр, скоро листинг";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_SHEEP_AMBIENT;
    }
}
