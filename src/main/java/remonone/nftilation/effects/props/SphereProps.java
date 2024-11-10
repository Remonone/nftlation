package remonone.nftilation.effects.props;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.bukkit.util.Vector;

@SuperBuilder
@Getter
public class SphereProps extends BaseProps{
    private Vector center;
    private double radius;
    private int density;
}
