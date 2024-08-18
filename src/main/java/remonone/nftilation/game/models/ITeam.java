package remonone.nftilation.game.models;

import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.game.ingame.core.ICoreData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ITeam {
    UUID getTeamID();
    List<PlayerModel> getPlayers();
    ICoreData getCoreData();
    TeamSpawnPoint getTeamSpawnPoint();
    boolean isCoreAlive();
    boolean isTeamActive();
    String getTeamName();
    char getTeamColor();
    Map<String, Object> getParameters();
}
