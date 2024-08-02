package remonone.nftilation.game;

import lombok.*;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftIronGolem;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.services.SkinCache;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.game.ingame.core.Core;
import remonone.nftilation.game.ingame.core.ICoreData;
import remonone.nftilation.game.ingame.services.RepairCoreService;
import remonone.nftilation.game.ingame.services.SecondTierService;
import remonone.nftilation.game.ingame.services.ServiceContainer;
import remonone.nftilation.game.ingame.services.ThirdTierService;
import remonone.nftilation.game.mob.AngryGolem;
import remonone.nftilation.game.models.IModifiableTeam;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.phase.PhaseCounter;
import remonone.nftilation.game.roles.Guts;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.handlers.OnEntityDieHandler;
import remonone.nftilation.utils.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.*;

public class GameInstance {
    
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    
    @Getter
    private final static GameInstance instance = new GameInstance();
    private Map<String, IModifiableTeam> teamData;
    @Getter
    private PhaseCounter counter;
    
    private boolean isFinished;
    
    public void startGame() {
        Map<String, List<DataInstance.PlayerInfo>> teams = Store.getInstance().getDataInstance().getTeams();
        constructTeamData(teams);
        disposePlayers();
        initServices();
        spawnGolems();
        counter = new PhaseCounter();
        counter.Init();
        for(ITeam team : teamData.values()) {
            initPlayerRoles(team.getPlayers());
            team.getPlayers().forEach(Role::refillInventoryWithItems);
            for(PlayerModel model : team.getPlayers()) {
                ScoreboardHandler.buildScoreboard(model.getReference());
            }
        }
    }

    private void spawnGolems() {
        List<Location> locations = ConfigManager.getInstance().getIronGolemPositions();
        for(Location location : locations) {
            location.getChunk().load();
            AngryGolem golem = new AngryGolem(location);
            ((CraftWorld)location.getWorld()).getHandle().addEntity(golem, CreatureSpawnEvent.SpawnReason.CUSTOM);
            EntityHandleComponent.setEntityUnloadLocked(golem.getBukkitEntity());
            EntityHandleComponent.setEntityHostile(golem.getBukkitEntity());
            EntityHandleComponent.setEntityBounty(golem.getBukkitEntity(), 120);
            EntityList.addEntity((LivingEntity) golem.getBukkitEntity());
            golem.setCustomName("Angry Golem");
            ((CraftIronGolem)golem.getBukkitEntity()).setRemoveWhenFarAway(false);
        }
    }

    public boolean checkIfPlayersInSameTeam(Player player1, Player player2) {
        String data1 = Store.getInstance().getDataInstance().getPlayerTeam(player1.getUniqueId());
        String data2 = Store.getInstance().getDataInstance().getPlayerTeam(player2.getUniqueId());
        return !StringUtils.isEmpty(data1) && !StringUtils.isEmpty(data2) && data1.equals(data2);
    }

    private void initServices() {
        ServiceContainer.registerService(new RepairCoreService());
        ServiceContainer.registerService(new SecondTierService());
        ServiceContainer.registerService(new ThirdTierService());
    }


