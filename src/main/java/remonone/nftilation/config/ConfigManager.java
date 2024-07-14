package remonone.nftilation.config;

import lombok.Getter;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.PropertyConstant;
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
    private List<Location> diamondSpawnList;
    @Getter
    private List<Location> roboSybylsSpawnList;
    @Getter
    private List<Location> ironGolemPositions;
    
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
            teamSpawnPoints = new ArrayList<>();
        }
        Logger.log("Loaded " + teamSpawnPoints.size() + " team spawn points.");
        teamSpawnList = teamSpawnPoints;
        List<Location> diamondPositions = (List<Location>) configuration.getList(PropertyConstant.DIAMOND_POSITION);
        if(diamondPositions == null) {
            diamondPositions = new ArrayList<>();
        }
        diamondSpawnList = diamondPositions;
        centerLocation = (Location) configuration.get(PropertyConstant.CENTER_LOCATION);
        List<Location> roboSybylPoints = (List<Location>) configuration.getList(PropertyConstant.ROBO_SYBYL_SPAWN_POINTS);
        if(roboSybylPoints == null) {
            roboSybylPoints = new ArrayList<>();
        }
        roboSybylsSpawnList = roboSybylPoints;
        List<Location> ironGolemPos = (List<Location>) configuration.getList(PropertyConstant.IRON_GOLEM_SPAWN_POINTS);
        if(ironGolemPos == null) {
            ironGolemPos = new ArrayList<>();
        }
        ironGolemPositions = ironGolemPos;
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

    public void addRoboSybylPoint(Location coords) {
        roboSybylsSpawnList.add(coords);
        SetValue(PropertyConstant.ROBO_SYBYL_SPAWN_POINTS, roboSybylsSpawnList.toArray());
    }
    
    public boolean trySetTeamSpawnCore(String id, Vector pos) {
        TeamSpawnPoint teamPoint = teamSpawnList.stream().filter(point -> point.getId().equals(id)).findFirst().orElse(null);
        if(teamPoint == null) {
            return false;
        }
        teamPoint.setCoreCenter(pos);
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
