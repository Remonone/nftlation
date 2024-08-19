package remonone.nftilation.restore;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.game.ingame.core.Core;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.TeamImpl;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamCollection implements ConfigurationSerializable, Cloneable {

    private final static String ID = "id";
    private final static String CORE_HEALTH = "coreHealth";
    private final static String TEAM_POINT_ID = "teamPointId";
    private final static String TEAM_NAME = "teamName";
    private final static String TEAM_COLOR = "teamColor";
    private final static String TEAM_ALIVE = "teamAlive";
    private final static String TEAM_ACTIVE = "teamActive";

    private final ITeam team;

    public TeamCollection(ITeam team) {
        this.team = team;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> info = new HashMap<>();
        info.put(ID, team.getTeamID());
        info.put(CORE_HEALTH, team.getCoreData().getHealth());
        info.put(TEAM_POINT_ID, team.getTeamSpawnPoint().getId());
        info.put(TEAM_NAME, team.getTeamName());
        info.put(TEAM_COLOR, team.getTeamColor());
        info.put(TEAM_ACTIVE, team.isTeamActive());
        info.put(TEAM_ALIVE, team.isCoreAlive());
        return info;
    }

    public static TeamCollection deserialize(Map<String, Object> map) {
        UUID id;
        String name = "";
        char teamColor = '\0';
        boolean teamAlive = false;
        boolean teamActive = false;
        TeamSpawnPoint point = null;
        if(!map.containsKey(ID)) {
            Logger.error("Failed deserialization of team!");
            return null;
        }
        id = (UUID) map.get(ID);
        if(map.containsKey(TEAM_POINT_ID)) {
            point = ConfigManager.getInstance().getTeamSpawnList().stream().filter(temp -> temp.getId().equals((String)map.get(TEAM_POINT_ID))).findFirst().orElse(null);
        }
        if(map.containsKey(TEAM_NAME)) {
            name = (String) map.get(TEAM_NAME);
        }
        if(map.containsKey(TEAM_COLOR)) {
            teamColor = (char) map.get(TEAM_COLOR);
        }
        if(map.containsKey(TEAM_ALIVE)) {
            teamAlive = (boolean) map.get(TEAM_ALIVE);
        }
        if(map.containsKey(TEAM_ACTIVE)) {
            teamActive = (boolean) map.get(TEAM_ACTIVE);
        }
        ITeam team = TeamImpl.builder()
                .teamName(name)
                .isTeamActive(teamActive)
                .isCoreAlive(teamAlive)
                .teamColor(ChatColor.getByChar(teamColor))
                .spawnPoint(point)
                .build();
        return new TeamCollection(team);
    }

    @Override
    public TeamCollection clone() {
        try {
            return (TeamCollection) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
