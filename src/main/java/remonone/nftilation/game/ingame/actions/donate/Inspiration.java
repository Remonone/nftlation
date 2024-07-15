package remonone.nftilation.game.ingame.actions.donate;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Inspiration implements IAction {

    @Override
    public void Init(Map<String, Object> params) {
        if(!params.containsKey("team")) {
            Logger.warn("Couldn't initiate Inspiration action. Team is missing!");
            return;
        }
        String team = (String) params.get("team");
        List<Player> players = GameInstance.getInstance().getTeamPlayers(team).stream().map(GameInstance.PlayerModel::getReference).collect(Collectors.toList());
        if(players.isEmpty()) {
            Logger.warn("Inspiration task failed. Team name has not been found!");
            return;
        }
        for(Player p : players) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, DataConstants.TICKS_IN_MINUTE, 0, false, true));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DataConstants.TICKS_IN_MINUTE, 0, false, true));
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
