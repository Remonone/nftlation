package remonone.nftilation.game.mob;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftIronGolem;

import static org.bukkit.Bukkit.getServer;

public class AngryGolem extends EntityIronGolem {
    
    public AngryGolem(Location location) {
        super(((CraftWorld) location.getWorld()).getHandle());
        this.setPosition(location.getX(), location.getY(), location.getZ());
        this.setYawPitch(location.getYaw(), location.getPitch());
        this.bukkitEntity = new CraftIronGolem((CraftServer) getServer(), this);
        
    }
    @Override
    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1F, true));
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, .9F, 32.0F));
        this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 1F));
        this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 0.6F));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(2, new PathfinderGoalHurtByTarget(this, false));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, 0, true, true, null));
    }
}
