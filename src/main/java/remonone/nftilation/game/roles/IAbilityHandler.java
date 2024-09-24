package remonone.nftilation.game.roles;

import remonone.nftilation.game.models.PlayerModel;

public interface IAbilityHandler {
    boolean executeHandle(PlayerModel model);
    float getCooldown(PlayerModel model);
}
