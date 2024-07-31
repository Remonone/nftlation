package remonone.nftilation.game.ingame.actions.donate;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.List;
import java.util.Map;

public class CryptRaise implements IAction {

    @Override
    public void Init(Map<String, Object> params) {
        if(!params.containsKey(PropertyConstant.ACTION_TEAM)) {
            throw new NullPointerException("Couldn't initiate CryptRaise action. Team is missing!");
        }
        String team = (String) params.get(PropertyConstant.ACTION_TEAM);
        List<Player> players = PlayerUtils.getPlayersFromTeam(GameInstance.getInstance().getTeam(team));
        if(players.isEmpty()) {
            Logger.warn("CryptRaise task failed. Team name has not been found!");
            return;
        }
        for(Player p : players) {
            p.getInventory().addItem(new ItemStack(Material.BAKED_POTATO, 10));
        }
    }

    @Override
    public String getTitle() {
        return "Воодушевление";
    }

    @Override
    public String getDescription() {
        return "Вы находите в себе силы воспрять и идти дальше!";
    }

    @Override
    public Sound getSound() {
        return Sound.ITEM_TOTEM_USE;
    }

}
