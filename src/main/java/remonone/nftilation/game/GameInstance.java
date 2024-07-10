package remonone.nftilation.game;

import lombok.*;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.ingame.core.Core;
import remonone.nftilation.game.ingame.services.RepairCoreService;
import remonone.nftilation.game.ingame.services.SecondTierService;
import remonone.nftilation.game.ingame.services.ServiceContainer;
import remonone.nftilation.game.ingame.services.ThirdTierService;
import remonone.nftilation.game.phase.PhaseCounter;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.utils.EntityList;
import remonone.nftilation.utils.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.*;

public class GameInstance {
    
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    
    @Getter
    private final static GameInstance instance = new GameInstance();
    private Map<String, Team> teamData;
    @Getter
    private PhaseCounter counter;
    
    public void startGame() {
        Map<String, List<DataInstance.PlayerInfo>> teams = Store.getInstance().getDataInstance().getTeams();
        constructTeamData(teams);
        disposePlayers();
        InitServices();
        for(Team team : teamData.values()) {
            createTeamScoreboard(team);
            initPlayerRoles(team.players);
            fillPlayerItems(team.players);
            for(PlayerModel model : team.players) {
                ScoreboardHandler.buildScoreboard(model.reference);
            }
        }
        counter = new PhaseCounter();
        counter.Init();
    }
    
    public boolean checkIfPlayersInSameTeam(Player player1, Player player2) {
        PlayerData data1 = Store.getInstance().getDataInstance().FindPlayerByName(player1.getName());
        PlayerData data2 = Store.getInstance().getDataInstance().FindPlayerByName(player2.getName());
        return data1 != null && data2 != null && data1.getTeam().getTeamName().equals(data2.getTeam().getTeamName());
    }

