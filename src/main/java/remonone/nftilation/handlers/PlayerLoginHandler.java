package remonone.nftilation.handlers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.PlayerLoginEvent;
import remonone.nftilation.enums.LoginState;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.game.stage.GameStage;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.ResetUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumSet;

public class PlayerLoginHandler implements Listener {
    
    private final static String texture = "ewogICJ0aW1lc3RhbXAiIDogMTU5MTI1NDU3MDU3MiwKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFkODE0OWI5ZDA3MzU5MzhiNWQ5OGExOGMwN2RlNzI3YjAxZmI1ODE2MTFmYjEyYzNmY2UwMzcyZWY1MmQwNyIKICAgIH0KICB9Cn0=";
    
    @EventHandler
    public void onPlayerLogin(final PlayerLoginEvent event) {
        Player player = event.getPlayer();
        PlayerData data = event.getPlayerData();
        Store instance = Store.getInstance();
        GameStage stage = instance.getGameStage();
        // Check if player able to join
        DataInstance dataInstance = instance.getDataInstance();

        LoginState state = dataInstance.TryAddPlayerToGame(data, player);
        if(!EnumSet.of(LoginState.ALREADY_LOGGED_IN, LoginState.LOGGED_IN).contains(state)) {
            KickPlayerWithReason(player, state);
            return;
        }
        
        if(state.equals(LoginState.ALREADY_LOGGED_IN)) {
            player.sendMessage(MessageConstant.ALREADY_LOGGED_IN);
            return;
        }
        ResetUtils.globalResetPlayerStats(player);
        
        // Setting up the player
        String toDisplay;
        if(data.getRole() == PlayerRole.PLAYER) {
            toDisplay = formatNickname(data.getTeam().getTeamColor(), data.getTeam().getTeamShort(), player.getName());
        } else {
            toDisplay = formatNickname(ChatColor.RED.getChar(), data.getRole().toString(), player.getName());
        }
        player.setDisplayName(toDisplay);
        player.setPlayerListName(toDisplay);
        player.setCustomName(toDisplay);
        player.setPlayerListName(toDisplay);
        player.sendMessage(ChatColor.GREEN + MessageConstant.SUCCESSFUL_LOGIN);
        changeName(player, toDisplay);
        // Transferring the player
        if(stage.getStage() == Stage.LOBBY) {
            instance.getLobbyDisposer().DisposePlayer(data, player);
            player.setGameMode(GameMode.ADVENTURE);
        }
        
        if(stage.getStage() == Stage.IN_GAME) {
            player.setGameMode(GameMode.SURVIVAL);
            Role role = instance.getDataInstance().getPlayerRole(player.getUniqueId());
            DataInstance.PlayerInfo playerData = instance.getDataInstance().FindPlayerByName(player.getUniqueId());
            GameInstance.PlayerModel model = GameInstance.getInstance()
                    .getTeamPlayers(playerData.getData().getTeam().getTeamName())
                    .stream()
                    .filter(playerModel -> playerModel.getReference().getUniqueId().equals(playerData.getPlayerId()))
                    .findFirst()
                    .orElse(null);
            if(model == null) {
                player.kickPlayer(MessageConstant.NO_PERMISSION_TO_JOIN);
                return;
            }
            model.setReference(player);
            ScoreboardHandler.updateScoreboard(model);
            GameInstance.getInstance().getCounter().bar.addPlayer(player);
            Role.UpdatePlayerAbilities(player, role, model.getUpgradeLevel());
        }
            
    }

    private void KickPlayerWithReason(Player player, LoginState state) {
        String reason = GetReason(state);
        player.kickPlayer(reason);
    }

    private String GetReason(LoginState state) {
        switch(state) {
            case NOT_PRESENTED:
                return MessageConstant.NO_TOURNAMENT_PRESENTED;
            case NOT_ALLOWED:
                return MessageConstant.NO_PERMISSION_TO_JOIN;
            default:
                return MessageConstant.UNKNOWN_KICK;
        }
    }

    private String formatNickname(char color, String prefix, String name) {
        return ChatColor.getByChar(color) + "[" + prefix + "] " + name;
    }
    
    @EventHandler
    public void onPlayerDisconnect(final PlayerQuitEvent event) {
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        dataInstance.DisconnectPlayer(event.getPlayer().getUniqueId());
    }

    public void changeName(Player p, String newName){
        for(Player pl : Bukkit.getOnlinePlayers()) {
            if (pl == p) continue;
            //CHANGES THE PLAYER'S GAME PROFILE
            GameProfile gp = ((CraftPlayer)p).getProfile();
            pl.hidePlayer(Nftilation.getInstance(), p);
            try {
                gp.getProperties().removeAll("textures");
                gp.getProperties().put("textures", new Property("textures", texture, "aBUbj9LoIbT1B26lsabZTWNyBsHeTbDC92pbYLpofO/ytqu+8ej+TYIlqsmWIyfqufgBFIf7bGCCjH260o2YBz09ZcImqlkAFG10OggsF799pYv3WfvRosv8v2VcPnxVmFlQ1jIBYYhqxcUJkqEAMJoQPv8KV4SnOG4pQ6YUqnC7febAIFDSVx0D0Ho8A74BXQFqXbX4YDt2Qe7WDZDHtjG6gk0+IE2asvQ71Bx9OwPOQn83FbxD1wrz3VJ0N+NAfq2Iada1cITXNN5W8Te7f0gOwpVSmB4m5Wgx2m4avJv0kb+WMZ1T0f4CG348aGTEOF4ypVuLbO8LhPdPK50MZIoCtd42YTd/ClBoIc8AhkIy9bxIqTRpIkKgkSYG/dYB0I4F6H3+IINPOwPj+FlLYzBh764FbKW6tdqhVnA2vb27GX8ewTfCE2NIoddjKIeGZzX/ubfpOZqBAFG5tojzo0o5r3atMmILyZ1yowPhbdCQ3pC65IZWPSotDgUaeqOXvVfonk9ocs+gVXu/XaBNG/pYKsAGDJmbAnwqk/D4dqtONxT+4R5WQpYxTIOgT0drMVhupiYbtzjfyoHiJXu+rPwis8k7WJgMDpCZrcsXhAZYuttr7JLi6euTYeYTptSwxywLyCLDVoLyq0tzQbOnRVYCxrTEd2ndgyPVPavPbVQ="));
                
                Field nameField = GameProfile.class.getDeclaredField("name");
                nameField.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);
                if(newName.length() > 16) newName = newName.substring(0, 16);
                nameField.set(gp, ChatColor.translateAlternateColorCodes('&', newName));
                pl.showPlayer(Nftilation.getInstance(), p);
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
    }
}
