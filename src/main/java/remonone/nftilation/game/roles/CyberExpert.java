package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.objects.TrapCircle;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.BoundingBox;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.RayTrace;

import java.util.Arrays;
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

        return Arrays.asList(jail, pistol);
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
        String isJail = NBT.get(stack, (nbt) -> (String)nbt.getString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER));
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
        new TrapCircle(target, player, 10 * DataConstants.TICKS_IN_SECOND, player.getWorld());
    }

    @EventHandler
    public void onItemUse(final PlayerInteractEvent e) {
        Player user = e.getPlayer();
        Role role = Store.getInstance().getDataInstance().getPlayerRole(user.getUniqueId());
        if(!(role instanceof CyberExpert)) return;
        ItemStack stack = e.getItem();
        if(stack == null || stack.getAmount() < 1 || stack.getType().equals(Material.AIR)) return;
        String pistol = NBT.get(stack, (nbt) -> (String)nbt.getString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER));
        if(!StringUtils.isEmpty(pistol) && pistol.equals("shot")) {
            shotArrow(stack, user);
            return;
        }
    }

    private void shotArrow(ItemStack stack, Player user) {
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(user, stack);
            return;
        }
        Arrow arrow = user.launchProjectile(Arrow.class);
        arrow.setVelocity(arrow.getVelocity().multiply(1.3D));
        String team = Store.getInstance().getDataInstance().getPlayerTeam(user.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, user);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        long cooldown = 10 - upgradeLevel * 2L;
        InventoryUtils.setCooldownForItem(model, stack, cooldown);
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
