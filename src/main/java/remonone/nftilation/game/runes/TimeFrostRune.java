package remonone.nftilation.game.runes;

import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.game.damage.TimeFrostInvoker;
import remonone.nftilation.game.models.EffectPotion;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.NestedObjectFetcher;

import java.util.Collections;
import java.util.List;

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
    
    @SuppressWarnings("unchecked")
    public List<EffectPotion> getEffects(int level) {
        Object list = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_EFFECT, getMeta(), level);
        if(list == null) {
            Logger.warn("Time frost rune: Cannot fetch effects list! Skipping...");
            return Collections.emptyList();
        }
        return (List<EffectPotion>) list;
    }
    
    public double getStunCooldown(int level) {
        Object durationRaw = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_TIME_COOLDOWN, getMeta(), level);
        if(durationRaw == null) {
            Logger.warn("Time Frost rune: Cannot fetch stun cooldown! Skipping...");
            return 0;
        }
        return (Double) durationRaw;
    }
    
    public double getStunDuration(int level) {
        Object durationRaw = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_TIME_FROST, getMeta(), level);
        if(durationRaw == null) {
            Logger.warn("Time Frost rune: Cannot fetch stun duration! Skipping...");
            return 0;
        }
        return (Double) durationRaw;
    }
    
    public double getStunChance(int level) {
        Object percentageRaw = NestedObjectFetcher.getNestedObject(MetaConstants.META_RUNE_TIME_CHANCE, getMeta(), level);
        if(percentageRaw == null) {
            Logger.warn("Time Frost rune: Cannot fetch stun chance! Skipping...");
            return 0;
        }
        return (Double) percentageRaw;
    }
}
