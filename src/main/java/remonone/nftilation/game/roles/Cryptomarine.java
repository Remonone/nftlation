package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.damage.CryptomarineAxeDamage;
import remonone.nftilation.game.damage.CryptomarineDeathHandler;
import remonone.nftilation.game.ingame.objects.Barrier;
import remonone.nftilation.game.models.*;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

public class Cryptomarine extends Role {
    
    public Cryptomarine() {
        super("CM");
        super.registerHandlers(new HashMap<String, IAbilityHandler>() {{
            put(RoleConstant.CRYPTOMARINE_NBT_BARRIER, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return onBarrierUse(model);
                }

                @Override
                public float getCooldown(PlayerModel model) {
                    return ((Double)getMetaByName(model, MetaConstants.META_CRYPTOMARINE_BARRIER_COOLDOWN)).floatValue();
                }
            });
        }}, RoleConstant.CRYPTOMARINE_NBT_CONTAINER);
    }

    @SuppressWarnings("unchecked")
    private boolean onBarrierUse(PlayerModel model) {
        Player player = model.getReference();
        ITeam team = PlayerUtils.getTeamFromPlayer(player);
        double radius = (Double)getMetaByName(model, MetaConstants.META_CRYPTOMARINE_BARRIER_RADIUS);
        double duration = (Double)getMetaByName(model, MetaConstants.META_CRYPTOMARINE_BARRIER_DURATION);
        double scale = (Double)getMetaByName(model, MetaConstants.META_CRYPTOMARINE_BARRIER_FORCE);
        List<EffectPotion> potions = (List<EffectPotion>)getMetaByName(model, MetaConstants.META_CRYPTOMARINE_BARRIER_EFFECTS);
        Barrier barrier = Barrier.builder()
                .owner(player)
                .radius(radius)
                .potions(potions)
                .center(player.getLocation())
                .teamOwner(team)
                .throwbackScale(scale)
                .build();
        barrier.initBarrier((int)(duration * DataConstants.TICKS_IN_SECOND));
        return true;
    }

    @Override
    public String getRoleID() {
        return "CM";
    }
    
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params){
        List<ItemStack> items = new ArrayList<>();
        ItemStack itemStack = new ItemStack(Material.SHIELD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.CTYPTOMARINE_SHIELD);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        items.add(itemStack);
        int level = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        if((Boolean)getMetaInfo(MetaConstants.META_CRYPTOMARINE_BARRIER_AVAILABILITY, level)) {
            ItemStack barrier = new ItemStack(Material.BANNER);
            ItemMeta barrierMeta = barrier.getItemMeta();
            String name = (String)getMetaInfo(MetaConstants.META_CRYPTOMARINE_BARRIER_NAME, level);
            barrierMeta.setDisplayName(name);
            barrier.setItemMeta(barrierMeta);
            NBT.modify(barrier, nbt -> {
                nbt.setString(RoleConstant.ROLE, getRoleID());
                nbt.setString(RoleConstant.CRYPTOMARINE_NBT_CONTAINER, RoleConstant.CRYPTOMARINE_NBT_BARRIER);
            });
            items.add(barrier);
        }
        return items;
    }

    @Override
    public List<IDamageHandler> getDamageHandlers() {
        return Collections.singletonList(new CryptomarineDeathHandler());
    }

    @Override
    public List<IDamageInvoker> getDamageInvokers() {
        return Collections.singletonList(new CryptomarineAxeDamage());
    }


}
