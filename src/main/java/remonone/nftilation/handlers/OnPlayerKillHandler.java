package remonone.nftilation.handlers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnPlayerKillPlayerEvent;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.GameInstance;

public class OnPlayerKillHandler implements Listener {
    @EventHandler
    public void onPlayerKill(OnPlayerKillPlayerEvent e) {
        Player attacker = e.getKiller().getReference();
        Player victim = e.getVictim().getReference();
        e.getKiller().getReference().playSound(attacker.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
        if(!e.getKiller().getParameters().containsKey(PropertyConstant.PLAYER_TEAM_NAME)) return;
        String attackerTeam = (String)e.getKiller().getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        if(!e.getVictim().getParameters().containsKey(PropertyConstant.PLAYER_TEAM_NAME)) return;
        String victimTeam = (String)e.getVictim().getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        PlayerInteractComponent playerInteract = (PlayerInteractComponent)GameInstance.getComponentByName("PlayerInteract");
        playerInteract.adjustPlayerTokens(e.getKiller(), DataConstants.TOKEN_PER_KILL, OnTokenTransactionEvent.TransactionType.GAIN);
        playerInteract.increasePlayerKillCounter(attackerTeam, attacker);
        playerInteract.increasePlayerDeathCounter(victimTeam, victim);
    }
}
