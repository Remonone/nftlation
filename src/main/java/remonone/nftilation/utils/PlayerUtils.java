package remonone.nftilation.utils;

import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.inventory.InventoryBuilder;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.shop.content.CategoryElement;
import remonone.nftilation.game.shop.content.IExpandable;
import remonone.nftilation.game.shop.registry.ShopItemRegistry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerUtils {
    
    public static boolean trySendMessageOnProhibited(Player player, PlayerData data) {
        if(data == null || data.getRole() == PlayerRole.PLAYER) {
            player.sendMessage(ChatColor.RED + MessageConstant.PERMISSION_LOCKED);
            return true;
        }
        return false;
    }
    
    public static boolean trySendMessageOnWrongStage(Player player) {
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
    
    public static boolean tryWithdrawTokens(Player player, float amount, TransactionType type) {
        PlayerModel model = getModelFromPlayer(player);
        if(model == null) return false;
        return tryWithdrawTokens(model, amount, type);
    }
    
    public static boolean tryWithdrawTokens(PlayerModel model, float amount, TransactionType type) {
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
         if(component == null) return false;
        return component.adjustPlayerTokens(model, -amount, type);
    }
    
    public static ITeam getTeamFromPlayer(Player player) {
        PlayerModel model = getModelFromPlayer(player);
        if(model == null) return null;
        String teamName = (String) model.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        if(StringUtils.isBlank(teamName)) return null;
        return GameInstance.getInstance().getTeam(teamName);
    }

    public static void updateShopInventoryForPlayer(Player player) {
        if(getModelFromPlayer(player) == null) return;
        if(player.getOpenInventory() == null) return; // TODO: check if actually null on closed
        InventoryView view = player.getOpenInventory();
        String inventoryName = view.getTopInventory().getName();
        if(!inventoryName.startsWith(NameConstants.SHOP_TAB)) return;
        String tabName = inventoryName.substring(NameConstants.SHOP_TAB.length());
        IExpandable expandable = ShopItemRegistry.getExpandable(tabName);
        if(!(expandable instanceof CategoryElement)) return;
        CategoryElement element = (CategoryElement) expandable;
        Inventory inventory = InventoryBuilder.buildShopKeeperInventory(player, element);
        player.openInventory(inventory);
    }
    
    @AllArgsConstructor
    public static class AttackerInfo {
        public Player attacker;
        public Entity source;
    }
}
