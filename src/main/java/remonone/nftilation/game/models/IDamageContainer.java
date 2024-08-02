package remonone.nftilation.game.models;

import java.util.List;

public interface IDamageContainer {
    List<IDamageHandler> getDamageHandlers();
    List<IDamageInvoker> getDamageInvokers();
}
