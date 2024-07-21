package remonone.nftilation.game.ingame.actions.donate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.Store;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.ingame.actions.IAction;

import java.util.Map;

public class GoldenApple implements IAction {
    @Override
    public void Init(Map<String, Object> params) {
        String playerName = (String) params.get(PropertyConstant.ACTION_PLAYER);
        DataInstance.PlayerInfo info = Store.getInstance().getDataInstance().getPlayers().stream().filter(playerInfo -> playerInfo.getData().getLogin().equals(playerName)).findFirst().orElse(null);
        if(info == null) throw new NullPointerException("Player was not found!");
        Bukkit.getPlayer(info.getPlayerId()).getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
    }

    @Override
    public String getTitle() {
        return "Золотое яблоко";
    }

    @Override
    public String getDescription() {
        return "Ваш тайный поклонник одарил вас золотым яблоком!";
    }

    @Override
    public Sound getSound() {
        return Sound.BLOCK_ENCHANTMENT_TABLE_USE;
    }
}
