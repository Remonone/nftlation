package remonone.nftilation.application.services;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class SkinCache {

    private final Map<String, SkinnedTile> skinCache = new HashMap<>();

    private static SkinCache instance;

    public static SkinCache getInstance() {
        if (instance == null) {
            instance = new SkinCache();
        }
        return instance;
    }

    private SkinCache() {}

    public void storeSkin(String role, String texture, String signature) {
        if (skinCache.containsKey(role)) return;
        skinCache.put(role, new SkinnedTile(texture, signature));
    }

    public String getTexture(String role) {
        if(!skinCache.containsKey(role)) return "";
        return skinCache.get(role).getTexture();
    }

    public String getSignature(String role) {
        if(!skinCache.containsKey(role)) return "";
        return skinCache.get(role).getSignature();
    }

    @Getter
    @AllArgsConstructor
    private static class SkinnedTile {
        private String texture;
        private String signature;
    }
}
