package remonone.nftilation.game.roles;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class AbilityItemsHandler {

    private String container;
    private Role maintainRole;
    private Map<String, IAbilityHandler> handlerMap;
    
}
