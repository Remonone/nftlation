package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import remonone.nftilation.Nftilation;
import remonone.nftilation.components.EntityHandleComponent;
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
        EntityHandleComponent.setEntityHostile(zombie);
        GiveArmorToBoss(zombie);
        getServer().getPluginManager().registerEvents(this, Nftilation.getInstance());
    }

    private void GiveArmorToBoss(LivingEntity boss) {
        EntityEquipment equipment = boss.getEquipment();
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        SetItemMeta(helmet, "hamster_helmet");
        equipment.setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        SetItemMeta(chestplate, "hamster_chestplate");
        equipment.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        SetItemMeta(leggings, "hamster_leggings");
        equipment.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.LEATHER_HELMET);
        SetItemMeta(boots, "hamster_boots");
        equipment.setBoots(boots);
    }

    private void SetItemMeta(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void OnHamsterDamage(EntityDamageByEntityEvent e) {
        if(!(e.getEntity() instanceof Zombie)) return;
        Zombie zombie = (Zombie) e.getEntity();
        if(!zombie.getCustomName().equals(ChatColor.BLUE + "" + ChatColor.BOLD + "Hamster")) return;
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
