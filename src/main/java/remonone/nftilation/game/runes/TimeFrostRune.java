package remonone.nftilation.game.runes;

import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.game.damage.TimeFrostInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.NestedObjectFetcher;

public class TimeFrostRune extends Rune {
    
    public TimeFrostRune() {
        super("TR");
    }
    
    @Override
    public String getRuneID() {
        return "TR";
    }
    
    @Override
    public void setPlayer(PlayerModel model) {
        model.getDamageInvokers().add(new TimeFrostInvoker());
    }
    
    public double getStunDuration(int level) {
        Object durationRaw = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_TIME_FROST, getMeta(), level);
        if(durationRaw == null) {
            Logger.warn("Time Frost rune: Cannot fetch damage percentage! Skipping...");
            return 0;
        }
        return (Double) durationRaw;
    }
    
    public double getStunChance(int level) {
        Object percentageRaw = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_TIME_CHANCE, getMeta(), level);
        if(percentageRaw == null) {
            Logger.warn("Time Frost rune: Cannot fetch damage percentage! Skipping...");
            return 0;
        }
        return (Double) percentageRaw;
    }
}
