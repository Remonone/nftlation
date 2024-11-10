package remonone.nftilation.application.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ActivePlayers {
    private List<String> active_players;
}
