package remonone.nftilation.effects;

import org.bukkit.util.Vector;
import remonone.nftilation.effects.props.SpherePlainProps;
import remonone.nftilation.effects.strategies.IParticleStrategy;
import remonone.nftilation.effects.strategies.ParticleStrategyOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpherePlainEffect implements IEffect<SpherePlainProps> {

    private static final Random RANDOM = new Random();

    @Override
    public void execute(SpherePlainProps props) {
        Vector centerVector = props.getSphereGlobalPoint();
        List<Vector> points = getPointsWithinSphere(props.getLocalCenterPoint(), props.getTense(), props.getProjectedSphereRadius(), props.getPlaneRadius());
        IParticleStrategy strategy = props.getParticleStrategy();
        for(Vector point : points) {
            Vector pos = point.add(centerVector).add(props.getShift());
            ParticleStrategyOutput output = strategy.calculateStrategy(pos);
            Vector offset = output.getOffset();
            props.getWorld().spawnParticle(props.getParticle(), pos.getX(), pos.getY(), pos.getZ(), output.getCount(), offset.getX(), offset.getY(), offset.getZ(), output.getExtra());
        }
    }

    private List<Vector> getPointsWithinSphere(Vector pos, int amount, double R, double radius) {
        List<Vector> points = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double dTheta = (RANDOM.nextDouble() - 0.5) * (radius / R) * 2;
            double dPhi = (RANDOM.nextDouble() - 0.5) * (radius / R) * 2;

            // Углы центральной точки
            double theta = Math.acos(pos.getZ() / R);
            double phi = Math.atan2(pos.getY(), pos.getX());

            // Преобразование в декартовы координаты
            double newTheta = theta + dTheta;
            double newPhi = phi + dPhi;

            // Преобразование в декартовы координаты
            double x = R * Math.sin(newTheta) * Math.cos(newPhi);
            double y = R * Math.sin(newTheta) * Math.sin(newPhi);
            double z = R * Math.cos(newTheta);

            points.add(new Vector(x,y,z));
        }
        return points;
    }
}
