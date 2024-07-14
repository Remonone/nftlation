package remonone.nftilation.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;

import java.util.Collection;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class ChunkUtils {
    
    // BUG: Not working
    public static void loadChunkForTime(Location loc, int ticks) {
        MinecraftServer server = ((CraftServer)getServer()).getServer();
        UUID chunkLoaderId = UUID.randomUUID();
        CraftPlayer chunkPlayer = getCraftPlayer(loc, chunkLoaderId, server);
        for(Player pl : Bukkit.getOnlinePlayers()) {
            Logger.broadcast(pl.getUniqueId().toString());
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (chunkPlayer).getHandle()));
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(chunkPlayer.getEntityId()));
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn((chunkPlayer).getHandle()));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player: Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (chunkPlayer).getHandle()));
                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(chunkPlayer.getEntityId()));
                }
            }
        }.runTaskLater(Nftilation.getInstance(), ticks);
    }

    private static CraftPlayer getCraftPlayer(Location loc, UUID chunkLoaderId, MinecraftServer server) {
        GameProfile profile = new GameProfile(chunkLoaderId, "chunkLoader" + chunkLoaderId.toString().substring(0, 5));
        WorldServer worldServer = ((CraftWorld) loc.getChunk().getWorld()).getHandle();
        EntityPlayer chunkLoader = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));
        chunkLoader.setPosition(loc.getX(), loc.getY(), loc.getZ());
        Collection<Property> props = profile.getProperties().get("textures");
        for(Property prop : props) {
            System.out.println(prop);
        }
        CraftPlayer chunkPlayer = chunkLoader.getBukkitEntity();
        chunkPlayer.setAllowFlight(true);
        chunkPlayer.setFlying(true);
        return chunkPlayer;
    }
}
