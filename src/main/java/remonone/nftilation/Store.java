package remonone.nftilation;

import lombok.Getter;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.lobby.LobbyDisposer;
import remonone.nftilation.game.stage.GameStage;
import remonone.nftilation.game.transfer.GameTransfer;
import remonone.nftilation.utils.Logger;


@Getter
public class Store {
    
    private static Store instance;
    
    private final GameStage gameStage;
    private final GameTransfer gameTransfer;
    private final DataInstance dataInstance;
    private final LobbyDisposer lobbyDisposer;
    
    private Store() {
        Logger.log("Initializing components...");
        gameStage = new GameStage();
        gameTransfer = new GameTransfer();
        lobbyDisposer = new LobbyDisposer();
        dataInstance = new DataInstance();
    }
    
    
    public static Store getInstance() {
        if(instance == null) {
            instance = new Store();
        }
        return instance;
    }
}
