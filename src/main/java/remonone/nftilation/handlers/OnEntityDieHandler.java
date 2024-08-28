package remonone.nftilation.handlers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.*;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.OnPlayerKillPlayerEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.IDamageHandler;
import remonone.nftilation.game.models.IDamageInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.EntityDamageByPlayerLog;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.ResetUtils;


import java.util.PriorityQueue;

import static org.bukkit.Bukkit.getServer;

public class OnEntityDieHandler implements Listener {
    @EventHandler
    public void onPlayerTakeDamage(final EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getEntity();
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(teamName, player);
        if(model == null) return;
        PriorityQueue<IDamageHandler> queue = new PriorityQueue<>(model.getDamageHandlers());
        while(!queue.isEmpty()) {
            queue.poll().OnDamageHandle(event);
        }
        if(event.isCancelled() || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if(player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            OnDeath(player);
            Player attacker = EntityDamageByPlayerLog.getEventLogForLivingEntity(player.getUniqueId());
            if(attacker == null) return;
            EntityDamageByPlayerLog.removeLogEvent(player.getUniqueId());
            OnPlayerKillPlayerEvent e = new OnPlayerKillPlayerEvent(PlayerUtils.getModelFromPlayer(attacker), model);
            getServer().getPluginManager().callEvent(e);
        }
    }
    
    @EventHandler
    public void onHostileEntityDie(final EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) return;
        if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        if(!EntityHandleComponent.isEntityHostile(entity) ) return;
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(component == null) return;
        if(entity.getHealth() - event.getFinalDamage() <= 0) {
            Player attacker = EntityDamageByPlayerLog.getEventLogForLivingEntity(entity.getUniqueId());
            if(attacker == null) return;
            EntityDamageByPlayerLog.removeLogEvent(entity.getUniqueId());
            component.adjustPlayerTokens(attacker, EntityHandleComponent.getEntityBounty(entity), TransactionType.KILL_GAIN);
        }
    }
    
    @EventHandler
    public void onHostileEntityDamageFromPlayer(final EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player) return;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity target = (LivingEntity) event.getEntity();
        if(target == null || !EntityHandleComponent.isEntityHostile(target)) return;
        PlayerUtils.AttackerInfo info = PlayerUtils.getAttackerPlayer(event.getDamager());
        if(info == null) {
            return;
        }
        if(target.getHealth() - event.getFinalDamage() <= 0) {
            PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
            if(component == null) return;
            component.adjustPlayerTokens(info.attacker, EntityHandleComponent.getEntityBounty(target), TransactionType.KILL_GAIN);
            EntityDamageByPlayerLog.removeLogEvent(info.attacker.getUniqueId());
        } else {
            EntityDamageByPlayerLog.insertLogEvent(target, info.attacker);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTakeDamageFromPlayer(final EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if(Store.getInstance().getGameStage().getStage() == Stage.LOBBY) {
            event.setCancelled(true);
            return;
        }
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player target = (Player) event.getEntity();
        if(target.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            OnDeath(target);
        }
        PlayerUtils.AttackerInfo attackerData = PlayerUtils.getAttackerPlayer(event.getDamager());
        if(attackerData == null) return;
        PlayerModel targetModel = PlayerUtils.getModelFromPlayer(target);
        PlayerModel attackerModel = PlayerUtils.getModelFromPlayer(attackerData.attacker);
        if(targetModel == null || attackerModel == null) return;
        if(GameInstance.getInstance().checkIfPlayersInSameTeam(target, attackerData.attacker)) {
            event.setCancelled(true);
            return;
        }
        PriorityQueue<IDamageHandler> targetQueue = new PriorityQueue<>(targetModel.getDamageHandlers());
        while(!targetQueue.isEmpty()) {
            targetQueue.poll().OnEntityDamageHandle(event);
        }
        PriorityQueue<IDamageInvoker> queue = new PriorityQueue<>(attackerModel.getDamageInvokers());
        while(!queue.isEmpty()) {
            queue.poll().OnEntityDamageDealing(event, attackerData);
        }
        if(event.isCancelled()) return;
        if(target.getHealth() - event.getFinalDamage() <= 0) {
            EntityDamageByPlayerLog.removeLogEvent(target.getUniqueId());
            OnPlayerKillPlayerEvent e = new OnPlayerKillPlayerEvent(attackerModel, targetModel);
            getServer().getPluginManager().callEvent(e);
        }
        
        if(target.getHealth() - event.getFinalDamage() > 0) {
            EntityDamageByPlayerLog.insertLogEvent(target, attackerData.attacker);
        }
    }

    public static void OnDeath(Player player) {
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        GameInstance.getInstance().setPlayerDead(teamName, player);
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(teamName, player);
        if(!PlayerUtils.validateParams(model.getParameters())) {
            return;
        }
        String roleId = model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID).toString();
        Role.onDie(player, Role.getRoleByID(roleId));
        Vector pos = ConfigManager.getInstance().getCenterDeadZoneCoords();
        Location location = pos.toLocation(Store.getInstance().getDataInstance().getMainWorld());
        player.teleport(location);
        player.setGameMode(GameMode.SPECTATOR);
        player.sendTitle(ChatColor.DARK_RED + MessageConstant.PLAYER_ON_DIE, "", 2, 30, 2);
        getServer().getScheduler().runTaskLater(Nftilation.getInstance(), () -> {
            if(GameInstance.getInstance().getTeam(teamName).isCoreAlive()) {
                GameInstance.getInstance().respawnPlayer(player, teamName);
                ResetUtils.globalResetPlayerStats(player);
                if((boolean)RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_INVENTORY_AUTO_CLEAR, true))
                    Role.refillInventoryWithItems(model);
            }
        }, (long) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_RESPAWN_TIMER, (long) 5 * DataConstants.TICKS_IN_SECOND));
    }
    
    
}
