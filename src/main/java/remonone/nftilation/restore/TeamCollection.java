package remonone.nftilation.restore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@SerializableAs("TeamCollection")
public class TeamCollection implements ConfigurationSerializable, Cloneable {

    private final static String ID = "id";
    private final static String CORE_HEALTH = "coreHealth";
    private final static String TEAM_POINT_ID = "teamPointId";
    private final static String TEAM_NAME = "teamName";
    private final static String TEAM_COLOR = "teamColor";
    private final static String TEAM_ALIVE = "teamAlive";
    private final static String TEAM_ACTIVE = "teamActive";
    private final static String TEAM_PLAYERS = "teamPlayers";


    private final String id;
    private final List<String> playerNames;
    private final int coreHealth;
    private final String teamPointId;
    private final String teamName;
    private final char teamColor;
    private final boolean teamAlive;
    private final boolean teamActive;

    public TeamCollection(ITeam team) {
        this.id = team.getTeamID().toString();
        this.coreHealth = team.getCoreData().getHealth();
        this.teamPointId = team.getTeamSpawnPoint().getId();
        this.teamName = team.getTeamName();
        this.teamColor = team.getTeamColor();
        this.teamAlive = team.isCoreAlive();
        this.teamActive = team.isTeamActive();
        DataInstance instance = Store.getInstance().getDataInstance();
        this.playerNames = team.getPlayers().stream()
                .map(PlayerModel::getReference)
                .map(Player::getUniqueId)
                .map(instance::FindPlayerByName)
                .map(DataInstance.PlayerInfo::getData)
                .map(PlayerData::getLogin)
                .collect(Collectors.toList());

    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> info = new HashMap<>();
        info.put(ID, getId());
        info.put(CORE_HEALTH, getCoreHealth());
        info.put(TEAM_POINT_ID, getTeamPointId());
        info.put(TEAM_NAME, getTeamName());
        info.put(TEAM_COLOR, getTeamColor());
        info.put(TEAM_ACTIVE, isTeamActive());
        info.put(TEAM_ALIVE, isTeamAlive());
        return info;
    }

    @SuppressWarnings("unchecked")
    public static TeamCollection deserialize(Map<String, Object> map) {
        UUID id;
        String name = "";
        char teamColor = '\0';
        boolean teamAlive = false;
        boolean teamActive = false;
        String point = "";
        List<String> players;
        if(!map.containsKey(ID)) {
            Logger.error("Failed deserialization of team!");
            return null;
        }
        id = (UUID) map.get(ID);
        if(map.containsKey(TEAM_POINT_ID)) {
            point = (String)map.get(TEAM_POINT_ID);
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
        if(map.containsKey(TEAM_PLAYERS)) {
            players = (List<String>) map.get(TEAM_PLAYERS);
        } else {
            players = new ArrayList<>();
        }
        return TeamCollection.builder()
                .id(id.toString())
                .teamName(name)
                .teamColor(teamColor)
                .teamPointId(point)
                .teamActive(teamActive)
                .teamAlive(teamAlive)
                .playerNames(players)
                .build();
    }

    public static TeamCollection getCollectionFromTeam(ITeam team) {
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
