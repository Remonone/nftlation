package remonone.nftilation.effects.strategies;

import lombok.AllArgsConstructor;
import org.bukkit.util.Vector;

@AllArgsConstructor
public class ParticleDirectionalStrategy implements IParticleStrategy {
    private Vector destination;
    private double speed;
    
    @Override
    public ParticleStrategyOutput calculateStrategy(Vector position) {
        ParticleStrategyOutput output = new ParticleStrategyOutput();
        Vector dest = destination.clone();
        output.setOffset(dest.subtract(position).normalize());
        output.setCount(0);
        output.setExtra(speed);
        return output;
    }
}
