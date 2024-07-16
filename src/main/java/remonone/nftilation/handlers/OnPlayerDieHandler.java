package remonone.nftilation.handlers;

import org.apache.commons.lang.StringUtils;
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
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerDamageByPlayerLog;
import remonone.nftilation.utils.ResetUtils;

import java.util.Objects;

import static org.bukkit.Bukkit.getServer;

public class OnPlayerDieHandler implements Listener {
    @EventHandler
    public void onPlayerTakeDamage(final EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        Player player = (Player) event.getEntity();
        if(player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            OnDeath(player);
            Player attacker = PlayerDamageByPlayerLog.getEventLogForPlayer(player.getUniqueId());
            Logger.debug("Checking for attacker: " + (attacker != null ? attacker.getName() : ""));
            if(attacker == null) return;
            countKill(player, attacker);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTakeDamageFromPlayer(final EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if(Store.getInstance().getGameStage().getStage() == Stage.LOBBY) {
            event.setCancelled(true);
        }
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player target = (Player) event.getEntity();
        Player attacker = null;
        if(event.getDamager() instanceof TNTPrimed) {
            TNTPrimed tntPrimed = (TNTPrimed) event.getDamager();
            attacker = EntityHandleComponent.getEntityOwner(tntPrimed);
            if(Objects.equals(attacker, target)) {
                event.setDamage(event.getDamage() / 2);
            }
        }
        if(event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }
        if(event.getDamager() instanceof Arrow) {
            attacker = (Player) ((Arrow) event.getDamager()).getShooter();
        }
        if(event.getDamager() instanceof AreaEffectCloud) {
            attacker = EntityHandleComponent.getEntityOwner(event.getDamager());
            if(attacker == null) return;
            String teamName = Store.getInstance().getDataInstance().getPlayerTeam(attacker.getUniqueId());
            if(StringUtils.isEmpty(teamName)) return;
            if(attacker.equals(target)) {
                event.setCancelled(true);
                return;
            }
        }
        if(event.getDamager() instanceof Fireball) {
            attacker = EntityHandleComponent.getEntityOwner(event.getDamager());
        }
        if(!(event.getDamager() instanceof Player) && event.getDamager() instanceof LivingEntity) {
            Player owner = EntityHandleComponent.getEntityOwner(event.getDamager());
            if(owner == null) {
                if(target.getHealth() - event.getFinalDamage() <= 0) {
                    event.setCancelled(true);
                    OnDeath(target);
                }
                return;
            }
            attacker = owner;
        }
        if(attacker == null) {
            return;
        }
        if(GameInstance.getInstance().checkIfPlayersInSameTeam(target, attacker)) {
            event.setCancelled(true);
            return;
        }
        if(target.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            OnDeath(target);
            countKill(target, attacker);
        } else {
            PlayerDamageByPlayerLog.insertLogEvent(target, attacker);
        }
    }
    
    public void countKill(Player target, Player attacker) {
        String attackerTeam = Store.getInstance().getDataInstance().getPlayerTeam(attacker.getUniqueId());
        GameInstance.getInstance().awardPlayer(attackerTeam, attacker, DataConstants.TOKEN_PER_KILL);
        String targetTeam =Store.getInstance().getDataInstance().getPlayerTeam(target.getUniqueId());
        GameInstance.getInstance().increasePlayerKillCounter(attackerTeam, attacker);
        GameInstance.getInstance().increasePlayerDeathCounter(targetTeam, target);
    }

    public static void OnDeath(Player player) {
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        GameInstance.getInstance().setPlayerDead(teamName, player);
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(teamName, player);
        Role.OnDie(player, Role.getRoleByID(model.getRoleId()));
        Vector pos = ConfigManager.getInstance().getCenterDeadZoneCoords();
        Location location = pos.toLocation(Store.getInstance().getDataInstance().getMainWorld());
        player.teleport(location);
        player.setGameMode(GameMode.SPECTATOR);
        player.sendTitle(ChatColor.DARK_RED + MessageConstant.PLAYER_ON_DIE, "", 2, 30, 2);
        getServer().getScheduler().runTaskLater(Nftilation.getInstance(), () -> {
            if(GameInstance.getInstance().isTeamAlive(teamName)) {
                GameInstance.getInstance().respawnPlayer(player, teamName);
                ResetUtils.globalResetPlayerStats(player);
                if((boolean)RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_INVENTORY_AUTO_CLEAR, true))
                    Role.refillInventoryWithItems(player, Role.getRoleByID(model.getRoleId()), model.getUpgradeLevel());
            }
        }, (long) RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_RESPAWN_TIMER, 5 * DataConstants.TICKS_IN_SECOND));
    }
}
