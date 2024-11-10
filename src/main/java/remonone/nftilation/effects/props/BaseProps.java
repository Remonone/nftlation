package remonone.nftilation.effects.props;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Particle;
import org.bukkit.World;
import remonone.nftilation.effects.strategies.IParticleStrategy;

@Getter
@SuperBuilder
public abstract class BaseProps {
    protected Particle particle;
    protected World world;
    protected IParticleStrategy particleStrategy;
}
