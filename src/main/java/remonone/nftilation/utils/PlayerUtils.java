package remonone.nftilation.utils;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Role;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerUtils {
    
    public static boolean trySendMessageOnProhibited(Player player, PlayerData data) {
        if(data == null || data.getRole() == PlayerRole.PLAYER) {
            player.sendMessage(ChatColor.RED + MessageConstant.PERMISSION_LOCKED);
            return true;
        }
        if(Store.getInstance().getGameStage().getStage() != Stage.IDLE) {
            player.sendMessage(ChatColor.RED + MessageConstant.STATE_NOT_IDLE);
            return true;
        }
        return false;
    }

    public static boolean validateParams(Map<String, Object> playerParams) {
        final String emptyRole = "_";
        String roleId = playerParams.getOrDefault(PropertyConstant.PLAYER_ROLE_ID, emptyRole).toString();
        if(roleId.equals(emptyRole)) return false;
        if(Role.getRoleByID(roleId) == null) return false;
        return playerParams.containsKey(PropertyConstant.PLAYER_LEVEL_PARAM)
                && playerParams.containsKey(PropertyConstant.PLAYER_KILL_COUNT)
                && playerParams.containsKey(PropertyConstant.PLAYER_DEATH_COUNT);
    }
    public static List<Player> getPlayersFromTeam(ITeam team) {
        return team.getPlayers().stream().map(PlayerModel::getReference).collect(Collectors.toList());
    }

    public static List<Player> getPlayersFromTeam(String teamName) {
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        return getPlayersFromTeam(team);
    }

    public static boolean isTeamHaveAlivePlayers(ITeam team) {
        if(team == null) return false;
        return team.getPlayers().stream().anyMatch(model -> (Boolean) model.getParameters().getOrDefault(PropertyConstant.PLAYER_IS_ALIVE_PARAM, false));
    }

    public static boolean isTeamHaveAlivePlayers(String teamName) {
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        if(team == null) return false;
        return isTeamHaveAlivePlayers(team);
    }
    
    public static AttackerInfo getAttackerPlayer(Entity damager) {
        if(damager instanceof Player) {
            return new AttackerInfo((Player) damager, damager);
        }
        if(damager instanceof Arrow
                || damager instanceof TNTPrimed
                || damager instanceof AreaEffectCloud) {
            Player player = EntityHandleComponent.getEntityOwner(damager);
            if(player == null) return null;
            return new AttackerInfo(player, damager);
        }
        if(damager instanceof Fireball) {
            Fireball fireball = (Fireball) damager;
            if(fireball.getShooter() instanceof Player) {
                return new AttackerInfo((Player) fireball.getShooter(), (Player) fireball.getShooter());
            }
            Player shooter = EntityHandleComponent.getEntityOwner((Entity)fireball.getShooter());
            if(shooter == null) return null;
            return new AttackerInfo(shooter, (Entity)fireball.getShooter());
        }

        return null;
    }
    
    public static PlayerModel getModelFromPlayer(Player player) {
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        return GameInstance.getInstance().getPlayerModelFromTeam(teamName, player);
    }
    
    @AllArgsConstructor
    public static class AttackerInfo {
        public Player attacker;
        public Entity source;
    }
}
