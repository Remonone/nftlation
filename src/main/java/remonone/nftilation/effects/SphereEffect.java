package remonone.nftilation.effects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import remonone.nftilation.effects.props.SphereProps;
import remonone.nftilation.effects.strategies.IParticleStrategy;
import remonone.nftilation.effects.strategies.ParticleStrategyOutput;

import java.util.ArrayList;
import java.util.List;

public class SphereEffect implements IEffect<SphereProps>{
    @Override
    public void execute(SphereProps props) {
        List<Location> positions = getPositions(props.getWorld(), props.getRadius(), props.getDensity());
        IParticleStrategy strategy = props.getParticleStrategy();
        for(Location l : positions) {
            l.add(props.getCenter());
            ParticleStrategyOutput output = strategy.calculateStrategy(l.toVector());
            Vector offset = output.getOffset();
            props.getWorld().spawnParticle(props.getParticle(), l, output.getCount(), offset.getX(), offset.getY(), offset.getZ());
        }
    }
    
    private List<Location> getPositions(World world, double radius, int amount) {
        List<Location> positions = new ArrayList<>();
        double phi = Math.PI * (Math.sqrt(5.) - 1.);
        for(int i = 0; i < amount; i++) {
            double y = 1 - (i / (float)(amount - 1)) * 2 ;
            double r = Math.sqrt(1 - y * y);
            double theta = phi * i;
    
            double x = Math.cos(theta) * r * radius;
            double z = Math.sin(theta) * r * radius;

            positions.add(new Location(world, x, y * radius, z));
        }
        return positions;
    }
}
