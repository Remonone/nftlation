package remonone.nftilation.effects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import remonone.nftilation.effects.props.LineProps;
import remonone.nftilation.effects.strategies.ParticleStrategyOutput;

public class LineEffect implements IEffect<LineProps> {
    @Override
    public void execute(LineProps props) {
        World world = props.getWorld();
        Vector from = props.getFrom().clone();
        Vector to = props.getTo().clone();
        Vector direction = to.subtract(from).normalize();
        direction.multiply(props.getStep());
        Location stepper = new Location(world, from.getX(), from.getY(), from.getZ());
        while(stepper.toVector().distance(props.getTo()) > 1F) {
            ParticleStrategyOutput output = props.getParticleStrategy().calculateStrategy(stepper.toVector());
            Vector offset = output.getOffset();
            world.spawnParticle(props.getParticle(), stepper, output.getCount(), offset.getX(), offset.getY(), offset.getZ());
            stepper.add(direction);
        }
    }
}
