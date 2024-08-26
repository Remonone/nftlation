package remonone.nftilation.game.ingame.services.events;

import org.bukkit.entity.Player;
import remonone.nftilation.game.ingame.services.IPurchasableService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorEnforcementEvent implements IPurchasableService {

    private static final Map<UUID, ArmorEnforcementUnit> enforcementUnits = new HashMap<UUID, ArmorEnforcementUnit>();

    @Override
    public String getServiceName() {
        return "armor-enforcement";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {

    }

    private static class ArmorEnforcementUnit {
        Map<UUID, Integer> playerEnforcementMap;
        public ArmorEnforcementUnit(Map<UUID, Integer> playerEnforcementMap) {
            this.playerEnforcementMap = playerEnforcementMap;
        }
    }
}
