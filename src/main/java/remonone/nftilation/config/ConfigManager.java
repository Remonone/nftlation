package remonone.nftilation.config;

import lombok.Getter;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.models.PhaseProps;
import remonone.nftilation.hints.Hint;
import remonone.nftilation.utils.Logger;

import java.io.File;
import java.util.*;

public class ConfigManager {
    
    @Getter
    private final static ConfigManager instance = new ConfigManager();
    
    private File file;
    private YamlConfiguration configuration;
    
    // CONFIGS
    
    @Getter
    private Vector adminRoomCoords;
    @Getter
    private Vector lobbyRoomCoords;
    @Getter
    private Vector centerDeadZoneCoords;
    @Getter
    private List<TeamSpawnPoint> teamSpawnList;
    @Getter
    private Location centerLocation;
    @Getter
    private double fragilityScale;
    
    @Getter
    private List<Location> diamondSpawnList;
    @Getter
    private List<Location> roboSybilsSpawnList;
    @Getter
    private List<Location> ironGolemPositions;
    @Getter
    private List<Hint> hints;
    @Getter
    private List<PhaseProps> phaseProps;
    
    
    private ConfigManager() {}
    
    public void Load() {
        Logger.log("Loading configs...");
        file = new File(Nftilation.getInstance().getDataFolder(), "config.yml");
        if(!file.exists()) {
            Nftilation.getInstance().saveResource("config.yml", false);
        }
        
        configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch(Exception e) {
            Logger.error("Error during loading a configurations: " + e.getMessage());
            return;
        }
        
        LoadData();
    }
    
    @SuppressWarnings("unchecked")
    private void LoadData() {
        adminRoomCoords = (Vector) configuration.get(PropertyConstant.ADMIN_ROOM);
        lobbyRoomCoords = (Vector) configuration.get(PropertyConstant.LOBBY_ROOM);
        centerDeadZoneCoords = (Vector) configuration.get(PropertyConstant.CENTER_DEAD_POINT);
        List<TeamSpawnPoint> teamSpawnPoints = (List<TeamSpawnPoint>) configuration.getList(PropertyConstant.TEAMS_SPAWN_POINTS);
        if(teamSpawnPoints == null) {
            teamSpawnPoints = Collections.emptyList();
        }
        Logger.log("Loaded " + teamSpawnPoints.size() + " team spawn points.");
        teamSpawnList = teamSpawnPoints;
        List<Location> diamondPositions = (List<Location>) configuration.getList(PropertyConstant.DIAMOND_POSITION);
        if(diamondPositions == null) {
            diamondPositions = Collections.emptyList();
        }
        diamondSpawnList = diamondPositions;
        centerLocation = (Location) configuration.get(PropertyConstant.CENTER_LOCATION);
        List<Location> roboSybilPoints = (List<Location>) configuration.getList(PropertyConstant.ROBO_SYBIL_SPAWN_POINTS);
        if(roboSybilPoints == null) {
            roboSybilPoints = Collections.emptyList();
        }
        roboSybilsSpawnList = roboSybilPoints;
        List<Location> ironGolemPos = (List<Location>) configuration.getList(PropertyConstant.IRON_GOLEM_SPAWN_POINTS);
        if(ironGolemPos == null) {
            ironGolemPos = Collections.emptyList();
        }
        ironGolemPositions = ironGolemPos;
        fragilityScale = configuration.getDouble(PropertyConstant.FRAGILITY_SCALE);
        List<Hint> hintList = (List<Hint>) configuration.getList(PropertyConstant.HINTS);
        if(hintList == null) {
            hintList = Collections.emptyList();
        }
        hints = hintList;
        List<PhaseProps> phases = (List<PhaseProps>)configuration.getList(PropertyConstant.PHASES);
        if(phases == null) {
            phases = Collections.emptyList();
        }
        phaseProps = phases;
    }

    public void Save() {
        Logger.log("Saving configs...");
        try {
            configuration.save(file);
        } catch(Exception e) {
            Logger.error("Cannot save the configs due to next reason: " + e.getMessage());
        }
    }
    
    public void setAdminRoomCoords(Vector coords) {
        adminRoomCoords = coords;
        SetValue(PropertyConstant.ADMIN_ROOM, adminRoomCoords);
    }
    
