package remonone.nftilation.game;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftIronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.application.services.SkinCache;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.damage.TeamAttackInvoker;
import remonone.nftilation.game.ingame.core.Core;
import remonone.nftilation.game.ingame.services.*;
import remonone.nftilation.game.ingame.services.events.NukeEvent;
import remonone.nftilation.game.ingame.services.teams.CoreUpgradeService;
import remonone.nftilation.game.ingame.services.teams.PassiveIncomeUpgrade;
import remonone.nftilation.game.ingame.services.teams.ResourceIncomeService;
import remonone.nftilation.game.ingame.services.teams.ItemUtilityService;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.mob.AngryGolem;
import remonone.nftilation.game.models.*;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.utils.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getPlayer;

public class GameConfiguration {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public static void disposePlayers(Set<ITeam> teams) {
        for(ITeam team : teams) {
            team.getPlayers().forEach(playerModel -> playerModel.getReference().teleport(team.getTeamSpawnPoint().getPosition()));
        }
    }

    public static void initServices() {
        ServiceContainer.registerService(new RepairCoreService());
        ServiceContainer.registerService(new SecondTierService());
        ServiceContainer.registerService(new ThirdTierService());
        ServiceContainer.registerService(new ResourceIncomeService());
        ServiceContainer.registerService(new PassiveIncomeUpgrade());
        ServiceContainer.registerService(new ItemUtilityService());
        ServiceContainer.registerService(new CoreUpgradeService());
        ServiceContainer.registerService(new NukeEvent());
    }

    public static void spawnGolems() {
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

    public static void initPlayerRoles(ITeam team) {
        for(PlayerModel model : team.getPlayers()) {
            Map<String, Object> params = model.getParameters();
            if(!PlayerUtils.validateParams(params)) {
                continue;
            }
            String roleId = params.get(PropertyConstant.PLAYER_ROLE_ID).toString();
            try {
                String texture = SkinCache.getInstance().getTexture(roleId);
                String signature = SkinCache.getInstance().getSignature(roleId);
                PlayerNMSUtil.changePlayerSkin(model.getReference(), texture, signature);
                Role.updatePlayerAbilities(model);
            } catch(Exception ex) {
                Logger.error("Unable load role: " + roleId + ". Player: " + model.getReference().getDisplayName());
            }
        }
    }

    public static void initPlayerRunes(ITeam team) {
        for(PlayerModel model : team.getPlayers()) {
            String runeId = (String)model.getParameters().get(PropertyConstant.PLAYER_RUNE_ID);
            if(StringUtils.isBlank(runeId)) {
                Logger.error("Cannot init rune for player: " + model.getReference().getDisplayName());
                continue;
            }
            Rune rune = Rune.getRuneByID(runeId);
            rune.setPlayer(model);
        }
    }

    public static void spawnShopKeeper(TeamSpawnPoint point) {
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

    public static Map<String, IModifiableTeam> constructTeamData(Map<String, List<DataInstance.PlayerInfo>> teams, Function<String, Void> onTeamDestroy) {
        Map<String, IModifiableTeam> teamData = new HashMap<>();
        Stack<TeamSpawnPoint> teamList = new Stack<>();
        teamList.addAll(ConfigManager.getInstance().getTeamSpawnList());
        for(Map.Entry<String, List<DataInstance.PlayerInfo>> entry : teams.entrySet()) {
            String teamName = entry.getKey();
            TeamData teamInfo = Store.getInstance().getDataInstance().getTeamData().stream().filter(data -> data.getTeamName().equals(teamName)).findFirst().orElse(null);
            if (teamInfo == null) {
                Logger.error("Cannot init team " + teamName + "! Skipping...");
                continue;
            }
            TeamSpawnPoint point = teamList.pop();
            List<PlayerModel> teamPlayers = new ArrayList<>();
            for (DataInstance.PlayerInfo info : entry.getValue()) {
                if (info.getRole() == null) {
                    info.setRole(getRandomRole(entry.getValue()));
                }
                if(info.getRune() == null) {
                    info.setRune(getRandomRune());
                }
                Map<String, Object> parameters = getParametersObject(info, teamName);
                PlayerModel model = new PlayerModel(getPlayer(info.getPlayerId()), 0, parameters);
                model.getDamageInvokers().addAll(constructDamageInvokers(model));
                model.getDamageHandlers().addAll(constructDamageHandlers(model));
                teamPlayers.add(model);
            }
            Core teamCore = setCore(point);
            teamCore.setOnDieFunction(() -> onTeamDestroy.apply(teamName));
            GameConfiguration.spawnShopKeeper(point);
            IModifiableTeam team = TeamImpl.builder()
                    .teamName(teamName)
                    .players(teamPlayers)
                    .spawnPoint(point)
                    .core(teamCore)
                    .teamColor(ChatColor.getByChar(teamInfo.getTeamColor()))
                    .isTeamActive(true)
                    .isCoreAlive(true)
                    .parameters(composeParams())
                    .build();
            teamData.put(teamName, team);
        }
        return teamData;
    }

    private static Map<String, Object> composeParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(PropertyConstant.TEAM_RESOURCE_INCOME, 0);
        params.put(PropertyConstant.TEAM_PASSIVE_INCOME, 0);
        params.put(PropertyConstant.TEAM_UTILITY_ITEM_LEVEL, 0);
        params.put(PropertyConstant.TEAM_CORE_BLOCK, 0);
        return params;
    }

    private static Rune getRandomRune() {
        List<Rune> runes = Rune.getRunes();
        return runes.get(RANDOM.nextInt(runes.size()));
    }

    private static Role getRandomRole(List<DataInstance.PlayerInfo> players) {
        List<String> reservedRoles = players.stream()
                .filter(info -> ObjectUtils.notEqual(info.getRole(), null))
                .map(roleContainer -> roleContainer.getRole().getRoleID())
                .collect(Collectors.toList());
        List<Role> availableRoles = Role.getRoles().stream().filter(role -> !reservedRoles.contains(role.getRoleID()) && !role.getName().equals("Guts")).collect(Collectors.toList());
        return availableRoles.get(RANDOM.nextInt(availableRoles.size()));
    }

    private static Core setCore(TeamSpawnPoint point) {
        Core core = new Core(() -> {});
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location location = point.getCoreCenter().toLocation(world);
        String matName = (String)NestedObjectFetcher.getNestedObject("coreUpgrade", MetaConfig.getInstance().getUpgrades(), 0);
        location.getBlock().setType(Material.getMaterial(matName));
        return core;
    }

    private static Collection<? extends IDamageHandler> constructDamageHandlers(PlayerModel model) {
        Role role = Role.getRoleByID(model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID).toString());
        if(role == null) {
            Logger.error("Unexpected issue have encountered during construction damage invokers for: " + model.getReference().getDisplayName());
            return Collections.emptyList();
        }
        return new ArrayList<>(role.getDamageHandlers());
    }

