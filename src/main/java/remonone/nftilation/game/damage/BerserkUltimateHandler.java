package remonone.nftilation.game.damage;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.EffectPotion;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Berserk;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.AttackPresets;
import remonone.nftilation.utils.PlayerUtils;

import java.util.List;

public class BerserkUltimateHandler extends BaseDamageHandler {

    @SuppressWarnings("unchecked")
    @Override
    public void OnDamageHandle(EntityDamageEvent e) {
        if(!e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        Player player = (Player) e.getEntity();
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) return;
        String roleId = (String) model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID);
        Role role = Role.getRoleByID(roleId);
        if(role == null) return;
        if(!(role instanceof Berserk)) return;
        if(!model.getParameters().containsKey(PropertyConstant.PLAYER_FALL_DAMAGE_BERSERK)) return;
        model.getParameters().remove(PropertyConstant.PLAYER_FALL_DAMAGE_BERSERK);
        double range = (Double)role.getMetaByName(model, MetaConstants.META_BERSERK_RAGE_EXPLOSION_RANGE);
        double damage = (Double)role.getMetaByName(model, MetaConstants.META_BERSERK_RAGE_EXPLOSION_DAMAGE);
        AttackPresets.summonExplosion(player.getLocation(), player, range, damage, 3, 10, 10, 1, true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1f, .1f);
        List<Entity> entities = player.getNearbyEntities(range, range, range);
        for(Entity entity : entities) {
            if (!(entity instanceof Player)) {
                entities.remove(entity);
                continue;
            }
            Player target = (Player) entity;
            if(GameInstance.getInstance().checkIfPlayersInSameTeam(target, player)) {
                entities.remove(entity);
            }
        }
        if(entities.isEmpty()) return;
        List<EffectPotion> effects = (List<EffectPotion>) role.getMetaByName(model, MetaConstants.META_BERSERK_RAGE_EFFECTS);
        for(EffectPotion effect : effects) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect.getEffect()), effect.getDuration(), effect.getStrength()));
        }
        List<EffectPotion> debuffs = (List<EffectPotion>) role.getMetaByName(model, MetaConstants.META_BERSERK_RAGE_DEBUFFS);
        for(Entity entity : entities) {
            Player target = (Player) entity;
            for(EffectPotion debuff : debuffs) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(debuff.getEffect()), debuff.getDuration(), debuff.getStrength(), false, false));
            }
        }
        
    }
    
    @Override
    public int getPriority() {
        return 1;
    }
}
