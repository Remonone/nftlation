package remonone.nftilation.application.models;

import lombok.Data;

@Data
public class TeamData {
    private String teamName;
    private String teamShort;
    private char teamColor;
    private boolean isActive;
}
