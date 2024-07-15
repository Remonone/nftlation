package remonone.nftilation.game.ingame.actions.donate;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CryptRaise implements IAction {

    @Override
    public void Init(Map<String, Object> params) {
        if(!params.containsKey("team")) {
            Logger.warn("Couldn't initiate CryptRaise action. Team is missing!");
            return;
        }
        String team = (String) params.get("team");
        List<Player> players = GameInstance.getInstance().getTeamPlayers(team).stream().map(GameInstance.PlayerModel::getReference).collect(Collectors.toList());
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
