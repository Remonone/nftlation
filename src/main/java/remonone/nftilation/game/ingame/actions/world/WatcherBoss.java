package remonone.nftilation.game.ingame.actions.world;

import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameConfiguration;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.PlayerNMSUtil;

import java.util.Map;

public class WatcherBoss implements IAction {
    @SneakyThrows
    @Override
    public void Init(Map<String, Object> params) {
        if(!GameInstance.getInstance().addBossTeam()) {
            throw new Exception("Cannot init watcher boss!");
        }
        ITeam team = GameInstance.getInstance().getTeam(PropertyConstant.TEAM_BOSS);
        PlayerModel model = team.getPlayers().get(0);
        DataInstance.PlayerInfo info = Store.getInstance().getDataInstance().FindPlayerByID(model.getReference().getUniqueId());
        info.getData().setTeam(new TeamData(PropertyConstant.TEAM_BOSS, "BS", '4'));
        GameConfiguration.initPlayerRoles(team);
        team.getPlayers().forEach(Role::refillInventoryWithItems);
        model.getReference().setCustomName(ChatColor.GOLD + "СМОТРИТЕЛЬ");
        PlayerNMSUtil.changePlayerName(model.getReference(), ChatColor.GOLD + "СМОТРИТЕЛЬ");
        model.getReference().teleport(team.getTeamSpawnPoint().getPosition());
        model.getReference().getWorld().setTime(18000L);
    }

    @Override
    public String getTitle() {
        return "Крипто-смотритель";
    }

    @Override
    public String getDescription() {
        return "Бегите, глупцы!";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_WITHER_AMBIENT;
    }
}
