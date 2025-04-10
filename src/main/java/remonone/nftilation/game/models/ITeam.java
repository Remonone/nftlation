package remonone.nftilation.game.models;

import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.game.ingame.objects.ICoreData;

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
    boolean isUILayoutExists();
    String getTeamName();
    char getTeamColor();
    Map<String, Object> getParameters();
    String getTeamInfo();
}
