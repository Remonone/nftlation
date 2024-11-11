package remonone.nftilation.game.models;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.ChatColor;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.ingame.objects.Core;
import remonone.nftilation.game.ingame.objects.ICoreData;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public class TeamImpl implements IModifiableTeam {
    
    private final UUID uuid = new UUID(50, 10);
    @Getter
    private boolean isCoreAlive;
    @Getter
    private boolean isTeamActive;
    @Getter
    private List<PlayerModel> players;
    private TeamSpawnPoint spawnPoint;
    private Core core;
    @Getter
    private String teamName;
    private ChatColor teamColor;
    @Getter
    private Map<String, Object> parameters;
    
    @Override
    public void setTeamActive(boolean value) {
        this.isTeamActive = value;
    }

    @Override
    public void setCoreAlive(boolean value) {
        this.isCoreAlive = value;
    }

    @Override
    public Core getCoreInstance() {
        return core;
    }

    @Override
    public UUID getTeamID() {
        return uuid;
    }

    @Override
    public ICoreData getCoreData() {
        return core;
    }

    @Override
    public TeamSpawnPoint getTeamSpawnPoint() {
        return spawnPoint;
    }

    @Override
    public boolean isUILayoutExists() {
        return true;
    }

    @Override
    public char getTeamColor() {
        return teamColor.getChar();
    }

    @Override
    public String getTeamInfo() {
        String scoreName = getTeamName() + "[";
        if(isCoreAlive()) {
            scoreName += ChatColor.GREEN + "" + ChatColor.BOLD + "✓";
        } else if(!isTeamActive()) {
            scoreName += ChatColor.DARK_RED + "" + ChatColor.BOLD +  "x";
        } else {
            scoreName += ChatColor.DARK_RED + "" + ChatColor.BOLD + getTeamMembersAlive();
        }
        scoreName += ChatColor.RESET + "]";
        return scoreName;
    }

    private String getTeamMembersAlive() {
        Collection<PlayerModel> alivePlayers = getPlayers().stream().filter(playerModel ->
                (Boolean)playerModel
                        .getParameters()
                        .getOrDefault(PropertyConstant.PLAYER_IS_ALIVE_PARAM, false))
                .collect(Collectors.toList());
        return String.valueOf(alivePlayers.size());
    }
}
