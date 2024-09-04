package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.effects.LineEffect;
import remonone.nftilation.effects.props.LineProps;
import remonone.nftilation.game.ingame.objects.TrapCircle;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.*;

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
        ItemMeta pistolMeta = pistol.getItemMeta();
        pistolMeta.setDisplayName(ChatColor.DARK_PURPLE + "Пистолет Макарова");
        pistol.setItemMeta(pistolMeta);
        NBT.modify(pistol, (nbt) -> {nbt.setString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER, "shot");});
        ItemStack teleport = new ItemStack(Material.EYE_OF_ENDER);
        NBT.modify(teleport, (nbt) -> {nbt.setString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER, "teleport");});
        ItemMeta teleportMeta = teleport.getItemMeta();
        teleportMeta.setDisplayName(ChatColor.GOLD + "Скачок");
        teleport.setItemMeta(teleportMeta);
        return Arrays.asList(jail, pistol, teleport);
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
        if(stack == null || stack.getType() == Material.AIR || stack.getAmount() < 1) return;
        String usedItem = NBT.get(stack, (nbt) -> (String)nbt.getString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER));
        Logger.debug(usedItem);
        if(StringUtils.isBlank(usedItem)) return;
        event.setCancelled(true);
        int upgradeLevel = (int)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        if(usedItem.equals("jail")) {
            jailTarget(stack, player, upgradeLevel);
            return;
        }
        if(usedItem.equals("shot")) {
            shotArrow(stack, player, upgradeLevel);
            return;
        }
        if(usedItem.equals("teleport")) {
            teleportWithin(stack, player, upgradeLevel);
        }
    }

    private void teleportWithin(ItemStack stack, Player player, int level) {
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(player, stack);
            return;
        }
        Location positionToTeleport = getBlockLookedAt(player, 10).add(new Vector(.5F, 0, .5F));
        LineProps props = LineProps.builder()
                .world(player.getWorld())
                .from(player.getEyeLocation().toVector())
                .to(positionToTeleport.toVector())
                .particle(Particle.REDSTONE)
                .step(.1D)
                .count(0)
                .build();
        props.setCustomOffset(RGBConstants.purple);
        new LineEffect().execute(props);
        player.teleport(positionToTeleport);
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        InventoryUtils.setCooldownForItem(model, stack, 30);
    }

    private void jailTarget(ItemStack stack, Player player, int level) {
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(player, stack);
            return;
        }
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
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        InventoryUtils.setCooldownForItem(model, stack, 120);
    }

    private void shotArrow(ItemStack stack, Player user, int level) {
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(user, stack);
            return;
        }
        Arrow arrow = user.launchProjectile(Arrow.class);
        arrow.setVelocity(arrow.getVelocity().multiply(1.3D));
        PlayerModel model = PlayerUtils.getModelFromPlayer(user);
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

    private Location getBlockLookedAt(Player player, int range) {
        BlockIterator iterator = new BlockIterator(player.getWorld(), player.getEyeLocation().toVector(), player.getEyeLocation().getDirection(), 0, range);
        Block lastAirBlock = player.getEyeLocation().getBlock();
        while(iterator.hasNext()) {
            Block block = iterator.next();
            if(!block.getType().equals(Material.AIR)) {
                break;
            }
            lastAirBlock = block;
        }
        return lastAirBlock.getLocation();
    }
}
