package remonone.nftilation.utils;

import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class BoundingBox {
    //min and max points of hit box
    Vector max;
    Vector min;

    public BoundingBox(Entity entity){
        AxisAlignedBB bb = ((CraftEntity) entity).getHandle().getBoundingBox();
        min = new Vector(bb.a,bb.b,bb.c);
        max = new Vector(bb.d,bb.e,bb.f);
    }

    public Vector midPoint(){
        return max.clone().add(min).multiply(0.5);
    }
}
