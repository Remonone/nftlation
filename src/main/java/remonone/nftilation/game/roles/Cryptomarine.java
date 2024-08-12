package remonone.nftilation.game.roles;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.damage.CryptomarineAxeDamage;
import remonone.nftilation.game.damage.CryptomarineDeathHandler;
import remonone.nftilation.game.models.IDamageHandler;
import remonone.nftilation.game.models.IDamageInvoker;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

public class Cryptomarine extends Role {
    
    public Cryptomarine() {
        super("CM");
    }
    
    @Override
    public String getRoleID() {
        return "CM";
    }
    
    @Override
    protected List<ItemStack> getAbilityItems(Map<String, Object> params){
        ItemStack itemStack = new ItemStack(Material.SHIELD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.CTYPTOMARINE_SHIELD);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return Collections.singletonList(itemStack);
    }
    
    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        if(!PlayerUtils.validateParams(params)) return;
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        float health = DataConstants.PLAYER_HEALTH + upgradeLevel * 2;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        float speed = DataConstants.PLAYER_SPEED;
        float modifier = upgradeLevel == 3 ? 10 : 20;
        player.setWalkSpeed(speed - (speed / 100) * modifier);
        if(upgradeLevel > 1) {
            player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, DataConstants.CONSTANT_POTION_DURATION, 1, false, false));
        }
    }

    @Override
    public List<IDamageHandler> getDamageHandlers() {
        return Collections.singletonList(new CryptomarineDeathHandler());
    }

    @Override
    public List<IDamageInvoker> getDamageInvokers() {
        return Collections.singletonList(new CryptomarineAxeDamage());
    }
}
