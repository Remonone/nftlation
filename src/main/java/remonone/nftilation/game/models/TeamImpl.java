package remonone.nftilation.game.models;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.ChatColor;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.game.ingame.objects.Core;
import remonone.nftilation.game.ingame.objects.ICoreData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public class TeamImpl implements IModifiableTeam {
    
    private final UUID uuid = new UUID(50, 10);
    @Getter
    private boolean isCoreAlive;
    @Getter
    private boolean isTeamActive;
    @Getter
    private List<PlayerModel> players;
    private TeamSpawnPoint spawnPoint;
    private Core core;
    @Getter
    private String teamName;
    private ChatColor teamColor;
    @Getter
    private Map<String, Object> parameters;
    
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
    public ICoreData getCoreData() {
        return core;
    }

    @Override
    public TeamSpawnPoint getTeamSpawnPoint() {
        return spawnPoint;
    }
    
    @Override
    public char getTeamColor() {
        return teamColor.getChar();
    }
}
