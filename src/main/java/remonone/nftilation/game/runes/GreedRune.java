package remonone.nftilation.game.runes;

import org.bukkit.event.EventHandler;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.NestedObjectFetcher;

import java.util.Map;

public class GreedRune extends Rune {
    
    public GreedRune() {
        super("GR");
    }

    @Override
    public String getRuneID() {
        return "GR";
    }
    
    @EventHandler
    public void onPlayerAward(OnTokenTransactionEvent e) {
        if(e.isCancelled() || !e.getTransactionType().equals(OnTokenTransactionEvent.TransactionType.GAIN)) {
            return;
        }
        PlayerModel model = e.getPlayer();
        if(model == null) { return; }
        Map<String, Object> params = model.getParameters();
        if(params == null || params.isEmpty()) { return; }
        String runeId = (String)params.getOrDefault(PropertyConstant.PLAYER_RUNE_ID, "_");
        if(runeId.equals("_")) { return; }
        if(!runeId.equals(getRuneID())) { return; }
        int level = (Integer) params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        Object raw = NestedObjectFetcher.getNestedObject("effect", this.getMeta(), level);
        if(raw == null) { return; }
        int additionalPercent = (Integer)raw;
        float tokens = e.getTokensAmount();
        e.setTokensAmount(tokens + tokens / 100 * additionalPercent);
    }
}
