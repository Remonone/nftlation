package remonone.nftilation.application.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamData {
    private String teamName;
    private String teamShort;
    private char teamColor;
}
