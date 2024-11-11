package remonone.nftilation.effects.strategies;

import lombok.AllArgsConstructor;
import org.bukkit.util.Vector;


@AllArgsConstructor
public class ParticleStaticStrategy implements IParticleStrategy {
    private int count;
    private Vector offset;
    
    @Override
    public ParticleStrategyOutput calculateStrategy(org.bukkit.util.Vector position) {
        ParticleStrategyOutput result = new ParticleStrategyOutput();
        result.setCount(count);
        result.setOffset(offset);
        result.setExtra(0);
        return result;
    }
}