    private void createTeamScoreboard(Team team) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        org.bukkit.scoreboard.Team registeredTeam = scoreboard.registerNewTeam(team.name);
        team.players.stream().map(PlayerModel::getReference).forEach(player -> registeredTeam.addEntry(player.getName()));
        registeredTeam.setAllowFriendlyFire(false);
        registeredTeam.setCanSeeFriendlyInvisibles(true);
        team.scoreboardTeam = registeredTeam;
        team.players.stream().map(PlayerModel::getReference).forEach(player -> player.setScoreboard(scoreboard));
    }
    
    public void unregisterScoreboardTeams() {
        teamData.values().forEach(team -> team.scoreboardTeam.unregister());
    }

    private void InitServices() {
        ServiceContainer.registerService(new RepairCoreService());
        ServiceContainer.registerService(new SecondTierService());
        ServiceContainer.registerService(new ThirdTierService());
    }


    private void initPlayerRoles(List<PlayerModel> models) {
        models.forEach(info -> Role.UpdatePlayerAbilities(info.reference, Role.getRoleByID(info.roleId), info.getUpgradeLevel()));
    }

    private void constructTeamData(Map<String, List<DataInstance.PlayerInfo>> teams) {
        teamData = new HashMap<>();
        Stack<TeamSpawnPoint> teamList = new Stack<>();
        teamList.addAll(ConfigManager.getInstance().getTeamSpawnList());
        for(Map.Entry<String, List<DataInstance.PlayerInfo>> entry : teams.entrySet()) {
            String teamName = entry.getKey();
            TeamSpawnPoint point = teamList.pop();
            List<PlayerModel> teamPlayers = new ArrayList<>();
            for(DataInstance.PlayerInfo info : entry.getValue()) {
                if(info.getRole() == null) {
                    info.setRole(getRandomRole(entry.getValue()));
                }
                teamPlayers.add(new PlayerModel(getPlayer(info.getPlayerId()), true, 1, info.getRole().getRoleID(), 0, 0, 0));
            }
            Core teamCore = SetCore(teamName, point);
            SpawnShopKeeper(point);
            Team team = new Team(teamPlayers, point, true, true, teamName);
            team.core = teamCore;
            teamData.put(teamName, team);
        }
    }

    private Role getRandomRole(List<DataInstance.PlayerInfo> players) {
        List<String> reservedRoles = players.stream()
                .filter(info -> ObjectUtils.notEqual(info.getRole(), null))
                .map(roleContainer -> roleContainer.getRole().getRoleID())
                .collect(Collectors.toList());
        List<Role> availableRoles = Role.getRoles().stream().filter(role -> !reservedRoles.contains(role.getRoleID())).collect(Collectors.toList());
        return availableRoles.get(RANDOM.nextInt(availableRoles.size()));
    }

    public Iterator<String> getTeamIterator() {
        return teamData.keySet().iterator();
    }
    
    public void awardPlayer(String teamName, Player player, int tokens) {
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        model.tokens += tokens;
        ScoreboardHandler.updateScoreboard(model);
    }
    
    public void withdrawFunds(String teamName, Player player, int amount) {
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        if(model.tokens < amount) return;
        model.tokens -= amount;
        ScoreboardHandler.updateScoreboard(model);
    }
    
    public void increasePlayerKillCounter(String teamName, Player player) {
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        model.killCounter++;
        ScoreboardHandler.updateScoreboard(model);
    }

    public void increasePlayerDeathCounter(String teamName, Player player) {
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        model.deathCounter++;
        ScoreboardHandler.updateScoreboard(model);
    }
    
    public int getTeamPlayersAlive(String teamName) {
        Team team = teamData.get(teamName);
        if(team == null) return 0;
        if(team.isCoreAlive) return team.players.size();
        Collection<PlayerModel> alivePlayers = team.players.stream().filter(playerModel -> playerModel.isAlive).collect(Collectors.toList());
        return alivePlayers.size();
    }

    private void SpawnShopKeeper(TeamSpawnPoint point) {
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location location = point.getShopKeeperPosition().toLocation(world);
        Villager villager = world.spawn(location, Villager.class);
        villager.setAI(false);
        float rot = villager.getLocation().setDirection(point.getShopKeeperPosition().subtract(point.getCoreCenter()).normalize()).getYaw();
        villager.getLocation().setYaw(rot);
        villager.setCustomName("Shop keeper");
        villager.setCustomNameVisible(false);
        villager.setInvulnerable(true);
        EntityList.addEntity(villager);
    }

    private Core SetCore(String teamName, TeamSpawnPoint point) {
        Core core = new Core(teamName, () -> destroyTeam(teamName));
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location location = point.getCoreCenter().toLocation(world);
        location.getBlock().setType(Material.BEACON);
        return core;
    }
    
    public void upgradePlayer(Player player, int level, int price) {
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        PlayerModel model = getPlayerModelFromTeam(data.getTeam().getTeamName(), player);
        if(level - model.upgradeLevel != 1) {
            player.sendMessage(ChatColor.RED + MessageConstant.INCORRECT_UPGRADE_LEVEL);
            awardPlayer(data.getTeam().getTeamName(), player, price);
            return;
        }
        if((int)RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_AVAILABLE_TIER, 1) < level) {
            player.sendMessage(ChatColor.RED + MessageConstant.INCORRECT_STAGE_FOR_UPGRADE);
            awardPlayer(data.getTeam().getTeamName(), player, price);
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, .5f, 1f);
        model.upgradeLevel = level;
        Role role = Role.getRoleByID(model.roleId);
        Role.SetInventoryItems(player, role, level);
        Role.UpdatePlayerAbilities(player, role, level);
        ScoreboardHandler.updateScoreboard(model);
    }

    private void disposePlayers() {
        for(Team team : teamData.values()) {
            team.players.forEach(playerModel -> setPlayerToPosition(team, playerModel.reference));
        }
    }
    
    public void teleportPlayerToBase(String teamName, Player player) {
        Team team = teamData.get(teamName);
        if(team == null) return;
        setPlayerToPosition(team, player);
    }
    
    private void setPlayerToPosition(Team team, Player player) {
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location position = team.spawnPoint.getPosition().toLocation(world);
        player.teleport(position);
    }

    private void fillPlayerItems(List<PlayerModel> players) {
        players.forEach(info -> Role.refillInventoryWithItems(info.reference, Role.getRoleByID(info.roleId), info.upgradeLevel));
    }

    public TeamData getTeamByCorePosition(Vector position) {
        for(Team point : teamData.values()) {
            if(position.isInSphere(point.spawnPoint.getCoreCenter(), 1.0D)) {
                return point.core.getTeamData();
            }
        }
        return null;
    }
    
    public List<PlayerModel> getTeamPlayers(String teamName) {
        return this.teamData.get(teamName).players;
    } 
    
    public boolean damageCore(String teamName) {
        Team team = teamData.get(teamName);
        if(team == null) return false;
        boolean isDamaged = team.core.TakeDamage();
        if(isDamaged) {
            team.players.forEach(ScoreboardHandler::updateScoreboard);
        }
        return isDamaged;
    }
    
    public void healCore(Player player, String teamName, int price) {
        Team team = teamData.get(teamName);
        if(team == null) return;
        if(team.core == null) return;
        if(!team.core.Heal()) {
            awardPlayer(teamName, player, price);
            player.sendMessage(ChatColor.RED + MessageConstant.CANNOT_HEAL_CORE);
            return;
        }
        team.players.forEach(ScoreboardHandler::updateScoreboard);
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
    }

    private void destroyTeam(String teamName) {
        Team team = teamData.get(teamName);
        team.isCoreAlive = false;
        
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location location = team.spawnPoint.getCoreCenter().toLocation(world);
        location.getBlock().setType(Material.AIR);
        notifyDestruction(team);
        team.core = null;
        for(Team activeTeam : teamData.values()) {
            activeTeam.players.stream().filter(playerModel -> activeTeam.isCoreAlive || playerModel.isAlive).forEach(ScoreboardHandler::updateScoreboard);
        }
    }

    private void notifyDestruction(Team team) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            String title;
            String subTitle;
            if(team.players.stream().anyMatch(playerModel -> playerModel.reference.getUniqueId().equals(player.getUniqueId()))) {
                title = ChatColor.RED + "" + ChatColor.BOLD + MessageConstant.CORE_DESTROYED_TITLE;
                subTitle = ChatColor.GOLD + MessageConstant.CORE_DESTROYED_SUBTITLE;
            } else {
                title = ChatColor.GREEN + "" + ChatColor.BOLD + String.format(MessageConstant.OTHER_CORE_DESTROYED_TITLE, team.core.getTeamData().getTeamName());
                subTitle = ChatColor.WHITE + MessageConstant.OTHER_CORE_DESTROYED_SUBTITLE;
            }
            player.sendTitle(title, subTitle, 3, 10, 3);
        });
        Logger.broadcast(ChatColor.GOLD + String.format(MessageConstant.CORE_DESTROYED_BROADCAST, team.core.getTeamData().getTeamName()));
    }
    
    public PlayerModel getPlayerModelFromTeam(String teamName, Player player) {
        Team team = teamData.get(teamName);
        if(team == null) return null;
        return team.getPlayers().stream().filter(playerModel -> playerModel.reference.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }
    
    public boolean isTeamAlive(String teamName) {
        return teamData.get(teamName).isCoreAlive;
    }
    
    public int getCoreHealth(String teamName) {
        Team team = teamData.get(teamName);
        if(team == null) return 0;
        return team.core.getHealth();
    }
    
    public void setPlayerDead(String teamName, UUID playerId) {
        Team team = teamData.get(teamName);
        if(team == null) return;
        PlayerModel model = team.players.stream().filter(playerModel -> playerModel.reference.getUniqueId().equals(playerId)).findFirst().orElse(null);
        if(model == null) return;
        model.isAlive = false;
        if(!team.isCoreAlive) {
            if(team.players.stream().noneMatch(playerModel -> playerModel.isAlive)) {
                team.isActive = false;
            }
            teamData.values().stream().filter(t -> t.isActive).forEach(temp -> temp.players.forEach(ScoreboardHandler::updateScoreboard));
        }
    }
    
    public void respawnPlayer(Player player, String teamName) {
        Team team = teamData.get(teamName);
        if(team == null) return;
        if(team.isCoreAlive) {
            setPlayerToPosition(team, player);
            PlayerModel model = team.players.stream().filter(playerModel -> playerModel.reference.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
            if(model == null) return;
            model.isAlive = true;
            Role.UpdatePlayerAbilities(player, Role.getRoleByID(model.getRoleId()), model.getUpgradeLevel());
        }
        
    }
    
    public boolean isTeamActive(String teamName) {
        Team team = teamData.get(teamName);
        if(team == null) return false;
        return team.isActive;
    }

    @Getter
    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class Team {
        private UUID uuid = new UUID(50, 10);
        @NonNull
        private List<PlayerModel> players;
        private Core core;
        @NonNull
        private TeamSpawnPoint spawnPoint;
        @NonNull
        private boolean isCoreAlive;
        @NonNull
        private boolean isActive;
        @NonNull
        private String name;
        private org.bukkit.scoreboard.Team scoreboardTeam;
    }
    
    @Getter
    @AllArgsConstructor
    public static class PlayerModel {
        @Setter
        private Player reference;
        private boolean isAlive;
        @Setter
        private int upgradeLevel;
        private String roleId;
        private int tokens;
        private int killCounter;
        private int deathCounter;
    }
}
