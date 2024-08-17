package remonone.nftilation.game.damage;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.runes.ReaperRune;
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;

public class ReaperRuneInvoker extends BaseDamageInvoker {
    @Override
    public int getPriority() {
        return 15;
    }
    
    @Override
    public void OnEntityDamageDealing(EntityDamageByEntityEvent event, PlayerUtils.AttackerInfo info) {
        PlayerModel attackerModel = PlayerUtils.getModelFromPlayer(info.attacker);
        if(attackerModel == null) return;
        Map<String, Object> params = attackerModel.getParameters();
        if(params.isEmpty()
                || !params.containsKey(PropertyConstant.PLAYER_RUNE_ID)
                || !params.containsKey(PropertyConstant.PLAYER_LEVEL_PARAM)
                || !params.containsKey(PropertyConstant.PLAYER_KILL_COUNT)) return;
        String runeId = (String) params.get(PropertyConstant.PLAYER_RUNE_ID);
        Rune rune = Rune.getRuneByID(runeId);
        if(!(rune instanceof ReaperRune)) return;
        int level = (Integer) params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int killCount = (Integer) params.get(PropertyConstant.PLAYER_KILL_COUNT);
        ReaperRune bloodLustRune = (ReaperRune) rune;
        double percentage = bloodLustRune.getPercentValue(level);
        double damage = event.getFinalDamage();
        double finalDamage = damage * percentage * killCount / 100;
        event.setDamage(damage + finalDamage);
    }
}
