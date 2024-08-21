package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.Sound;
import remonone.nftilation.game.ingame.actions.IAction;

import java.util.Map;

public class MoneyRain implements IAction {
    @Override
    public void Init(Map<String, Object> params) {

    }

    @Override
    public String getTitle() {
        return "Денежный дождь";
    }

    @Override
    public String getDescription() {
        return "Это денежный дождь, аллилуйя, дождь из денег";
    }

    @Override
    public Sound getSound() {
        return Sound.WEATHER_RAIN;
    }
}
