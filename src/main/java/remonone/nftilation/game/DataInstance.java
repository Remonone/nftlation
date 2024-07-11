package remonone.nftilation.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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

    private final List<PlayerData> players = new ArrayList<>();
    @Getter
    private final List<TeamData> teamData = new ArrayList<>();
    
    @Getter
    private final Map<String, List<PlayerInfo>> teams = new HashMap<>();
    
    public LoginState TryAddPlayerToGame(PlayerData playerData, Player player) {
        if(playerData.getRole().equals(PlayerRole.PLAYER) && playerData.getTeam().getTeamName().isEmpty()) return LoginState.EMPTY_TEAM;
        if(players.contains(playerData)) {
            return LoginState.ALREADY_LOGGED_IN;
        }
        
        if(!initialized && playerData.getRole().equals(PlayerRole.PLAYER)) {
            return LoginState.NOT_ALLOWED;
        }
        
        if(playerData.getRole().equals(PlayerRole.PLAYER)) {
            if(!isTeamPresented(playerData.getTeam().getTeamName())) return LoginState.NOT_PRESENTED;
            if(Store.getInstance().getGameStage().getStage() == Stage.IN_GAME) {
                PlayerInfo info = getPlayerInfo(playerData);
                if(info == null) return LoginState.NOT_ALLOWED;
                info.data = playerData;
            } else {
                teams.get(playerData.getTeam().getTeamName()).add(new PlayerInfo(playerData, null, player.getUniqueId(), new HashMap<>()));
            }
        }
        players.add(playerData);
        
        Logger.log("Player " + playerData.getLogin() + " has authenticated to the game");
        return LoginState.LOGGED_IN;
    }
    
    public String getPlayerTeam(String playerName) {
        PlayerData data = FindPlayerByName(playerName);
        if(data == null) return "";
        return data.getTeam().getTeamName();
    }
    
    public void DisconnectPlayer(String playerName) {
        PlayerData data = players.stream().filter(p -> p.getLogin().equals(playerName)).findFirst().orElse(null);
        if(data == null) {
            Logger.warn("Player " + playerName + " has not been authenticated to the game");
            return;
        }
        players.remove(data);
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) {
            if (data.getRole().equals(PlayerRole.PLAYER)) {
                teams.get(data.getTeam().getTeamName()).removeIf(playerInfo -> playerInfo.data.getLogin().equals(data.getLogin()));
            }
        }
        Logger.log("Player " + data.getLogin() + " has been disconnected");
    }
    
    public List<PlayerInfo> getTeamPlayers(String teamName) {
        if(!teams.containsKey(teamName)) {
            Logger.warn("Team " + teamName + " does not exist");
            return new ArrayList<>();
        }
        return teams.get(teamName);
    }

    public PlayerData FindPlayerByName(final String name) {
        for(PlayerData player : players) {
            if(player.getLogin().equals(name)) {
                return player;
            }
        }
        return null;
    }
    
    
    public Map<String, Object> getPlayerParams(String playerName) {
        PlayerInfo info = getPlayerInfo(playerName);
        if(info == null) return null;
        return info.params;
    }
    
    public boolean isTeamPresented(String team) {
        return teams.containsKey(team);
    }
    
    public Role getPlayerRole(String playerName) {
        PlayerInfo info = getPlayerInfo(playerName);
        if(info == null) {
            return null;
        }
        return info.getRole();
    }
    
    private PlayerInfo getPlayerInfo(String playerName) {
        PlayerData data = FindPlayerByName(playerName);
        if(data == null || !data.getRole().equals(PlayerRole.PLAYER)) {
            return null;
        }
        return teams.get(data.getTeam().getTeamName())
                .stream().filter(playerInfo -> playerInfo.data.getLogin().equals(data.getLogin()))
                .findFirst().orElse(null);
    }

    private PlayerInfo getPlayerInfo(PlayerData playerData) {
        if(playerData == null) {
            return null;
        }
        return teams.get(playerData.getTeam().getTeamName())
                .stream().filter(playerInfo -> playerInfo.data.getLogin().equals(playerData.getLogin()))
                .findFirst().orElse(null);
    }
    
    public boolean updatePlayerRole(Role role, String playerName) {
        PlayerData playerData = FindPlayerByName(playerName);
        if(role == null) {
            Logger.error("Role is undefined!");
            return false;
        }
        if(playerData == null) {
            Logger.warn("Player " + playerName + " has not been authenticated to the game");
            return false;
        }
        
        PlayerInfo info = getPlayerInfo(playerName);
        if(info == null) {
            Logger.error("Player " + playerName + " has not been found!");
            throw new RuntimeException("Player " + playerName + " has not been found!");
        }
        if(info.getRole() != null && info.getRole().equals(role)) {
            Logger.warn("Role was already has been set to " + playerName);
            return false;
        }
        if(teams.get(playerData.getTeam().getTeamName()).stream().map(PlayerInfo::getRole).anyMatch(role::equals)) {
            Logger.warn("This role has been reserved before!");
            return false;
        }
        
        info.setRole(role);
        return true;
    }
    

    @AllArgsConstructor
    @Getter
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

