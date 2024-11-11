package remonone.nftilation.effects.strategies;

import lombok.AllArgsConstructor;
import org.bukkit.util.Vector;

@AllArgsConstructor
public class ParticleRepulsionStrategy implements IParticleStrategy {
    private final Vector repulsionPoint;
    private final double force;

    @Override
    public ParticleStrategyOutput calculateStrategy(Vector position) {
        Vector clone = position.clone();
        ParticleStrategyOutput output = new ParticleStrategyOutput();
        output.setOffset(clone.subtract(repulsionPoint).normalize());
        output.setExtra(force);
        output.setCount(0);
        return output;
    }
}
