package remonone.nftilation.game;

import javafx.util.Pair;
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
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.utils.Logger;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class DataInstance {
        
    public DataInstance() {}
    
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
    
    private final Map<UUID, PlayerInfo> infos = new HashMap<>();
    @Getter
    private final List<TeamData> teamData = new ArrayList<>();
    
    @Getter
    private final Map<String, List<PlayerInfo>> teams = new HashMap<>();
    
    public LoginState tryAddPlayerToGame(PlayerData playerData, Player player) {
        if(playerData.getRole().equals(PlayerRole.PLAYER) && playerData.getTeam().getTeamName().isEmpty()) return LoginState.EMPTY_TEAM;
        if(!initialized && playerData.getRole().equals(PlayerRole.PLAYER)) {
            return LoginState.NOT_ALLOWED;
        }
        if(FindPlayerByID(player.getUniqueId()) != null) {
            return LoginState.ALREADY_LOGGED_IN;
        }
        Pair<LoginState, PlayerInfo> registerState = registerPlayerInfo(playerData, player);
        if(!LoginState.LOGGED_IN.equals(registerState.getKey())) return registerState.getKey();
        Logger.log("Adding player " + player.getName() + " to game");
        players.add(registerState.getValue());
        return LoginState.LOGGED_IN;
    }

    private Pair<LoginState, PlayerInfo> registerPlayerInfo(PlayerData playerData, Player player) {
        if(!playerData.getRole().equals(PlayerRole.PLAYER)) {
            return new Pair<>(LoginState.LOGGED_IN, new PlayerInfo(playerData, null, null, player.getUniqueId()));
        }
        if(!isTeamPresented(playerData.getTeam().getTeamName())) return new Pair<>(LoginState.NOT_PRESENTED, null);
        if(Store.getInstance().getGameStage().getStage() == Stage.IN_GAME) {
            PlayerInfo info = getPlayerInfo(playerData);
            if(info == null) return new Pair<>(LoginState.NOT_ALLOWED, null);
            info.data = playerData;
            return new Pair<>(LoginState.LOGGED_IN, info);
        } else {
            PlayerInfo info = new PlayerInfo(playerData, null, null, player.getUniqueId());
            teams.get(playerData.getTeam().getTeamName()).add(info);
            return new Pair<>(LoginState.LOGGED_IN, info);
        }
    }

    public String getPlayerTeam(UUID playerId) {
        PlayerInfo info = FindPlayerByID(playerId);
        if(info == null || info.getData() == null || info.getData().getTeam() == null) return "";
        return info.getData().getTeam().getTeamName();
    }
    
    public void disconnectPlayer(UUID playerId) {
        PlayerInfo info = players.stream().filter(p -> p.getPlayerId().equals(playerId)).findFirst().orElse(null);
        if(info == null) {
            Logger.warn("Player " + playerId + " has not been authenticated to the game");
            return;
        }
        players.remove(info);
        infos.remove(playerId);
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

    public PlayerInfo FindPlayerByID(final UUID playerId) {
        if(infos.containsKey(playerId)) {
            return infos.get(playerId);
        }
        for(PlayerInfo player : players) {
            if(player.getPlayerId().equals(playerId)) {
                infos.put(playerId, player);
                return player;
            }
        }
        return null;
    }
    
    public Player getPlayerByLogin(final String login) {
        for(PlayerInfo playerInfo : players) {
            if(playerInfo.getData().getLogin().equals(login)) {
                return getServer().getPlayer(playerInfo.getPlayerId());
            }
        }
        Logger.warn("Player with login " + login + " not found");
        return null;
    }
    
    public boolean isTeamPresented(String team) {
        return teams.containsKey(team);
    }
    
    public Role getPlayerRole(UUID playerId) {
        PlayerInfo info = FindPlayerByID(playerId);
        if(info == null) {
            return null;
        }
        return info.getRole();
    }

    public Rune getPlayerRune(UUID playerId) {
        PlayerInfo info = FindPlayerByID(playerId);
        if(info == null) {
            return null;
        }
        return info.getRune();
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
        PlayerInfo info = FindPlayerByID(playerId);
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
    
    public boolean updatePlayerRune(Rune rune, UUID playerId) {
        PlayerInfo info = FindPlayerByID(playerId);
        if(info == null) {
            Logger.warn("Player " + playerId + " has not been authenticated to the game");
            return false;
        }
        if(rune == null) {
            Logger.warn("Rune is undefined!");
            return false;
        }
        info.setRune(rune);
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
        private Rune rune;
        @Setter
        private UUID playerId;
    }
    
    
}