    private static Collection<? extends IDamageInvoker> constructDamageInvokers(PlayerModel model) {
        List<IDamageInvoker> invokers = new ArrayList<>();
        invokers.add(new TeamAttackInvoker());
        Role role = Role.getRoleByID(model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID).toString());
        if(role == null) {
            Logger.error("Unexpected issue have encountered during construction damage invokers for: " + model.getReference().getDisplayName());
            return Collections.emptyList();
        }
        invokers.addAll(role.getDamageInvokers());
        return invokers;
    }

    private static Map<String, Object> getParametersObject(DataInstance.PlayerInfo info, String teamName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PropertyConstant.PLAYER_LEVEL_PARAM, 1);
        parameters.put(PropertyConstant.PLAYER_DEATH_COUNT, 0);
        parameters.put(PropertyConstant.PLAYER_KILL_COUNT, 0);
        parameters.put(PropertyConstant.PLAYER_ROLE_ID, info.getRole().getRoleID());
        parameters.put(PropertyConstant.PLAYER_IS_ALIVE_PARAM, true);
        parameters.put(PropertyConstant.PLAYER_TEAM_NAME, teamName);
        parameters.put(PropertyConstant.PLAYER_RUNE_ID, info.getRune().getRuneID());
        return parameters;
    }

    public static int startRepeatedTask() {
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<ITeam> teamIt = GameInstance.getInstance().getTeamIterator();
                while(teamIt.hasNext()) {
                    ITeam team = teamIt.next();
                    int level = (Integer) team.getParameters().getOrDefault(PropertyConstant.TEAM_PASSIVE_INCOME, 0);
                    Double passiveIncome = (Double)NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_PASSIVE, MetaConfig.getInstance().getUpgrades(), level);
                    if(passiveIncome == null || passiveIncome - DataConstants.ZERO_THRESHOLD < 0) {
                        continue;
                    }
                    double tickIncome = passiveIncome / 10;
                    team.getPlayers().forEach(player -> {
                        if(!player.getReference().isOnline()) return;
                        component.adjustPlayerTokens(player.getReference(), (float) tickIncome, TransactionType.PASSIVE_GAIN);
                    });
                }
            }
        };
        runnable.runTaskTimer(Nftilation.getInstance(), 10L, 10L);
        return runnable.getTaskId();
    }
}
