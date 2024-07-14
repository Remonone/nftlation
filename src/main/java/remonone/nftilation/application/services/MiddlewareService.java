package remonone.nftilation.application.services;

import org.bukkit.ChatColor;
import remonone.nftilation.application.models.PlayerCredentials;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.enums.PlayerRole;

import java.util.*;

public class MiddlewareService {

    public static List<TeamData> teams = new ArrayList<TeamData>() {{
        add(new TeamData("Blue", "BL", ChatColor.BLUE.getChar(), true));
        add(new TeamData("Red", "RD", ChatColor.RED.getChar(), true));
        add(new TeamData("Green", "GR", ChatColor.GREEN.getChar(), true));
        add(new TeamData("Yellow", "YL", ChatColor.YELLOW.getChar(), true));
    }};


    public static PlayerData logInPlayer(PlayerCredentials credentials) {
        try {
//                PlayerData data = HttpRequestSender.post(RequestConstant.REQ_PLAYER_LOG_IN, credentials, PlayerData.class);
            TeamData data = teams.stream().filter(teamData -> teamData.getTeamName().equals(credentials.team)).findFirst().orElse(null);
            if(data == null) {
                throw new Exception("Team name have been not specified or wrong...");
            }
            return new PlayerData(credentials.getLogin(), PlayerRole.PLAYER, data);
        } catch(Exception ex) {
            return null;
        }
    }

    public static List<TeamData> fetchTeams() {
        return teams;
    }

}
