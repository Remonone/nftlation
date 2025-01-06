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
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.game.models.*;
import remonone.nftilation.game.phase.PhaseCounter;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.handlers.OnEntityDieHandler;
import remonone.nftilation.utils.*;

import java.util.*;
import java.util.function.Function;
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
        teamData = GameConfiguration.constructTeamData(teams, destroyTeam);
        
        teamRaw.addAll(teamData.values());
        GameConfiguration.disposePlayers(teamRaw);
        GameConfiguration.initServices();
        GameConfiguration.spawnGolems();
        counter = new PhaseCounter();
        counter.Init();
        for(ITeam team : teamData.values()) {
            GameConfiguration.initPlayerRoles(team);
            GameConfiguration.initPlayerRunes(team);
            team.getPlayers().forEach(Role::refillInventoryWithItems);
            for(PlayerModel model : team.getPlayers()) {
                ScoreboardHandler.buildScoreboard(model.getReference());
            }
        }
        initComponents();
        this.repeatedTask = GameConfiguration.startRepeatedTask();
        this.reservation = new BossReservation();
        GameConfiguration.initHints();
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

    public final Function<String, Void> destroyTeam = (String teamName) -> {
        IModifiableTeam team = teamData.get(teamName);
        team.setCoreAlive(false);
        Location location = team.getTeamSpawnPoint().getCoreCenter();
        location.getBlock().setType(Material.AIR);
        notifyDestruction(team);

        if(!PlayerUtils.isTeamHaveAlivePlayers(teamName)) {
            team.setTeamActive(false);
            checkOnActiveTeams();
        }
        if((Boolean)RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_IMMINENT_DEATH, false) && !isFinished) {
            team.getPlayers().forEach(playerModel -> OnEntityDieHandler.OnDeath(playerModel.getReference()));
        }
        for(ITeam currentTeam : teamData.values()) {
            currentTeam.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
        }
        checkOnActiveTeams();
        return null;
    };

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

    public boolean damageCore(String teamName, boolean isPlayerDamager) {
        IModifiableTeam team = teamData.get(teamName);
        if(team == null) return false;
        int oldHP = team.getCoreInstance().getHealth();
        boolean isDestroyed = team.getCoreInstance().TakeDamage(isPlayerDamager);
        int newHP = team.getCoreInstance().getHealth();
        if(!isDestroyed && isPlayerDamager) {
            int oldScale = oldHP % 5;
            int newScale = newHP % 5;
            if(newScale > oldScale) {
                team.getPlayers().forEach(playerModel -> {
                    Player player = playerModel.getReference();
                    NotificationUtils.sendNotification(player, MessageConstant.TEAM_DAMAGED_MESSAGE, NotificationUtils.NotificationType.WARNING, false);
                    player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1f, .7f);
                });
            }
        }
        team.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
        return isDestroyed;
    }
    
    public void healCore(Player player, String teamName, float price) {
        IModifiableTeam team = teamData.get(teamName);
        if(team == null) return;
        if(team.getCoreInstance() == null) return;
        if(team.getCoreInstance().isCoreCannotBeHealed()) {
            NotificationUtils.sendNotification(player, MessageConstant.CANNOT_HEAL_CORE, NotificationUtils.NotificationType.FAIL, false);
            return;
        }
        PlayerInteractComponent component = (PlayerInteractComponent) getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(component == null) return;
        if(!component.adjustPlayerTokens(player, -price, TransactionType.PURCHASE)) {
            NotificationUtils.sendNotification(player, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
            return;
        }
        team.getCoreInstance().Heal();
        team.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
    }

    private void checkOnActiveTeams() {
        List<ITeam> aliveTeams = teamData.values().stream().filter(ITeam::isTeamActive).collect(Collectors.toList());
        if(aliveTeams.size() < 2) {
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

    private void notifyDestruction(ITeam team) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            String title;
            String subTitle;
            if(team.getPlayers().stream().anyMatch(playerModel -> playerModel.getReference().getUniqueId().equals(player.getUniqueId()))) {
                title = ChatColor.RED + "" + ChatColor.BOLD + MessageConstant.CORE_DESTROYED_TITLE;
                subTitle = ChatColor.GOLD + MessageConstant.CORE_DESTROYED_SUBTITLE;
            } else {
                title = ChatColor.GREEN + "" + ChatColor.BOLD + String.format(MessageConstant.OTHER_CORE_DESTROYED_TITLE, team.getTeamName());
                subTitle = ChatColor.WHITE + MessageConstant.OTHER_CORE_DESTROYED_SUBTITLE;
            }
            player.sendTitle(title, subTitle, 10, 60, 10);
        });
        Logger.broadcast(ChatColor.GOLD + String.format(MessageConstant.CORE_DESTROYED_BROADCAST, team.getTeamName()));
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
        DataInstance.PlayerInfo applicantInfo = Store.getInstance().getDataInstance().FindPlayerByName(applicant.getUniqueId());
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
