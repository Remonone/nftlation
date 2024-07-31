package remonone.nftilation.game.models;

import remonone.nftilation.game.ingame.core.Core;

public interface IModifiableTeam extends ITeam {
    void setTeamActive(boolean value);
    void setCoreAlive(boolean value);
    Core getCoreInstance();
}
