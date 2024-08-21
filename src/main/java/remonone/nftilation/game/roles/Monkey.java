package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.damage.MonkeyWandDamage;
import remonone.nftilation.game.models.IDamageInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Monkey extends Role {
    
    public Monkey() {
        super("MN");
    }
    
    @Override
    public String getRoleID() {
        return "MN";
    }

    @Override
    protected List<ItemStack> getAbilityItems(Map<String, Object> params){
        ItemStack itemStack = new ItemStack(Material.INK_SACK);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.MONKEY_ABILITY_ITEM);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {nbt.setString(RoleConstant.MONKEY_NBT_CONTAINER, RoleConstant.MONKEY_NBT_INVISIBILITY);});
        return Collections.singletonList(itemStack);
    }
    
    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        super.setPlayer(player, params);
        player.setAllowFlight(true);
    }

    @EventHandler
    public void onPlayerFlight(final PlayerToggleFlightEvent e) {
        Player accessor = e.getPlayer();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(accessor.getUniqueId());
        if(!(role instanceof Monkey)) {
            return;
        }
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) {
            return;
        }
        PlayerModel model = PlayerUtils.getModelFromPlayer(accessor);
        Map<String, Object> params = model.getParameters();
        int level = (int)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int jumpAmount = (Integer)getMetaInfo(MetaConstants.META_MONKEY_JUMP_COUNT, level);
        if(params.containsKey(RoleConstant.MONKEY_JUMP_COUNT)) {
            int jumpCount = (Integer)params.get(RoleConstant.MONKEY_JUMP_COUNT);
            if(jumpCount >= jumpAmount) {
                accessor.setAllowFlight(false);
            }
        }
        int count = (int)params.getOrDefault(RoleConstant.MONKEY_JUMP_COUNT, 0);
        params.put(RoleConstant.MONKEY_JUMP_COUNT, count + 1);
        e.setCancelled(true);
        accessor.setFlying(false);
        double acceleration = (double)getMetaInfo(MetaConstants.META_MONKEY_JUMP_TOSSING, level);
        double velocityUp = (double)getMetaInfo(MetaConstants.META_MONKEY_JUMP_ACCELERATION, level);
        double cooldown = (double)getMetaInfo(MetaConstants.META_MONKEY_JUMP_COOLDOWN, level);
        accessor.sendMessage(acceleration + " " + velocityUp);
        accessor.setVelocity(accessor.getVelocity().multiply(acceleration).setY(velocityUp).add(accessor.getLocation().getDirection().normalize()));
        params.put("cooldown", System.currentTimeMillis() + (long)(cooldown * DataConstants.ONE_SECOND));
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player accessor = e.getPlayer();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(accessor.getUniqueId());
        if(!(role instanceof Monkey)) {
            return;
        }
        CheckOnGround(accessor, e.getFrom(), e.getTo());
    }

    private void CheckOnGround(Player accessor, Location from, Location to) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(accessor);
        Map<String, Object> params = model.getParameters();
        if((from.getBlockY()>to.getBlockY())
                && !(accessor.getLocation().add(0, -2, 0)
                .getBlock()
                .getType()
                .equals(Material.AIR)) && (params.containsKey("cooldown") && (((long)params.get("cooldown")) < System.currentTimeMillis()))) {
            accessor.setAllowFlight(true);
            params.remove(RoleConstant.MONKEY_JUMP_COUNT);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getAmount() < 1 || item.getType() == Material.AIR) {
            return;
        }
        if (!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Monkey)) return;
        String isInvisiblity = NBT.get(item, nbt -> (String) nbt.getString(RoleConstant.MONKEY_NBT_CONTAINER));
        if (StringUtils.isEmpty(isInvisiblity) || !isInvisiblity.equals(RoleConstant.MONKEY_NBT_INVISIBILITY)) return;
        if(InventoryUtils.isCooldownRemain(item)) {
            InventoryUtils.notifyAboutCooldown(player, item);
            return;
        }
        GameInstance instance = GameInstance.getInstance();
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        String team = dataInstance.getPlayerTeam(player.getUniqueId());
        PlayerModel model = instance.getPlayerModelFromTeam(team, player);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int level = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int length = (Integer) getMetaInfo(MetaConstants.META_MONKEY_INVISIBILITY_DURATION, level);
        World world = player.getWorld();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, length * DataConstants.TICKS_IN_SECOND, 0, false, false));
        world.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, .5f, .8f);
        float cooldown = ((Double) getMetaInfo(MetaConstants.META_MONKEY_INVISIBILITY_COOLDOWN, level)).floatValue();
        InventoryUtils.setCooldownForItem(model, item, cooldown);
    }

    @Override
    public List<IDamageInvoker> getDamageInvokers() {
        return Collections.singletonList(new MonkeyWandDamage());
    }
}
