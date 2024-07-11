package remonone.nftilation.application.services;

import org.bukkit.ChatColor;
import remonone.nftilation.application.models.PlayerCredentials;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.enums.PlayerRole;

import java.util.*;
import java.util.function.Function;

public class MiddlewareService {

    public static List<TeamData> teams = new ArrayList<TeamData>() {{
        add(new TeamData("BluePr1de", "BPR", ChatColor.BLUE.getChar(), true));
        add(new TeamData("Red", "RD", ChatColor.RED.getChar(), false));
    }};

    public static int counter = 0;

    public static void logInPlayer(PlayerCredentials credentials, Function<PlayerData, Void> onData, Function<String, Void> onFail) {
        Thread thread = new Thread(() -> {
            try {
//                PlayerData data = HttpRequestSender.post(RequestConstant.REQ_PLAYER_LOG_IN, credentials, PlayerData.class);
                TeamData data = teams.get(counter);
                counter = counter == 0 ? 1 : 0;
                onData.apply(new PlayerData(credentials.getLogin(), PlayerRole.PLAYER, data));
            } catch(Exception ex) {
                onFail.apply(ex.getMessage());
            }
        });
        thread.start();

    }

    public static void fetchTeams(Function<List<TeamData>, Void> onFetch, Function<String,Void> onFail) {
        Thread tread = new Thread(() -> {
            try {
//                TeamDataFetchResponse response = HttpRequestSender.get(RequestConstant.REQ_FETCH_TEAMS, TeamDataFetchResponse.class);
                onFetch.apply(teams);
            } catch(Exception ex) {
                onFail.apply(ex.getMessage());
            }
        });
        tread.start();
    }

}
