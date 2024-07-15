package remonone.nftilation.application.models;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ActivePlayers {
    @NotNull
    private List<String> active_players;
}
