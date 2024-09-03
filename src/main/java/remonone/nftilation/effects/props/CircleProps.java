package remonone.nftilation.effects.props;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.bukkit.util.Vector;

@SuperBuilder
@Getter
public class CircleProps extends BaseProps {
    private double minAngle;
    private double maxAngle;
    private double radius;
    private double step;
    private Vector center;
    private Vector offset;
}
