package remonone.nftilation.game.runes;

import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.game.damage.BloodLustInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.NestedObjectFetcher;

public class BloodLustRune extends Rune {
    
    public BloodLustRune() {
        super("BR");
    }
    
    @Override
    public String getRuneID() {
        return "BR";
    }
    
    @Override
    public void setPlayer(PlayerModel model) {
        model.getDamageInvokers().add(new BloodLustInvoker());
    }
    
    public double getHealthPercentage(int level) {
        Object percentageRaw = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_EFFECT, getMeta(), level);
        if(percentageRaw == null) {
            Logger.warn("BloodLust: Cannot fetch health percentage! Skipping...");
            return 0;
        }
        return (Double) percentageRaw;
    }
}
