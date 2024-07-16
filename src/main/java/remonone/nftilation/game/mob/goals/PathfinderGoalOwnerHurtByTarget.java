package remonone.nftilation.game.mob.goals;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;
import remonone.nftilation.game.mob.RuslanBlaze;

public class PathfinderGoalOwnerHurtByTarget extends PathfinderGoalTarget {
    final EntityOwnable a;
    EntityLiving b;
    private int c;

    public PathfinderGoalOwnerHurtByTarget(RuslanBlaze ruslanBlaze) {
        super(ruslanBlaze, false);
        this.a = ruslanBlaze;
        this.a(1);
    }

    public boolean a() {
        
        EntityLiving entityliving = (EntityLiving) this.a.getOwner();
        if (entityliving == null) {
            return false;
        } else {
            this.b = entityliving.getLastDamager();
            int i = entityliving.bT();
            return i != this.c && this.a(this.b, false);
        }
        
    }

    public void c() {
        this.e.setGoalTarget(this.b, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
        EntityLiving entityliving = (EntityLiving) this.a.getOwner();
        if (entityliving != null) {
            this.c = entityliving.bT();
        }

        super.c();
    }
    
}
