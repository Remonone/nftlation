package remonone.nftilation.game.stage;

import lombok.Getter;
import lombok.Setter;
import remonone.nftilation.enums.Stage;

@Setter
@Getter
public class GameStage {
    
    private Stage stage;
    
    public GameStage() {
        this.stage = Stage.IDLE;
    }

}
