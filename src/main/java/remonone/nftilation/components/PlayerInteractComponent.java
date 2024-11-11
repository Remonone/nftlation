package remonone.nftilation.components;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.roles.Berserk;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class PlayerInteractComponent implements IComponent {

    private GameInstance instance;
    public List<ITeam> teams;

    @Override
    public void initComponent() {
        instance = GameInstance.getInstance();
        teams = new ArrayList<>();
        instance.getTeamIterator().forEachRemaining(teams::add);
        if(teams.isEmpty()) {
            throw new RuntimeException("No teams found");
        }
    }

    public boolean adjustPlayerTokens(Player player, float tokens, TransactionType type) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        return adjustPlayerTokens(model, tokens, type);
    }

    /**
     * @param model Player model
     * @param tokens Tokens to give(positive/negative)
     * @param type Transaction type: GAIN, SPEND, TRANSFER
     * @return state if operation was successful
     */
    public boolean adjustPlayerTokens(PlayerModel model, float tokens, TransactionType type) {
        if(tokens == 0) return false;
        if(model.getTokens() + tokens < 0) return false;
        OnTokenTransactionEvent e = new OnTokenTransactionEvent(type, tokens, model);
        getServer().getPluginManager().callEvent(e);
        if(e.isCancelled()) return false;
        float roundedTokens = model.getTokens() + e.getTokensAmount();
        roundedTokens = (float) Math.round(roundedTokens * 100) / 100;
        model.setTokens(roundedTokens);
        ScoreboardHandler.updateScoreboard(model);
        return true;
    }

    public void increasePlayerKillCounter(String teamName, Player player) {
        PlayerModel model = instance.getPlayerModelFromTeam(teamName, player);
        Map<String, Object> params = model.getParameters();
        int killCount = (Integer) params.getOrDefault(PropertyConstant.PLAYER_KILL_COUNT, 0);
        params.put(PropertyConstant.PLAYER_KILL_COUNT, ++killCount);
        ScoreboardHandler.updateScoreboard(model);
    }

    public void increasePlayerDeathCounter(String teamName, Player player) {
        PlayerModel model = instance.getPlayerModelFromTeam(teamName, player);
        Map<String, Object> params = model.getParameters();
        int killCount = (Integer) params.getOrDefault(PropertyConstant.PLAYER_DEATH_COUNT, 0);
        params.put(PropertyConstant.PLAYER_DEATH_COUNT, ++killCount);
        ScoreboardHandler.updateScoreboard(model);
    }
    
    public static boolean isPlayerNotAbleToUpgrade(Player player, int nextLevel) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        Map<String, Object> params = model.getParameters();
        
        if(!params.containsKey(PropertyConstant.PLAYER_LEVEL_PARAM)) {
            Logger.error("Cannot fetch upgrade level for player: " + player.getDisplayName());
            return true;
        }
        if(nextLevel - (int)params.get(PropertyConstant.PLAYER_LEVEL_PARAM) != 1) {
            player.sendMessage(ChatColor.RED + MessageConstant.INCORRECT_UPGRADE_LEVEL);
            return true;
        }
        if((int) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_AVAILABLE_TIER, 1) < nextLevel) {
            player.sendMessage(ChatColor.RED + MessageConstant.INCORRECT_STAGE_FOR_UPGRADE);
            return true;
        }
        return false;
    }

    public void upgradePlayer(Player player, int level) {
        if(isPlayerNotAbleToUpgrade(player, level)) return;
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        Map<String, Object> params = model.getParameters();
        
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, .5f, 1f);
        params.put(PropertyConstant.PLAYER_LEVEL_PARAM, level);

        Role role = Role.getRoleByID(model.getParameters().getOrDefault(PropertyConstant.PLAYER_ROLE_ID, "_").toString());
        if(role == null) {
            Logger.error("Cannot upgrade level for player: " + player.getDisplayName());
            return;
        }
        if(role instanceof Berserk) {
            if(level == 2) {
                Logger.broadcast(ChatColor.RED + "Мишка потерял концентрацию и его внимание расплывчато!");
            }
            if(level == 3) {
                Logger.broadcast(ChatColor.DARK_RED + "Мишка сильно ослаб и находится в предсмертном состоянии!");
            }
        }
        Role.setInventoryItems(model);
        Role.updatePlayerAbilities(player);
        ScoreboardHandler.updateScoreboard(model);
    }

    @Override
    public String getName() {
        return NameConstants.PLAYER_INTERACT_NAME;
    }
}
