package remonone.nftilation.game.damage;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.game.runes.TimeFrostRune;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;
import java.util.Random;

public class TimeFrostInvoker extends BaseDamageInvoker implements Listener {
    
    private static final Random RANDOM = new Random();
    
    @Override
    public int getPriority() {
        return 15;
    }
    
    @Override
    public void OnEntityDamageDealing(EntityDamageByEntityEvent event, PlayerUtils.AttackerInfo info) {
        PlayerModel attackerModel = PlayerUtils.getModelFromPlayer(info.attacker);
        if(attackerModel == null) return;
        Player target = (Player) event.getEntity();
        PlayerModel targetModel = PlayerUtils.getModelFromPlayer(target);
        if(targetModel == null) return;
        Map<String, Object> params = attackerModel.getParameters();
        if(params.isEmpty() || !params.containsKey(PropertyConstant.PLAYER_RUNE_ID) || !params.containsKey(PropertyConstant.PLAYER_LEVEL_PARAM)) return;
        String runeId = (String) params.get(PropertyConstant.PLAYER_RUNE_ID);
        Rune rune = Rune.getRuneByID(runeId);
        if(!(rune instanceof TimeFrostRune)) return;
        int level = (Integer) params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        TimeFrostRune timeFrostRune = (TimeFrostRune) rune;
        double chance = timeFrostRune.getStunChance(level);
        if(RANDOM.nextFloat() * 100 > chance) return;
        long stunDuration = (long) (timeFrostRune.getStunDuration(level) * DataConstants.ONE_SECOND);
        targetModel.getParameters().put(PropertyConstant.PLAYER_STUN_DURATION, stunDuration + System.currentTimeMillis());
    }
    
}
