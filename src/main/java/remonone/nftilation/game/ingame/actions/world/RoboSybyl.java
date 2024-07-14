package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.ChunkUtils;
import remonone.nftilation.utils.EntityList;

import java.util.List;
import java.util.Map;

public class RoboSybyl implements IAction {
    @Override
    public void Init(Map<String, Object> params) {
        List<Location> spawnPoints = ConfigManager.getInstance().getRoboSybylsSpawnList();
        spawnPoints.forEach(spawnPoint -> {
            ChunkUtils.loadChunkForTime(spawnPoint, 10);
            for(int i = 0; i < DataConstants.ACTION_ROBO_SYBYL_COUNT_PER_POINT; i++) {
                Zombie zombie = spawnPoint.getWorld().spawn(spawnPoint, Zombie.class);
                zombie.setBaby(true);
                zombie.setCustomName(ChatColor.MAGIC + "" + spawnPoint.getBlockX() + spawnPoint.getBlockZ() + "RoboSybyl" + i);
                zombie.setCustomNameVisible(false);
                SetSybylAttackerItems(zombie);
                EntityHandleComponent.setEntityHostile(zombie);
                EntityHandleComponent.setEntityUnloadLocked(zombie);
                EntityList.addEntity(zombie);
            }
        });
    }

    private void SetSybylAttackerItems(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        ItemStack helmet = new ItemStack(Material.IRON_HELMET);
        SetItemMeta(helmet, "sybyl_helmet");
        equipment.setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        SetItemMeta(chestplate, "sybyl_chestplate");
        equipment.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
        SetItemMeta(leggings, "sybyl_leggings");
        equipment.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.IRON_BOOTS);
        SetItemMeta(boots, "sybyl_boots");
        equipment.setBoots(boots);

        ItemStack weapon = new ItemStack(Material.IRON_AXE);
        SetItemMeta(weapon, "sybyl_weapon");
        equipment.setItemInMainHand(weapon);
    }

    private void SetItemMeta(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
    }

    @Override
    public String getTitle() {
        return "Робо-сибил атака";
    }

    @Override
    public String getDescription() {
        return "Захотели пофармить алмазы? Не волнуйтесь, тут таких, как вы, вагоны";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_ELDER_GUARDIAN_AMBIENT;
    }
}
