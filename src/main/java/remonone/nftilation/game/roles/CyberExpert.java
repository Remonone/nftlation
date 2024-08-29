package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CyberExpert extends Role {

    private static double SIGHT_SPAN_ANGLE = 20D;

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
            nbt.setString("cyber-expert", "jail");
        });

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
        String isJail = (String) NBT.get(stack, (nbt) -> (String)nbt.getString("cyber-expert"));
        if(isJail == null || !isJail.equals("jail")) return;
        Entity selectedEntity = getEntityLookedAt(player);
        if(selectedEntity == null) {
            player.sendMessage("Entity has not been found!");
            return;
        }
        if(!(selectedEntity instanceof Player)) {
            player.sendMessage("Player was not been found!");
            return;
        }
        
    }

    private Entity getEntityLookedAt(Player player) {
        Vector playerPos = player.getEyeLocation().toVector();
        Vector playerSightDirection = player.getEyeLocation().getDirection();
        List<Entity> entities = player.getNearbyEntities(5, 5, 5);
        for(Entity entity : entities) {
            Vector entityPos = entity.getLocation().toVector();
            Vector normalizedDirection = entityPos.subtract(playerPos).normalize();
            double angle = normalizedDirection.angle(playerSightDirection);
            if(angle < SIGHT_SPAN_ANGLE) return entity;
        }
        return null;
    }
}
