package remonone.nftilation.effects.props;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.bukkit.util.Vector;

@Getter
@SuperBuilder
public class LineProps extends BaseProps {
    private Vector from;
    private Vector to;
    private double step;

}
