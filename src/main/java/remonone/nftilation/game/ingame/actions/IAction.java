package remonone.nftilation.game.ingame.actions;

import org.bukkit.Sound;

import java.util.Map;

public interface IAction {
    void Init(Map<String, Object> params);
    String getTitle();
    String getDescription();
    Sound getSound();
}
