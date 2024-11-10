package remonone.nftilation.effects.strategies;

import org.bukkit.util.Vector;

public interface IParticleStrategy {
    ParticleStrategyOutput calculateStrategy(Vector position);
}
