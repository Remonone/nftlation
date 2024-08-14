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
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.application.services.SkinCache;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.damage.TeamAttackInvoker;
import remonone.nftilation.game.ingame.core.Core;
import remonone.nftilation.game.ingame.services.RepairCoreService;
import remonone.nftilation.game.ingame.services.SecondTierService;
import remonone.nftilation.game.ingame.services.ServiceContainer;
import remonone.nftilation.game.ingame.services.ThirdTierService;
import remonone.nftilation.game.mob.AngryGolem;
import remonone.nftilation.game.models.*;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.utils.EntityList;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerNMSUtil;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getPlayer;

public class GameConfiguration {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public static void disposePlayers(Set<ITeam> teams) {
        for(ITeam team : teams) {
            team.getPlayers().forEach(playerModel -> {
                playerModel.getReference().teleport(team.getTeamSpawnPoint().getPosition());
            });
        }
    }

    public static void initServices() {
        ServiceContainer.registerService(new RepairCoreService());
        ServiceContainer.registerService(new SecondTierService());
        ServiceContainer.registerService(new ThirdTierService());
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
                Role.UpdatePlayerAbilities(model);
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
                    .build();
            teamData.put(teamName, team);
        }
        return teamData;
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
        location.getBlock().setType(Material.BEACON);
        return core;
    }

    private static Collection<? extends IDamageHandler> constructDamageHandlers(PlayerModel model) {
        List<IDamageHandler> invokers = new ArrayList<>();
        Role role = Role.getRoleByID(model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID).toString());
        if(role == null) {
            Logger.error("Unexpected issue have encountered during construction damage invokers for: " + model.getReference().getDisplayName());
            return Collections.emptyList();
        }
        invokers.addAll(role.getDamageHandlers());
        return invokers;
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

}
