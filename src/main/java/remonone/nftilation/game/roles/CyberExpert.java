package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.ingame.objects.TrapCircle;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.BoundingBox;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.RayTrace;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CyberExpert extends Role {


    @Override
    public String getRoleID() {
        return "CE";
    }

    public CyberExpert() {
        super("CE");
    }

    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> playerParams) {
        ItemStack jail = new ItemStack(Material.IRON_FENCE);
        ItemMeta meta = jail.getItemMeta();
        meta.setDisplayName("Jail");
        jail.setItemMeta(meta);
        NBT.modify(jail, (nbt) -> {
            nbt.setString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER, "jail");
        });
        ItemStack pistol = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta teleportMeta = pistol.getItemMeta();
        teleportMeta.setDisplayName(ChatColor.DARK_PURPLE + "Пушка");
        pistol.setItemMeta(teleportMeta);
        NBT.modify(pistol, (nbt) -> {nbt.setString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER, "shot");});

        return Collections.singletonList(jail);
    }

    @EventHandler
    public void onItemInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) return;
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        Role role = getRoleByID((String)model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID));
        if(!(role instanceof CyberExpert)) return;
        ItemStack stack = event.getItem();
        String isJail = NBT.get(stack, (nbt) -> (String)nbt.getString("cyber-expert"));
        if(isJail == null || !isJail.equals("jail")) return;
        event.setCancelled(true);
        Entity selectedEntity = getEntityLookedAt(player);
        if(selectedEntity == null) {
            player.sendMessage("Entity has not been found!");
            return;
        }
        if(!(selectedEntity instanceof Player)) {
            player.sendMessage("Player was not been found!");
            return;
        }
        Player target = (Player)selectedEntity;
//        player.sendMessage(selectedEntity.getName());
        TrapCircle trapCircle = new TrapCircle(target, player, 10 * DataConstants.TICKS_IN_SECOND, player.getWorld());
    }

    private Entity getEntityLookedAt(Player player) {
        List<Entity> entities = player.getNearbyEntities(5, 5, 5);
        
        RayTrace ray = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection());
        for(Entity entity : entities) {
            BoundingBox box = new BoundingBox(entity);
            if(ray.intersects(box, 6, .1D)) return entity;
        }
        return null;
    }
}
