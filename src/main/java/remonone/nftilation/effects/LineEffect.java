package remonone.nftilation.effects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import remonone.nftilation.effects.props.LineProps;

public class LineEffect implements IEffect<LineProps> {
    @Override
    public void execute(LineProps props) {
        double step = props.getStep();
        World world = props.getWorld();
        Vector from = props.getFrom().clone();
        Vector to = props.getTo().clone();
        Vector difference = to.subtract(from);
        double distance = difference.length();
        difference.multiply(props.getStep());
        Location stepper = new Location(world, from.getX(), from.getY(), from.getZ());
        for(double i = 0.0; i < distance; i += step) {
            world.spawnParticle(props.getParticle(), stepper, props.getCount(), props.getOffsetX(), props.getOffsetY(), props.getOffsetZ());
            stepper.add(difference);
        }
    }
}
