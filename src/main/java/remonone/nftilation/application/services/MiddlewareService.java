package remonone.nftilation.application.services;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.*;
import remonone.nftilation.constants.RequestConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.HttpRequestSender;
import remonone.nftilation.utils.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class MiddlewareService {

    public final static List<TeamData> teams = new ArrayList<TeamData>() {{
        add(new TeamData("Red", "RD", ChatColor.RED.getChar()));
        add(new TeamData("Yellow", "YL", ChatColor.YELLOW.getChar()));
        add(new TeamData("Green", "GR", ChatColor.GREEN.getChar()));
        add(new TeamData("Blue", "BL", ChatColor.BLUE.getChar()));
    }};


    public static PlayerData logInPlayer(PlayerCredentials credentials, boolean isAdmin) {
        try {
//                PlayerData data = HttpRequestSender.post(RequestConstant.REQ_PLAYER_LOG_IN, credentials, PlayerData.class);
            TeamData data = teams.stream().filter(teamData -> teamData.getTeamName().equals(credentials.team)).findFirst().orElse(null);
            if(data == null) {
                throw new Exception("Team name have been not specified or wrong...");
            }
            return new PlayerData(credentials.getLogin(), isAdmin ? PlayerRole.ADMIN : PlayerRole.PLAYER, isAdmin ? null : data);
        } catch(Exception ex) {
            return null;
        }
    }

    public static List<TeamData> fetchTeams() {
        return teams;
    }

    public static void loadSkins() {
        for(Role role : Role.getRoles()) {
//            try{
//                SkinResponse response = HttpRequestSender.get(RequestConstant.REQ_GET_SKINS + "?game_role=" + role.getRoleID(), SkinResponse.class);
//                SkinCache.getInstance().storeSkin(role.getRoleID(), response.getSkin(), response.getSign());
//                Logger.log("Skin for " + role.getRoleID() + " has been successfully loaded");
//            } catch(Exception ex) {
//                Logger.error("Could not load skin: " + role.getRoleName());
//            }

        }
    }
    public static boolean applyPlayers() {
        List<String> activePlayers = Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> Store.getInstance()
                        .getDataInstance()
                        .FindPlayerByName(player.getUniqueId())
                        .getData()
                        .getRole()
                        .equals(PlayerRole.PLAYER))
                .map(player -> Store.getInstance()
                        .getDataInstance()
                        .FindPlayerByName(player.getUniqueId())
                        .getData()
                        .getLogin())
                .collect(Collectors.toList());
        try{
            HttpRequestSender.post(RequestConstant.REQ_SEND_APPLY, new ActivePlayers(activePlayers), Object.class);
            return true;
        } catch(Exception e) {
            Logger.error("Cannot apply players: " + e.getMessage());
            return false;
        }
    }
    
    public static void confirmDonation(Donation donation) {
        try {
            HttpRequestSender.get(RequestConstant.REQ_CONFIRM_DONATION + donation.getDonation_id(), Object.class);

        } catch (Exception e) {
            Logger.error("Failed to update the state of donation with id " + donation.getDonation_id());
        }
    }
}