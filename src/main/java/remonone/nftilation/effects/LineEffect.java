package remonone.nftilation.effects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import remonone.nftilation.effects.props.LineProps;

public class LineEffect implements IEffect<LineProps> {
    @Override
    public void execute(LineProps props) {
        World world = props.getWorld();
        Vector from = props.getFrom().clone();
        Vector to = props.getTo().clone();
        Vector direction = to.subtract(from).normalize();
        direction.multiply(props.getStep());
        Location stepper = new Location(world, from.getX(), from.getY(), from.getZ());
        while(stepper.toVector().distance(to) > 1F) {
            world.spawnParticle(props.getParticle(), stepper, props.getCount(), props.getOffsetX(), props.getOffsetY(), props.getOffsetZ());
            stepper.add(direction);
        }
    }
}
