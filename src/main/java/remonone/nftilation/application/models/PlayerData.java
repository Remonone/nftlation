package remonone.nftilation.application.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import remonone.nftilation.enums.PlayerRole;

@Data
@AllArgsConstructor
public class PlayerData {
    private String login;
    private PlayerRole role;
    private TeamData team;
}
