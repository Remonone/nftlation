package remonone.nftilation.application.models;

import lombok.Setter;
import lombok.ToString;
import remonone.nftilation.game.ingame.actions.ActionType;

import java.util.Map;

@ToString
@Setter
public class Donation {
    public ActionType type;
    public Map<String, Object> donationParameters;
}
