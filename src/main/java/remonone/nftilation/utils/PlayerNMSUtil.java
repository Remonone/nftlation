package remonone.nftilation.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_12_R1.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import remonone.nftilation.Nftilation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;

public class PlayerNMSUtil {

    public static void changePlayerSkin(Player p, String texture, String signature) {
        if(p == null) return;
        if(StringUtils.isEmpty(texture) || StringUtils.isEmpty(signature)) {
            Logger.warn("Trying to load undefined skin for: " + p.getName());
            return;
        }
        Location location = p.getLocation();
        EntityPlayer pl = ((CraftPlayer) p).getHandle();
        EnumGamemode gameMode = EnumGamemode.getById(p.getGameMode().getValue());
        GameProfile profile = pl.getProfile();
        PlayerConnection connection = pl.playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                pl));
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", texture, signature));
        connection.sendPacket(new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                pl));
        @SuppressWarnings("deprecation")
        int dimensionId = pl.getWorld().getWorld().getEnvironment().getId();
        Logger.log("Skin is set for player " + p.getDisplayName() + ". Respawning...");
        connection.sendPacket(new PacketPlayOutRespawn(dimensionId, pl.getWorld().getDifficulty(), pl.getWorld().getWorldData().getType(), gameMode));
        connection.sendPacket(new PacketPlayOutPosition(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), new HashSet<>(), 0));
        p.updateInventory();
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(Nftilation.getInstance(), p);
            player.showPlayer(Nftilation.getInstance(), p);
        }
    }

    @SuppressWarnings("deprecation")
    public static void changePlayerName(Player p, String newName){
        GameProfile gp = ((CraftPlayer)p).getProfile();
        try {
            Field nameField = GameProfile.class.getDeclaredField("name");
            nameField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);
            if(newName.length() > 16) newName = newName.substring(0, 16);
            nameField.set(gp, ChatColor.translateAlternateColorCodes('&', newName));
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new IllegalStateException(ex);
        }
        for(Player pl : Bukkit.getOnlinePlayers()) {
            if (pl == p) continue;
            pl.hidePlayer(p);
            pl.showPlayer(p);
            
        }

    }
}
