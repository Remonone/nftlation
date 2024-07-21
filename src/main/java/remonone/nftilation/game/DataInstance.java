package remonone.nftilation.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.World;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.enums.LoginState;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.Logger;

import java.util.*;

public class DataInstance {
        
    public DataInstance() {
        
    }
    
    private boolean initialized = false;
    @Getter
    private World mainWorld;
    
    public void Init(List<TeamData> teams, World world) {
        if(teams.isEmpty()) {
            Logger.warn("Teams list is empty");
            return;
        }
        this.mainWorld = world;
        this.teamData.addAll(teams);
        for(TeamData teamData : teams) {
            this.teams.put(teamData.getTeamName(), new ArrayList<>());
        }
        initialized = true;
    }

    @Getter
    private final List<PlayerInfo> players = new ArrayList<>();
    @Getter
    private final List<TeamData> teamData = new ArrayList<>();
    
    @Getter
    private final Map<String, List<PlayerInfo>> teams = new HashMap<>();
    
    public LoginState TryAddPlayerToGame(PlayerData playerData, Player player) {
        if(playerData.getRole().equals(PlayerRole.PLAYER) && playerData.getTeam().getTeamName().isEmpty()) return LoginState.EMPTY_TEAM;
//        if(players.contains(playerData)) {
        if(players.stream().anyMatch(playerData1 -> playerData1.getPlayerId().equals(player.getUniqueId()))) {
            return LoginState.ALREADY_LOGGED_IN;
        }
        
        if(!initialized && playerData.getRole().equals(PlayerRole.PLAYER)) {
            return LoginState.NOT_ALLOWED;
        }
        PlayerInfo playerInfo;
        if(playerData.getRole().equals(PlayerRole.PLAYER)) {
            if(!isTeamPresented(playerData.getTeam().getTeamName())) return LoginState.NOT_PRESENTED;
            if(Store.getInstance().getGameStage().getStage() == Stage.IN_GAME) {
                PlayerInfo info = getPlayerInfo(playerData);
                if(info == null) return LoginState.NOT_ALLOWED;
                info.data = playerData;
                playerInfo = info;
            } else {
                playerInfo = new PlayerInfo(playerData, null, player.getUniqueId(), new HashMap<>());
                teams.get(playerData.getTeam().getTeamName()).add(playerInfo);
            }
        } else {
            playerInfo = new PlayerInfo(playerData, null, player.getUniqueId(), new HashMap<>());
        }
        players.add(playerInfo);
        
        Logger.log("Player " + playerData.getLogin() + " has authenticated to the game");
        return LoginState.LOGGED_IN;
    }
    
    public String getPlayerTeam(UUID playerId) {
        PlayerInfo info = FindPlayerByName(playerId);
        if(info == null) return "";
        return info.getData().getTeam().getTeamName();
    }
    
    public void DisconnectPlayer(UUID playerId) {
        PlayerInfo info = players.stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().orElse(null);
        if(info == null) {
            Logger.warn("Player " + playerId + " has not been authenticated to the game");
            return;
        }
        players.remove(info);
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) {
            if (info.data.getRole().equals(PlayerRole.PLAYER)) {
                teams.get(info.data.getTeam().getTeamName()).removeIf(playerInfo -> playerInfo.data.getLogin().equals(info.data.getLogin()));
            }
        }
        Logger.log("Player " + info.data.getLogin() + " has been disconnected");
    }
    
    public List<PlayerInfo> getTeamPlayers(String teamName) {
        if(!teams.containsKey(teamName)) {
            Logger.warn("Team " + teamName + " does not exist");
            return new ArrayList<>();
        }
        return teams.get(teamName);
    }

    public PlayerInfo FindPlayerByName(final UUID playerId) {
        for(PlayerInfo player : players) {
            if(player.getPlayerId().equals(playerId)) {
                return player;
            }
        }
        return null;
    }
    
    
    public Map<String, Object> getPlayerParams(UUID playerId) {
        PlayerInfo info = FindPlayerByName(playerId);
        if(info == null) return null;
        return info.params;
    }
    
    public boolean isTeamPresented(String team) {
        return teams.containsKey(team);
    }
    
    public Role getPlayerRole(UUID playerId) {
        PlayerInfo info = FindPlayerByName(playerId);
        if(info == null) {
            return null;
        }
        return info.getRole();
    }

    private PlayerInfo getPlayerInfo(PlayerData playerData) {
        if(playerData == null) {
            return null;
        }
        return teams.get(playerData.getTeam().getTeamName())
                .stream().filter(playerInfo -> playerInfo.data.getLogin().equals(playerData.getLogin()))
                .findFirst().orElse(null);
    }
    
    public boolean updatePlayerRole(Role role, UUID playerId) {
        PlayerInfo info = FindPlayerByName(playerId);
        if(role == null) {
            Logger.error("Role is undefined!");
            return false;
        }
        if(info == null) {
            Logger.warn("Player " + playerId + " has not been authenticated to the game");
            return false;
        }
        if(info.getRole() != null && info.getRole().equals(role)) {
            Logger.warn("Role was already has been set to " + info.getData().getLogin());
            return false;
        }
        if(teams.get(info.getData().getTeam().getTeamName()).stream().map(PlayerInfo::getRole).anyMatch(role::equals)) {
            Logger.warn("This role has been reserved before!");
            return false;
        }
        
        info.setRole(role);
        return true;
    }
    

    @AllArgsConstructor
    @Getter
    @ToString
    public static class PlayerInfo {
        private PlayerData data;
        @Setter
        private Role role;
        @Setter
        private UUID playerId;
        @Getter
        private Map<String, Object> params;
    }
    
    
}

