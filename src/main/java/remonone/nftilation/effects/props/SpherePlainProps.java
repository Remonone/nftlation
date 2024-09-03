package remonone.nftilation.effects.props;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.bukkit.util.Vector;

@SuperBuilder
@Getter
public class SpherePlainProps extends BaseProps {
    private Vector sphereGlobalPoint;
    private Vector localCenterPoint;
    private Vector shift;
    private int tense;
    private double projectedSphereRadius;
    private double planeRadius;
}
