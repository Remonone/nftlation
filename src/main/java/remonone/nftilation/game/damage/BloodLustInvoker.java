package remonone.nftilation.game.damage;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.runes.BloodLustRune;
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;

public class BloodLustInvoker extends BaseDamageInvoker {
    @Override
    public int getPriority() {
        return 15;
    }

    @Override
    public void OnEntityDamageDealing(EntityDamageByEntityEvent e, PlayerUtils.AttackerInfo info) {
        Player attacker = info.attacker;
        PlayerModel attackerModel = PlayerUtils.getModelFromPlayer(attacker);
        if(attackerModel == null) return;
        Map<String, Object> params = attackerModel.getParameters();
        if(params.isEmpty() || !params.containsKey(PropertyConstant.PLAYER_RUNE_ID) || !params.containsKey(PropertyConstant.PLAYER_LEVEL_PARAM)) return;
        String runeId = (String) params.get(PropertyConstant.PLAYER_RUNE_ID);
        Rune rune = Rune.getRuneByID(runeId);
        if(!(rune instanceof BloodLustRune)) return;
        int level = (Integer) params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        BloodLustRune bloodLustRune = (BloodLustRune) rune;
        double percentage = bloodLustRune.getHealthPercentage(level);
        double damage = e.getDamage();
        double heal = damage * percentage / 100;
        double maxHealth = attacker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        info.attacker.setHealth(Math.min(maxHealth, Math.max(0, info.attacker.getHealth() + heal)));
    }
}
