package remonone.nftilation.application.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamData {
    private String teamName;
    private String teamShort;
    private char teamColor;
    private boolean isActive;
}
