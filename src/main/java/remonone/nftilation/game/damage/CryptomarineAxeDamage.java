package remonone.nftilation.game.damage;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.Store;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.roles.Cryptomarine;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class CryptomarineAxeDamage extends BaseDamageInvoker {

    private static final Random RANDOM = new Random();

    @Override
    public int getPriority() {
        return 1;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void OnEntityDamageDealing(EntityDamageByEntityEvent e, PlayerUtils.AttackerInfo info) {
        if(!(e.getDamager() instanceof Player)) return;
        if(!(e.getEntity() instanceof Player)) return;
        if(e.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) return;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player attacker = (Player)e.getDamager();
        Player victim = (Player)e.getEntity();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(attacker.getUniqueId()) instanceof Cryptomarine)) return;
        ItemStack itemStack = attacker.getInventory().getItemInMainHand();
        if(itemStack == null || itemStack.getAmount() < 1 || itemStack.getType().equals(Material.AIR)) return;
        String axe = NBT.get(itemStack, nbt -> (String) nbt.getString(RoleConstant.CRYPTOMARINE_NBT_CONTAINER));
        if(StringUtils.isEmpty(axe) || !axe.equals(RoleConstant.CRYPTOMARINE_NBT_AXE)) return;
        if(RANDOM.nextFloat() > RoleConstant.CRYPTOMARINE_LIGHTNING_CHANCE) return;
        World world = attacker.getWorld();
        Location location = e.getEntity().getLocation();
        world.strikeLightningEffect(location);
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(attacker, victim, EntityDamageEvent.DamageCause.LIGHTNING, RoleConstant.CRYPTOMARINE_LIGHTNING_DAMAGE);
        getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        victim.setHealth(victim.getHealth() - event.getFinalDamage());
        victim.setLastDamageCause(event);
    }
}
