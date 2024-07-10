package remonone.nftilation.game.ingame.services;

import java.util.HashMap;
import java.util.Map;

public class ServiceContainer {
    
    private final static Map<String, IPurchasableService> services = new HashMap<>();
    
    public static void registerService(IPurchasableService service) {
        services.put(service.getServiceName(), service);
    }
    
    public static IPurchasableService getService(String serviceName) {
        return services.get(serviceName);
    }
}
