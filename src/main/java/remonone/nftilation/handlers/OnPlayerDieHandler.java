package remonone.nftilation.handlers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;

import static org.bukkit.Bukkit.getServer;

public class OnPlayerDieHandler implements Listener {
    @EventHandler
    public void onPlayerTakeDamage(final EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if(event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            Logger.debug("Get exploded!");
        }
        Player player = (Player) event.getEntity();
        if(player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            OnDeath(player);
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
            attacker = (Player) tntPrimed.getMetadata("invoker").get(0).value();
            if(attacker.equals(target)) {
                event.setDamage(event.getDamage() / 2);
            }
        }
        if(event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }
        if(event.getDamager() instanceof Arrow) {
            attacker = (Player) ((Arrow) event.getDamager()).getShooter();
        }
        // TODO: Add Ruslan mobs
        if(attacker == null) return;
        if(GameInstance.getInstance().checkIfPlayersInSameTeam(target, attacker)) {
            event.setCancelled(true);
            return;
        }
        if(target.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            OnDeath(target);
            PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(attacker.getName());
            String teamName = data.getTeam().getTeamName();
            GameInstance.getInstance().awardPlayer(teamName, attacker, DataConstants.TOKEN_PER_KILL);
            GameInstance.getInstance().increasePlayerKillCounter(teamName, attacker);
            GameInstance.getInstance().increasePlayerDeathCounter(teamName, target);
        }
    }

    private void OnDeath(Player player) {
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        String teamName = data.getTeam().getTeamName();
        GameInstance.getInstance().setPlayerDead(teamName, player.getUniqueId());
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(teamName, player);
        Role.OnDie(player, Role.getRoleByID(model.getRoleId()), model.getUpgradeLevel());
        Vector pos = ConfigManager.getInstance().getCenterDeadZoneCoords();
        Location location = pos.toLocation(Store.getInstance().getDataInstance().getMainWorld());
        player.teleport(location);
        player.setGameMode(GameMode.SPECTATOR);
        player.sendTitle(ChatColor.DARK_RED + MessageConstant.PLAYER_ON_DIE, "", 2, 30, 2);
        getServer().getScheduler().runTaskLater(Nftilation.getInstance(), () -> {
            if(GameInstance.getInstance().isTeamAlive(teamName)) {
                GameInstance.getInstance().respawnPlayer(player, teamName);
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(player.getMaxHealth());
                Role.refillInventoryWithItems(player, Role.getRoleByID(model.getRoleId()), model.getUpgradeLevel());
                player.setFoodLevel(20);
            }
        }, (long) RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_RESPAWN_TIMER, 5 * 20));
    }
}
