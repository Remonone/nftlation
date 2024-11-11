package remonone.nftilation.game.damage;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.roles.Watcher;
import remonone.nftilation.utils.EntityDamageByPlayerLog;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.List;
import java.util.Random;


public class WatcherOnKillHandler extends BaseDamageHandler {
    
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    
    @Override
    public int getPriority() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void OnEntityDamageHandle(EntityDamageByEntityEvent e) {
        Logger.debug("Checking for kill...");
        Player player = (Player) e.getEntity();
        PlayerModel target = PlayerUtils.getModelFromPlayer(player);
        if(!(Role.getRoleByID((String)target.getParameters().get(PropertyConstant.PLAYER_ROLE_ID)) instanceof Watcher)) return;
        Logger.debug("Trying to kill watcher..." + (player.getHealth() - e.getFinalDamage()));
        if(player.getHealth() - e.getFinalDamage() > 0) return;
        Logger.debug("Health is below zero...");
        e.setCancelled(true);
        Entity damager = e.getDamager();
        if(!(damager instanceof Player)) {
            damager = EntityDamageByPlayerLog.getEventLogForLivingEntity(player.getUniqueId());
        }
        if(damager == null) return;
        Player damagerPlayer = (Player) damager;
        PlayerModel attacker = PlayerUtils.getModelFromPlayer(damagerPlayer);
        ITeam team = PlayerUtils.getTeamFromPlayer(damagerPlayer);
        if(team == null) return;
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        for(PlayerModel model : team.getPlayers()) {
            component.adjustPlayerTokens(model, 1000, TransactionType.KILL_GAIN);
        }
        PlayerModel watcher = PlayerUtils.getModelFromPlayer(player);
        Role role = Role.getRoleByID((String)watcher.getParameters().get(PropertyConstant.PLAYER_ROLE_ID));
        List<ItemStack> bossItems = role.getAbilityItems(watcher.getParameters());
        ItemStack itemStack = bossItems.get(RANDOM.nextInt(bossItems.size()));
        ((List<ItemStack>)attacker.getParameters().get(PropertyConstant.PLAYER_CUSTOM_ABILITY_ITEMS)).add(itemStack);
        PlayerInventory inventory = damagerPlayer.getInventory();
        if(inventory.firstEmpty() == -1) {
            damagerPlayer.sendMessage("После вашего возраждения вас ждет дар...");
        } else {
            ItemStatModifierComponent.markItemAsUndroppable(itemStack);
            ItemStatModifierComponent.markItemAsUnstorable(itemStack);
            ItemStatModifierComponent.markItemAsUncraftable(itemStack);
            inventory.addItem(itemStack);
        }
        watcher.getReference().setGameMode(GameMode.SPECTATOR);
        watcher.getReference().teleport(watcher.getReference().getLocation().add(new Vector(0, 200, 0)));
        watcher.getReference().kickPlayer("Ваша миссия выполнена, милорд...");
        GameInstance.getInstance().removeTeam((String) watcher.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME));
    }

    @Override
    public void OnDamageHandle(EntityDamageEvent e) {
        if(!checkForLegalCause(e.getCause())) {
            e.setCancelled(true);
        }
    }

    private boolean checkForLegalCause(EntityDamageEvent.DamageCause cause) {
        return cause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) || cause.equals(EntityDamageEvent.DamageCause.FIRE)
                || cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                || cause.equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK);
    }
}
