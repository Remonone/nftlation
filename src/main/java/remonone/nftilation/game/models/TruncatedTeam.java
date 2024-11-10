package remonone.nftilation.game.models;

import lombok.Builder;
import lombok.Getter;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.game.ingame.objects.Core;
import remonone.nftilation.game.ingame.objects.ICoreData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public class TruncatedTeam implements IModifiableTeam {

    private final UUID uuid = new UUID(50, 10);
    
    private List<PlayerModel> players;
    
    @Override
    public void setTeamActive(boolean value) {
        
    }

    @Override
    public void setCoreAlive(boolean value) {

    }

    @Override
    public Core getCoreInstance() {
        return null;
    }

    @Override
    public UUID getTeamID() {
        return null;
    }

    @Override
    public List<PlayerModel> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public ICoreData getCoreData() {
        return null;
    }

    @Override
    public TeamSpawnPoint getTeamSpawnPoint() {
        TeamSpawnPoint point = new TeamSpawnPoint();
        point.setPosition(ConfigManager.getInstance().getCenterLocation());
        return point;
    }

    @Override
    public boolean isCoreAlive() {
        return false;
    }

    @Override
    public boolean isTeamActive() {
        return false;
    }

    @Override
    public String getTeamName() {
        return "";
    }

    @Override
    public char getTeamColor() {
        return 0;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.emptyMap();
    }

    @Override
    public String getTeamInfo() {
        return "";
    }
}
