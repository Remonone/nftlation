package remonone.nftilation.game.ingame.actions.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.BlockUtils;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.VectorUtils;

import java.util.*;

import static org.bukkit.Bukkit.getServer;


public class MassiveDelirium implements IAction {
    
    private final static int MAX = 4;
    
    private final static Random random = new Random();
    
    private final List<PlayerDelirium> deliriums = new ArrayList<>();
    
    @Override
    public void Init(Map<String, Object> params) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(PlayerUtils.getModelFromPlayer(player) == null) continue;
            PlayerDelirium delirium = new PlayerDelirium(player);
            try {
                delirium.fillDuplicates();
            } catch (Exception e) {
                continue;
            }
            deliriums.add(delirium);
        }
        
        startIteration();
    }

    private void startIteration() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for(PlayerDelirium delirium : deliriums) {
                    delirium.summonDelirious();
                }
            }
        };
        runnable.runTaskTimer(Nftilation.getInstance(), 100, 10 * DataConstants.TICKS_IN_SECOND);
        new BukkitRunnable() {
            @Override
            public void run() {
                for(PlayerDelirium delirium : deliriums) {
                    delirium.clearDelirious();
                }
                getServer().getScheduler().cancelTask(runnable.getTaskId());
                deliriums.clear();
            }
        }.runTaskLater(Nftilation.getInstance(), 3 * DataConstants.TICKS_IN_MINUTE);
    }

    @Override
    public String getTitle() {
        return "Массовый бред";
    }

    @Override
    public String getDescription() {
        return "А тебе случайно не показалось..?";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_GHAST_AMBIENT;
    }
    
    private static class PlayerDelirium {
        
        private final Queue<EntityPlayer> playerClones;
        private final Player player;
        private List<PlayerModel> duplicates;
        
        public PlayerDelirium(Player player) {
            playerClones = new LinkedList<>();
            this.player = player;
        }
        
        public void fillDuplicates() throws Exception {
            PlayerModel model = PlayerUtils.getModelFromPlayer(player);
            Map<String, Object> params = model.getParameters();
            String teamName = (String) params.get(PropertyConstant.PLAYER_TEAM_NAME);
            ITeam team = GameInstance.getInstance().getTeam(teamName);
            if(team == null) {
                throw new Exception("Team was not found");
            }
            List<PlayerModel> teams = new ArrayList<>();
            Iterator<ITeam> teamsIt = GameInstance.getInstance().getTeamIterator();
            while(teamsIt.hasNext()) {
                ITeam t = teamsIt.next();
                if(t.getTeamName().equals(team.getTeamName())) continue;
                teams.addAll(t.getPlayers());
            }
            this.duplicates = teams;
        }
        
        public void summonDelirious() {
            MinecraftServer server = ((CraftServer) getServer()).getHandle().getServer();
            WorldServer worldServer = ((CraftWorld)this.player.getWorld()).getHandle();
            if(duplicates.
                    isEmpty()) return;
            if(playerClones.size() > 2) {
                EntityPlayer player = playerClones.poll();
                player.die();
                worldServer.removeEntity(player);
            }
            EntityPlayer newPlayer = createPlayer(server, worldServer);
            if(newPlayer == null) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location clonePos = getEmptyBlockLocation();
                    if(clonePos == null) return;
                    newPlayer.setPosition(clonePos.getX(), clonePos.getY(), clonePos.getZ());
                    newPlayer.playerConnection = new PlayerConnection(server,
                            new NetworkManager(EnumProtocolDirection.CLIENTBOUND), newPlayer);
                    worldServer.addEntity(newPlayer, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    
                    for(Player playerToHide : Bukkit.getOnlinePlayers()) {
                        playerToHide.hidePlayer(Nftilation.getInstance(), newPlayer.getBukkitEntity());
                    }
                    player.showPlayer(Nftilation.getInstance(), newPlayer.getBukkitEntity());
                    server.getPlayerList().players.remove(newPlayer);
                    playerClones.add(newPlayer);
                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(newPlayer));
                }
            }.runTask(Nftilation.getInstance());
        }

        private EntityPlayer createPlayer(MinecraftServer server, WorldServer worldServer) {
            if(this.duplicates.isEmpty()) return null;
            PlayerModel model = this.duplicates.get(random.nextInt(this.duplicates.size()));
            EntityPlayer playerToCopy = ((CraftPlayer)model.getReference()).getHandle();
            GameProfile profileToCopy = playerToCopy.getProfile();
            GameProfile newProfile = new GameProfile(UUID.randomUUID(), profileToCopy.getName());
            profileToCopy.getProperties().get("textures").stream().findFirst().ifPresent(property -> newProfile.getProperties().put("textures", property));
            return new EntityPlayer(server, worldServer, newProfile, new PlayerInteractManager(worldServer));
        }
        
        private Location getEmptyBlockLocation() {
            int it = 0;
            while(it < MAX) {
                Vector posToSpread = VectorUtils.getRandomPosInCircle(this.player.getLocation().toVector(), 10);
                Block block = this.player.getWorld().getBlockAt(posToSpread.getBlockX(), posToSpread.getBlockY(), posToSpread.getBlockZ());
                Location clonePos = BlockUtils.getNearestEmptySpace(block, 2);
                if(clonePos != null) {
                    int localIt = 0;
                    while(clonePos.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR) && localIt < 3) {
                        clonePos.add(VectorUtils.DOWN);
                        localIt++;
                    }
                    return clonePos;
                }
                it++;
            }
            return null;
        }
        
        public void clearDelirious() {
            playerClones.forEach(clone -> {
                clone.die();
                clone.playerConnection.disconnect("");
            });
            playerClones.clear();
            duplicates.clear();
        }
    }
}
