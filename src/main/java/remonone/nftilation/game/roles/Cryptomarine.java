package remonone.nftilation.game.roles;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.damage.CryptomarineAxeDamage;
import remonone.nftilation.game.damage.CryptomarineDeathHandler;
import remonone.nftilation.game.models.IDamageHandler;
import remonone.nftilation.game.models.IDamageInvoker;

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
    public List<IDamageHandler> getDamageHandlers() {
        return Collections.singletonList(new CryptomarineDeathHandler());
    }

    @Override
    public List<IDamageInvoker> getDamageInvokers() {
        return Collections.singletonList(new CryptomarineAxeDamage());
    }
}
