package remonone.nftilation.game.runes;

import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.game.damage.ReaperRuneInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.NestedObjectFetcher;

public class ReaperRune extends Rune {
    
    public ReaperRune() {
        super("RR");
    }
    
    @Override
    public String getRuneID() {
        return "RR";
    }
    
    @Override
    public void setPlayer(PlayerModel model) {
        model.getDamageInvokers().add(new ReaperRuneInvoker());
    } 
    
    public double getPercentValue(int level) {
        Object percentageRaw = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_EFFECT, getMeta(), level);
        if(percentageRaw == null) {
            Logger.warn("Reaper rune: Cannot fetch damage percentage! Skipping...");
            return 0;
        }
        return (Double) percentageRaw;
    }
}
