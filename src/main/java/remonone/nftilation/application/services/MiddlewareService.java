package remonone.nftilation.application.services;

import org.bukkit.ChatColor;
import remonone.nftilation.application.models.PlayerCredentials;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.enums.PlayerRole;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MiddlewareService {
    
    public static void logInPlayer(PlayerCredentials credentials, Function<PlayerData, Void> onData, Function<String, Void> onFail) {
        Thread thread = new Thread(() -> {
            try {
//                PlayerData data = HttpRequestSender.post(RequestConstant.REQ_PLAYER_LOG_IN, credentials, PlayerData.class);
                TeamData data = new TeamData();
                data.setActive(true);
                data.setTeamName("BluePr1de");
                data.setTeamShort("BPR");
                data.setTeamColor(ChatColor.LIGHT_PURPLE.getChar());
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
                List<TeamData> teams = new ArrayList<>();
                TeamData data = new TeamData();
                data.setActive(true);
                data.setTeamName("BluePr1de");
                data.setTeamShort("BPR");
                data.setTeamColor(ChatColor.LIGHT_PURPLE.getChar());
                teams.add(data);
                onFetch.apply(teams);
            } catch(Exception ex) {
                onFail.apply(ex.getMessage());
            }
        });
        tread.start();
    }
    
}
