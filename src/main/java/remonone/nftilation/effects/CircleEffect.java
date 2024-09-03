package remonone.nftilation.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;
import remonone.nftilation.effects.props.CircleProps;
import remonone.nftilation.utils.MathUtils;

public class CircleEffect implements IEffect<CircleProps> {
    @Override
    public void execute(CircleProps props) {
        World world = props.getWorld();
        Vector center = props.getCenter();
        Location centerLocation = new Location(world, center.getX(), center.getY(), center.getZ());
        for(float rotation = 0; rotation < 360; rotation += 2) {
            Vector rotationVector = MathUtils.getRotationVector(rotation);
            Location pos = centerLocation.clone().add(rotationVector.multiply(props.getRadius()).add(props.getOffset()));
            world.spawnParticle(Particle.SPELL_MOB, pos, props.getCount(), props.getOffsetX(), props.getOffsetY(), props.getOffsetZ());
        }
    }
}
