package remonone.nftilation.game.roles;

import org.bukkit.inventory.ItemStack;

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
        return Collections.emptyList();
    }
}
