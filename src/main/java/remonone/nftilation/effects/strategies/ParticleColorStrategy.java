package remonone.nftilation.effects.strategies;

import lombok.AllArgsConstructor;
import org.bukkit.util.Vector;

@AllArgsConstructor
public class ParticleColorStrategy implements IParticleStrategy {
    public Vector rgbColor;
    
    @Override
    public ParticleStrategyOutput calculateStrategy(Vector position) {
        ParticleStrategyOutput output = new ParticleStrategyOutput();
        output.setCount(0);
        output.setOffset(rgbColor);
        output.setExtra(0);
        return output;
    }
}
