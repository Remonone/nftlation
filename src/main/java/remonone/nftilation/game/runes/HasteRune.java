package remonone.nftilation.game.runes;

import org.bukkit.event.EventHandler;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnCooldownApplyEvent;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.NestedObjectFetcher;

import java.util.Map;

public class HasteRune extends Rune {
    
    public HasteRune() {
        super("HR");
    }
    
    @Override
    public String getRuneID() {
        return "HR";
    }
    
    @EventHandler
    public void onCooldownApply(final OnCooldownApplyEvent e) {
        PlayerModel model = e.getModel();
        if(model == null) return;
        Map<String, Object> params = model.getParameters();
        if(params == null || params.isEmpty()) return;
        String runeID = (String) params.get(PropertyConstant.PLAYER_RUNE_ID);
        if(runeID == null || runeID.isEmpty()) return;
        if(!runeID.equals(getRuneID())) return;
        int level = (Integer) params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        Object raw = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_EFFECT, this.getMeta(), level);
        if(raw == null) return;
        float cooldownReduction = (float) raw;
        float cooldown = e.getCooldown();
        e.setCooldown(cooldown * (100 - cooldownReduction) / 100);
    }
}
