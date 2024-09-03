package remonone.nftilation.effects;

import remonone.nftilation.effects.props.BaseProps;

public interface IEffect<T extends BaseProps> {
    void execute(T props);
}