    private void initPlayerRoles(List<PlayerModel> models) {
        for(PlayerModel model : models) {
            Map<String, Object> params = model.getParameters();
            if(!PlayerUtils.validateParams(params)) {
                continue;
            }
            String roleId = params.get(PropertyConstant.PLAYER_ROLE_ID).toString();
            try {
                String texture = SkinCache.getInstance().getTexture(roleId);
                String signature = SkinCache.getInstance().getSignature(roleId);
                PlayerNMSUtil.changePlayerSkin(model.getReference(), texture, signature);
                Role.UpdatePlayerAbilities(model);
            } catch(Exception ex) {
                Logger.error("Unable load role: " + roleId + ". Player: " + model.getReference().getDisplayName());
            }
        }
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
                Map<String, Object> parameters = getParametersObject(info, teamName);
                PlayerModel model = new PlayerModel(getPlayer(info.getPlayerId()), 0, parameters);
                model.getDamageInvokers().addAll(info.getRole().getDamageInvokers());
                model.getDamageHandlers().addAll(info.getRole().getDamageHandlers());
                teamPlayers.add(model);
            }
            Core teamCore = SetCore(teamName, point);
            SpawnShopKeeper(point);
            IModifiableTeam team = new IModifiableTeam() {

                private final UUID uuid = new UUID(50, 10);
                private boolean isTeamAlive = true;
                private boolean isTeamActive = true;
                
                @Override
                public UUID getTeamID() {
                    return uuid;
                }

                @Override
                public List<PlayerModel> getPlayers() {
                    return teamPlayers;
                }

                @Override
                public ICoreData getCoreData() {
                    return teamCore;
                }

                @Override
                public TeamSpawnPoint getTeamSpawnPoint() {
                    return point;
                }

                @Override
                public boolean isCoreAlive() {
                    return isTeamAlive;
                }

                @Override
                public boolean isTeamActive() {
                    return isTeamActive;
                }

                @Override
                public String getTeamName() {
                    return teamName;
                }

                @Override
                public char getTeamColor() {
                    return 0;
                }
                
                @Override
                public void setTeamActive(boolean value) {
                    isTeamAlive = value;
                }

                @Override
                public void setCoreAlive(boolean value) {
                    isTeamActive = value;
                }

                @Override
                public Core getCoreInstance() {
                    return teamCore;
                }
            };
            teamData.put(teamName, team);
        }
    }

    private static Map<String, Object> getParametersObject(DataInstance.PlayerInfo info, String teamName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PropertyConstant.PLAYER_LEVEL_PARAM, 1);
        parameters.put(PropertyConstant.PLAYER_DEATH_COUNT, 0);
        parameters.put(PropertyConstant.PLAYER_KILL_COUNT, 0);
        parameters.put(PropertyConstant.PLAYER_ROLE_ID, info.getRole().getRoleID());
        parameters.put(PropertyConstant.PLAYER_IS_ALIVE_PARAM, true);
        parameters.put(PropertyConstant.PLAYER_TEAM_NAME, teamName);
        return parameters;
    }

    private Role getRandomRole(List<DataInstance.PlayerInfo> players) {
        List<String> reservedRoles = players.stream()
                .filter(info -> ObjectUtils.notEqual(info.getRole(), null))
                .map(roleContainer -> roleContainer.getRole().getRoleID())
                .collect(Collectors.toList());
        List<Role> availableRoles = Role.getRoles().stream().filter(role -> !reservedRoles.contains(role.getRoleID()) && !role.getRoleName().equals("Guts")).collect(Collectors.toList());
        return availableRoles.get(RANDOM.nextInt(availableRoles.size()));
    }

    public Iterator<ITeam> getTeamIterator() {
        List<ITeam> teams = new ArrayList<>(teamData.values());
        return teams.iterator();
    }
    
    public void awardPlayer(String teamName, Player player, int tokens) {
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        model.setTokens(model.getTokens() + tokens);
        ScoreboardHandler.updateScoreboard(model);
    }
    
    public boolean haveEnoughMoney(String teamName, Player player, int amount) {
        return getPlayerModelFromTeam(teamName, player).getTokens() >= amount;
    }
    
    public boolean withdrawFunds(String teamName, Player player, int amount) {
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        if(!haveEnoughMoney(teamName, player, amount)) return false;
        model.setTokens(model.getTokens() - amount);
        ScoreboardHandler.updateScoreboard(model);
        return true;
    }
    
    public void increasePlayerKillCounter(String teamName, Player player) {
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        Map<String, Object> params = model.getParameters();
        int killCount = (Integer) params.getOrDefault(PropertyConstant.PLAYER_KILL_COUNT, 0);
        params.put(PropertyConstant.PLAYER_KILL_COUNT, ++killCount);
        ScoreboardHandler.updateScoreboard(model);
    }

    public void increasePlayerDeathCounter(String teamName, Player player) {
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        Map<String, Object> params = model.getParameters();
        int killCount = (Integer) params.getOrDefault(PropertyConstant.PLAYER_DEATH_COUNT, 0);
        params.put(PropertyConstant.PLAYER_DEATH_COUNT, ++killCount);
        ScoreboardHandler.updateScoreboard(model);
    }
    
    public ITeam getTeam(String teamName) {
        return teamData.getOrDefault(teamName, null);
    }

    private void SpawnShopKeeper(TeamSpawnPoint point) {
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location location = point.getShopKeeperPosition();
        location.getChunk().load();
        Villager villager = world.spawn(location, Villager.class);
        villager.setAI(false);
        villager.setCustomName("Shop keeper");
        villager.setCustomNameVisible(false);
        villager.setInvulnerable(true);
        villager.setRemoveWhenFarAway(false);
        EntityHandleComponent.setEntityUnloadLocked(villager);
        EntityList.addEntity(villager);
    }

    private Core SetCore(String teamName, TeamSpawnPoint point) {
        Core core = new Core(() -> destroyTeam(teamName));
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location location = point.getCoreCenter().toLocation(world);
        location.getBlock().setType(Material.BEACON);
        return core;
    }
    
    public void upgradePlayer(Player player, int level, int price) {
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = getPlayerModelFromTeam(teamName, player);
        Map<String, Object> params = model.getParameters();
        if(!params.containsKey(PropertyConstant.PLAYER_LEVEL_PARAM)) {
            Logger.error("Cannot fetch upgrade level for player: " + player.getDisplayName());
            return;
        }
        if(level - (int)params.get(PropertyConstant.PLAYER_LEVEL_PARAM) != 1) {
            player.sendMessage(ChatColor.RED + MessageConstant.INCORRECT_UPGRADE_LEVEL);
            return;
        }
        if((int)RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_AVAILABLE_TIER, 1) < level) {
            player.sendMessage(ChatColor.RED + MessageConstant.INCORRECT_STAGE_FOR_UPGRADE);
            return;
        }
        if(!haveEnoughMoney(teamName, player, price)) {
            player.sendMessage(ChatColor.RED + MessageConstant.NOT_ENOUGH_MONEY);
            return;
        }
        withdrawFunds(teamName, player, price);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, .5f, 1f);
        params.put(PropertyConstant.PLAYER_LEVEL_PARAM, level);

        Role role = Role.getRoleByID(model.getParameters().getOrDefault(PropertyConstant.PLAYER_ROLE_ID, "_").toString());
        if(role == null) {
            Logger.error("Cannot upgrade level for player: " + player.getDisplayName());
            return;
        }
        if(role instanceof Guts) {
            if(level == 2) {
                Logger.broadcast(ChatColor.RED + "Мишка потерял концентрацию и его внимание расплывчато!");
            }
            if(level == 3) {
                Logger.broadcast(ChatColor.DARK_RED + "Мишка сильно ослаб и находится в предсмертном состоянии!");
            }
        }
        Role.SetInventoryItems(model);
        Role.UpdatePlayerAbilities(player);
        ScoreboardHandler.updateScoreboard(model);
    }

    private void disposePlayers() {
        for(ITeam team : teamData.values()) {
            team.getPlayers().forEach(playerModel -> setPlayerToPosition(team.getTeamSpawnPoint().getPosition(), playerModel.getReference()));
        }
    }
    
    public void teleportPlayerToBase(String teamName, Player player) {
        ITeam team = teamData.get(teamName);
        if(team == null) return;
        setPlayerToPosition(team.getTeamSpawnPoint().getPosition(), player);
    }
    
    private void setPlayerToPosition(Location loc, Player player) {
        player.teleport(loc);
    }

    public ITeam getTeamByCorePosition(Vector position) {
        for(ITeam team : teamData.values()) {
            if(position.isInSphere(team.getTeamSpawnPoint().getCoreCenter(), 1.0D)) {
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
                    player.sendMessage(MessageConstant.TEAM_DAMAGED_MESSAGE);
                    player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1f, .7f);
                });
            }
        }
        team.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
        return isDestroyed;
    }
    
    public void healCore(Player player, String teamName, int price) {
        IModifiableTeam team = teamData.get(teamName);
        if(team == null) return;
        if(team.getCoreInstance() == null) return;
        if(!haveEnoughMoney(teamName, player, price)) {
            player.sendMessage(ChatColor.RED + MessageConstant.NOT_ENOUGH_MONEY);
            return;
        }
        if(!team.getCoreInstance().Heal()) {
            player.sendMessage(ChatColor.RED + MessageConstant.CANNOT_HEAL_CORE);
            return;
        }
        withdrawFunds(teamName, player, price);
        team.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
    }

    private void destroyTeam(String teamName) {
        IModifiableTeam team = teamData.get(teamName);
        team.setCoreAlive(false);
        
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location location = team.getTeamSpawnPoint().getCoreCenter().toLocation(world);
        location.getBlock().setType(Material.AIR);
        notifyDestruction(team);
        
        if(!PlayerUtils.isTeamHaveAlivePlayers(teamName)) {
            team.setTeamActive(false);
            checkOnActiveTeams();
        }
        if((Boolean)RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_IMMINENT_DEATH, false) && !isFinished) {
            team.getPlayers().forEach(playerModel -> OnEntityDieHandler.OnDeath(playerModel.getReference()));
        }
        for(ITeam currentTeam : teamData.values()) {
            currentTeam.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
        }
        checkOnActiveTeams();
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
    }
    
    public void respawnPlayer(Player player, String teamName) {
        ITeam team = teamData.get(teamName);
        if(team == null) return;
        if(team.isCoreAlive()) {
            setPlayerToPosition(team.getTeamSpawnPoint().getPosition(), player);
            PlayerModel model = getPlayerModelFromTeam(teamName, player);
            if(model == null) return;
            model.getParameters().put(PropertyConstant.PLAYER_IS_ALIVE_PARAM, true);
            Role.UpdatePlayerAbilities(model);
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
}
