package remonone.nftilation.game;

import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.IComponent;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.*;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.events.OnCoreDamageEvent;
import remonone.nftilation.game.models.*;
import remonone.nftilation.game.phase.PhaseCounter;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.restore.DumpCollector;
import remonone.nftilation.utils.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class GameInstance {

    @Getter
    private final static GameInstance instance = new GameInstance();
    private Map<String, IModifiableTeam> teamData;
    private final Set<ITeam> teamRaw = new HashSet<>();
    @Getter
    private PhaseCounter counter;

    private static Map<String, IComponent> components;
    private int repeatedTask;
    private BossReservation reservation;

    @Getter
    private boolean isFinished;
    
    public void startGame() {
        Map<String, List<DataInstance.PlayerInfo>> teams = Store.getInstance().getDataInstance().getTeams();
        teamData = GameConfiguration.constructTeamData(teams);
        
        teamRaw.addAll(teamData.values());
        GameConfiguration.disposePlayers(teamRaw);
        GameConfiguration.initServices();
        GameConfiguration.spawnGolems();
        GameConfiguration.initGlobalEvents();

        for(ITeam team : teamData.values()) {
            GameConfiguration.initPlayerRoles(team);
            GameConfiguration.initPlayerRunes(team);
            team.getPlayers().forEach(Role::refillInventoryWithItems);
            for(PlayerModel model : team.getPlayers()) {
                ScoreboardHandler.buildScoreboard(model.getReference());
            }
        }
        if(counter == null) {
            counter = new PhaseCounter();
            counter.init();
        }
        initComponents();
        this.repeatedTask = GameConfiguration.startRepeatedTask();
        this.reservation = new BossReservation();
        GameConfiguration.initHints();
        setRecoveryDataCollector();
    }

    public void initTimer(int i, int i1) {
        if(counter != null) {
            counter.stop();
            counter = null;
        }
        counter = new PhaseCounter();
        counter.init(i, i1);
    }
    
    public void setTeamData(List<IModifiableTeam> teams) {
        for(IModifiableTeam team : teamData.values()) {
            OnCoreDamageEvent.getHandlerList().unregister(team.getCoreInstance());
        }
        teamRaw.clear();
        teamRaw.addAll(teams);
        teamData.clear();
        for(IModifiableTeam team : teams) {
            teamData.put(team.getTeamName(), team);
        }
    }

    private void setRecoveryDataCollector() {
        new BukkitRunnable() {
            @Override
            public void run() {
                DumpCollector.generateDump();
            }
        }.runTaskTimer(Nftilation.getInstance(), 10 * DataConstants.TICKS_IN_MINUTE, 10 * DataConstants.TICKS_IN_MINUTE);
    }
    

    private void initComponents() {
        components = new HashMap<>();
        IComponent playerInteract = instantiateComponent();
        components.put(playerInteract.getName(), playerInteract);
    }

    private IComponent instantiateComponent() {
        try {
            IComponent comp = ((Class<? extends IComponent>) PlayerInteractComponent.class).newInstance();
            comp.initComponent();
            return comp;
        } catch(Exception ex) {
            Logger.error("Cannot instantiate component " + PlayerInteractComponent.class.getName());
            throw new RuntimeException("Cannot instantiate component " + PlayerInteractComponent.class.getName(), ex);
        }
    }

    public static IComponent getComponentByName(String name) {
        return components.get(name);
    }

    public boolean checkIfPlayersInSameTeam(Player player1, Player player2) {
        String data1 = Store.getInstance().getDataInstance().getPlayerTeam(player1.getUniqueId());
        String data2 = Store.getInstance().getDataInstance().getPlayerTeam(player2.getUniqueId());
        return !StringUtils.isEmpty(data1) && !StringUtils.isEmpty(data2) && data1.equals(data2);
    }

    public Iterator<ITeam> getTeamIterator() {
        return teamRaw.iterator();
    }
    
    public ITeam getTeam(String teamName) {
        if(teamName == null) return null;
        return teamData.getOrDefault(teamName, null);
    }
    
    public void teleportPlayerToBase(String teamName, Player player) {
        ITeam team = teamData.get(teamName);
        if(team == null) return;
        player.teleport(team.getTeamSpawnPoint().getPosition());
    }


    public ITeam getTeamByCorePosition(Location position) {
        for(ITeam team : teamData.values()) {
            if(position.getBlock().equals(team.getTeamSpawnPoint().getCoreCenter().getBlock())) {
                return team;
            }
        }
        return null;
    }

    public void checkOnActiveTeams() {
        List<ITeam> aliveTeams = teamData.values().stream().filter(ITeam::isTeamActive).collect(Collectors.toList());
        if(aliveTeams.size() < 2 && !isFinished) {
            isFinished = true;
            announceTeamWinner(aliveTeams.get(0));
        }
    }

    private void announceTeamWinner(ITeam team) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColor.GREEN + "Команда " + team.getTeamName() + " победили в режиме Nftlation! Поздравляем!"));
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                team.getPlayers().forEach(model -> {
                    Player player = model.getReference();
                    Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
                    FireworkMeta fireworkMeta = firework.getFireworkMeta();
                    fireworkMeta.setPower(5);
                    fireworkMeta.addEffect(FireworkEffect.builder().flicker(true).trail(true).withColor(ColorUtils.TranslateToColor(ChatColor.getByChar(team.getTeamColor()))).with(FireworkEffect.Type.BALL_LARGE).build());
                    firework.setFireworkMeta(fireworkMeta);
                });
            }
        };
        task.runTaskTimer(Nftilation.getInstance(), 0L, 40L);
        Bukkit.getScheduler().cancelTask(repeatedTask);
    }
    
    public PlayerModel getPlayerModelFromTeam(String teamName, Player player) {
        if(teamData == null) return null;
        ITeam team = teamData.get(teamName);
        if(team == null) return null;
        return team.getPlayers().stream().filter(playerModel -> playerModel.getReference().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }
    
    
    public void setPlayerDead(String teamName, Player player) {
        IModifiableTeam team = teamData.get(teamName);
        if(team == null) return;
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        if(model == null) return;
        Map<String, Object> params = model.getParameters();
        params.put(PropertyConstant.PLAYER_IS_ALIVE_PARAM, false);
        if(!team.isCoreAlive()) {
            if(team.getPlayers().stream().noneMatch(playerModel -> (Boolean)playerModel.getParameters().getOrDefault(PropertyConstant.PLAYER_IS_ALIVE_PARAM, false))) {
                team.setTeamActive(false);
            }
            teamData.values().forEach(temp -> temp.getPlayers().forEach(ScoreboardHandler::updateScoreboard));
        }
        checkOnActiveTeams();
    }
    
    public void respawnPlayer(Player player, String teamName) {
        ITeam team = teamData.get(teamName);
        if(team == null) return;
        if(team.isCoreAlive()) {
            Vector vector = VectorUtils.getRandomPosInCircle(VectorUtils.ZERO, 5);
            Location location = team.getTeamSpawnPoint().getPosition();
            location.add(vector);
            Location loc = BlockUtils.getNearestEmptySpace(location.getBlock(), 2);
            player.teleport(loc);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 10));
            PlayerModel model = getPlayerModelFromTeam(teamName, player);
            if(model == null) return;
            model.getParameters().put(PropertyConstant.PLAYER_IS_ALIVE_PARAM, true);
            Role.updatePlayerAbilities(model);
        }
        
    }

    public boolean setHealth(Player applicant, String teamName, int health) {
        if(applicant == null) return false;
        DataInstance.PlayerInfo applicantInfo = Store.getInstance().getDataInstance().FindPlayerByID(applicant.getUniqueId());
        if(applicantInfo == null || applicantInfo.getData() == null || !applicantInfo.getData().getRole().equals(PlayerRole.ADMIN)) return false;
        if(health < 0 || health > 100) return false;
        for(IModifiableTeam team : teamData.values()) {
            if(team.getTeamName().equals(teamName)) {
                if(!team.isCoreAlive()) return false;
                team.getCoreInstance().setHealth(health);
                team.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
                return true;
            }
        }
        return false;
    }

    public boolean addBossTeam() {
        if(reservation.reservationID == null) return false;
        Player player = getServer().getPlayer(reservation.reservationID);
        IModifiableTeam team = GameConfiguration.createShallowTeam(Collections.singletonList(player), PropertyConstant.TEAM_BOSS);
        teamRaw.add(team);
        teamData.put(team.getTeamName(), team);
        return true;
    }
    
    public boolean setReservation(UUID player) {
        if(reservation.reservationID != null) return false;
        reservation.reservationID = player;
        return true;
    }
    
    public void removeTeam(String teamName) {
        ITeam team = teamData.get(teamName);
        if(team == null) return;
        teamData.remove(teamName);
        teamRaw.remove(team);
    }
    
    @Getter
    @Setter
    private static class BossReservation {
        private UUID reservationID;
        public BossReservation() {
            reservationID = null;
        }
    }
}