    public void setLobbyRoomCoords(Vector coords) {
        lobbyRoomCoords = coords;
        SetValue(PropertyConstant.LOBBY_ROOM, lobbyRoomCoords);
    }
    
    public void setCenterDeadZoneCoords(Vector coords) {
        centerDeadZoneCoords = coords;
        SetValue(PropertyConstant.CENTER_DEAD_POINT, centerDeadZoneCoords);
    }
    
    public void addDiamondsSpawnPoint(Location coords) {
        diamondSpawnList.add(coords);
        SetValue(PropertyConstant.DIAMOND_POSITION, diamondSpawnList);
    }
    
    public void addIronGolemPos(Location loc) {
        ironGolemPositions.add(loc);
        SetValue(PropertyConstant.IRON_GOLEM_SPAWN_POINTS, ironGolemPositions);
    }
    
    public void addHint(Hint hint) {
        hints.add(hint);
        SetValue(PropertyConstant.HINTS, hints);
    }
    
    public String addTeamSpawnPosition(Location coords) {
        TeamSpawnPoint point = new TeamSpawnPoint();
        point.setId(UUID.randomUUID().toString().substring(0, 5));
        point.setPosition(coords);
        teamSpawnList.add(point);
        SetValue(PropertyConstant.TEAMS_SPAWN_POINTS, teamSpawnList.toArray());
        return point.getId();
    }
    public void removeTeamSpawnPosition(String id) {
        teamSpawnList.removeIf(point -> point.getId().equals(id));
        SetValue(PropertyConstant.TEAMS_SPAWN_POINTS, teamSpawnList.toArray());
    }

    public void addRoboSybilPoint(Location coords) {
        roboSybilsSpawnList.add(coords);
        SetValue(PropertyConstant.ROBO_SYBIL_SPAWN_POINTS, roboSybilsSpawnList.toArray());
    }
    
    public boolean trySetTeamSpawnCore(String id, Location pos) {
        TeamSpawnPoint teamPoint = teamSpawnList.stream().filter(point -> point.getId().equals(id)).findFirst().orElse(null);
        if(teamPoint == null) {
            return false;
        }
        teamPoint.setCoreCenter(pos);
        SetValue(PropertyConstant.TEAMS_SPAWN_POINTS, teamSpawnList.toArray());
        return true;
    }
    
    public boolean trySetTeamAirDropPosition(String id, Location pos) {
        TeamSpawnPoint teamSpawnPoint = teamSpawnList.stream().filter(point -> point.getId().equals(id)).findFirst().orElse(null);
        if(teamSpawnPoint == null) return false;
        teamSpawnPoint.setAirDropPosition(pos);
        SetValue(PropertyConstant.TEAMS_SPAWN_POINTS, teamSpawnList.toArray());
        return true;
    }
    
    public boolean trySetShopKeeper(String id, Location pos) {
        TeamSpawnPoint teamPoint = teamSpawnList.stream().filter(point -> point.getId().equals(id)).findFirst().orElse(null);
        if(teamPoint == null) {
            return false;
        }
        teamPoint.setShopKeeperPosition(pos);
        SetValue(PropertyConstant.TEAMS_SPAWN_POINTS, teamSpawnList.toArray());
        return true;
    }
    
    private void SetValue(String path, Object value) {
        configuration.set(path, value);
        Save();
    }
    
    public void setCenterLocation(Location loc) {
        centerLocation = loc;
        SetValue(PropertyConstant.CENTER_LOCATION, centerLocation);
    }
    
    public boolean isPositionExisting(String id) {
        return ObjectUtils.notEqual(teamSpawnList
                .stream()
                .filter(teamSpawnPoint -> teamSpawnPoint.getId().equals(id))
                .findFirst()
                .orElse(null), null);
    }
    
    public boolean trySetCheckerPosition(String id, Location pos) {
        TeamSpawnPoint teamPoint = teamSpawnList.stream().filter(point -> point.getId().equals(id)).findFirst().orElse(null);
        if(teamPoint == null) {
            return false;
        }
        teamPoint.setCheckerChestPosition(pos);
        SetValue(PropertyConstant.TEAMS_SPAWN_POINTS, teamSpawnList.toArray());
        return true;
    }
    
    
    public int positionsSize() {
        return teamSpawnList.size();
    }
}
