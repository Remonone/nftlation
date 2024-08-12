package remonone.nftilation.game.models;

import lombok.Builder;
import org.bukkit.ChatColor;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.game.ingame.core.Core;
import remonone.nftilation.game.ingame.core.ICoreData;

import java.util.List;
import java.util.UUID;

@Builder
public class TeamImpl implements IModifiableTeam {
    
    private final UUID uuid = new UUID(50, 10);
    private boolean isCoreAlive;
    private boolean isTeamActive;
    private List<PlayerModel> players;
    private TeamSpawnPoint spawnPoint;
    private Core core;
    private String teamName;
    private ChatColor teamColor;
    
    @Override
    public void setTeamActive(boolean value) {
        this.isTeamActive = value;
    }

    @Override
    public void setCoreAlive(boolean value) {
        this.isCoreAlive = value;
    }

    @Override
    public Core getCoreInstance() {
        return core;
    }

    @Override
    public UUID getTeamID() {
        return uuid;
    }

    @Override
    public List<PlayerModel> getPlayers() {
        return players;
    }

    @Override
    public ICoreData getCoreData() {
        return core;
    }

    @Override
    public TeamSpawnPoint getTeamSpawnPoint() {
        return spawnPoint;
    }

    @Override
    public boolean isCoreAlive() {
        return isCoreAlive;
    }

    @Override
    public boolean isTeamActive() {
        return isTeamActive;
    }

    @Override
    public String getTeamName() {
        return teamName;
    }

    @Override
    public char getTeamColor() {
        return teamColor.getChar();
    }
}
