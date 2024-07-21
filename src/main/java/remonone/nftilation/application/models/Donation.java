package remonone.nftilation.application.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import remonone.nftilation.game.ingame.actions.ActionType;

import java.util.Map;

@ToString
@Setter
@Getter
public class Donation {
    private ActionType type;
    private String donation_id;
    private Map<String, Object> parameters;
}
