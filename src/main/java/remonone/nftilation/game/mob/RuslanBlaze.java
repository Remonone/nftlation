package remonone.nftilation.game.mob;

import com.google.common.base.Predicate;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftBlaze;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.game.DataInstance;

import javax.annotation.Nullable;

import java.util.UUID;

import static org.bukkit.Bukkit.getServer;
@Getter
public class RuslanBlaze extends EntityBlaze implements EntityOwnable {
    
    private final String team;
    private final Entity owner;
    
    public RuslanBlaze(Location loc, String team, net.minecraft.server.v1_12_R1.EntityPlayer owner) {
        super(((CraftWorld)loc.getWorld()).getHandle());
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());
        this.setYawPitch(loc.getYaw(), loc.getPitch());
        this.bukkitEntity = new CraftBlaze((CraftServer) getServer(), this);
        this.team = team;
        this.owner = owner;
    }
    
    @Override
    protected void r() {
        this.goalSelector.a(4, new PathfinderGoalBlazeFireball(this));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0));
        this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0, 0.0F));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.targetSelector.a(2, new remonone.nftilation.game.mob.goals.PathfinderGoalOwnerHurtByTarget(this));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityLiving.class, 10, true, true, new Predicate<EntityLiving>() {
            public boolean checkIfEnemy(@Nullable EntityLiving entityinsentient) {
                if(entityinsentient == null) return false;
                CraftEntity entity = entityinsentient.getBukkitEntity();
                if(EntityHandleComponent.isEntityHostile(entity)) {
                    return true;
                }
                if(entity instanceof Player) {
                    Player target = (Player) entity;
                    DataInstance.PlayerInfo info = Store.getInstance().getDataInstance().FindPlayerByName(target.getUniqueId());
                    if(info == null) return false;
                    PlayerData data = info.getData();
                    if(data == null || !ObjectUtils.notEqual(data.getRole(), PlayerRole.PLAYER)) return false;
                    TeamData teamData = data.getTeam();
                    if(teamData == null || StringUtils.isEmpty(teamData.getTeamName())) return false;
                    return !data.getTeam().getTeamName().equals(team);
                }
                if(!(entity instanceof LivingEntity)) return false;
                Player owner = EntityHandleComponent.getEntityOwner(entity);
                if(owner != null) {
                    DataInstance.PlayerInfo info = Store.getInstance().getDataInstance().FindPlayerByName(owner.getUniqueId());
                    if(info == null) return false;
                    PlayerData data = info.getData();
                    if(data == null || !ObjectUtils.notEqual(data.getRole(), PlayerRole.PLAYER)) return false;
                    TeamData teamData = data.getTeam();
                    if(teamData == null || StringUtils.isEmpty(teamData.getTeamName())) return false;
                    return !data.getTeam().getTeamName().equals(team);
                }
                return false;
            }

            public boolean apply(@Nullable EntityLiving object) {
                return this.checkIfEnemy(object);
            }
        }));
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return owner.getUniqueID();
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return owner;
    }

    static class PathfinderGoalBlazeFireball extends PathfinderGoal {
        private final EntityBlaze a;
        private int b;
        private int c;

        public PathfinderGoalBlazeFireball(EntityBlaze var1) {
            this.a = var1;
            this.a(3);
        }

        public boolean a() {
            EntityLiving var1 = this.a.getGoalTarget();
            return var1 != null && var1.isAlive();
        }

        public void c() {
            this.b = 0;
        }

        public void d() {
            this.a.a(false);
        }

        public void e() {
            --this.c;
            EntityLiving var1 = this.a.getGoalTarget();
            double var2 = this.a.h(var1);
            if (var2 < 4.0) {
                if (this.c <= 0) {
                    this.c = 20;
                    this.a.B(var1);
                }

                this.a.getControllerMove().a(var1.locX, var1.locY, var1.locZ, 1.0);
            } else if (var2 < this.f() * this.f()) {
                double var4 = var1.locX - this.a.locX;
                double var6 = var1.getBoundingBox().b + (double)(var1.length / 2.0F) - (this.a.locY + (double)(this.a.length / 2.0F));
                double var8 = var1.locZ - this.a.locZ;
                if (this.c <= 0) {
                    ++this.b;
                    if (this.b == 1) {
                        this.c = 60;
                        this.a.a(true);
                    } else if (this.b <= 4) {
                        this.c = 6;
                    } else {
                        this.c = 100;
                        this.b = 0;
                        this.a.a(false);
                    }

                    if (this.b > 1) {
                        float var10 = MathHelper.c(MathHelper.sqrt(var2)) * 0.5F;
                        this.a.world.a(null, 1018, new BlockPosition((int)this.a.locX, (int)this.a.locY, (int)this.a.locZ), 0);

                        for(int var11 = 0; var11 < 1; ++var11) {
                            EntitySmallFireball var12 = new EntitySmallFireball(this.a.world, this.a, var4 + this.a.getRandom().nextGaussian() * (double)var10, var6, var8 + this.a.getRandom().nextGaussian() * (double)var10);
                            var12.locY = this.a.locY + (double)(this.a.length / 2.0F) + 0.5;
                            this.a.world.addEntity(var12);
                        }
                    }
                }

                this.a.getControllerLook().a(var1, 10.0F, 10.0F);
            } else {
                this.a.getNavigation().p();
                this.a.getControllerMove().a(var1.locX, var1.locY, var1.locZ, 1.0);
            }

            super.e();
        }

        private double f() {
            AttributeInstance var1 = this.a.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
            return var1 == null ? 16.0 : var1.getValue();
        }
    }
}
