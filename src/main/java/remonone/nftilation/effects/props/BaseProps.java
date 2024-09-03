package remonone.nftilation.effects.props;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

@Getter
@SuperBuilder
public abstract class BaseProps {
    protected Particle particle;
    protected int count;
    protected double offsetX, offsetY, offsetZ;
    protected World world;

    public void setCustomOffset(Vector offset) {
        this.offsetX = offset.getX();
        this.offsetY = offset.getY();
        this.offsetZ = offset.getZ();
    }
}
