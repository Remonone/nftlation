package remonone.nftilation.game.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.utils.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Phase props
 * @JSON duration - Length of phase
 * @JSON bar_color - Color of the bar
 * @JSON bar_style - Style from BarStyle enum
 * @JSON messages_to_send - Messages which sending to players on phase switch
 * @JSON title - Phase title
 * @JSON title_color - Color of the phase
 * @JSON description - Description of the phase
 * @JSON description_color - Color of the description
 * @see BarStyle
 */
@Data
@AllArgsConstructor
@SerializableAs("PhaseProps")
public class PhaseProps implements ConfigurationSerializable {
    private int length;
    private BarColor barColor;
    private BarStyle barStyle;
    private List<String> messagesToSendPlayers;
    private ChatColor phaseTitleColor;
    private String phaseTitle;
    private ChatColor phaseDescriptionColor;
    private String phaseDescription;

    @Override
    public Map<String, Object> serialize() {
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public static PhaseProps deserialize(Map<String, Object> map) {
        int length = 0;
        BarColor barColor = BarColor.WHITE;
        BarStyle barStyle = BarStyle.SOLID;
        List<String> messagesToSendPlayers = Collections.emptyList();
        ChatColor phaseTitleColor = ChatColor.WHITE;
        String phaseTitle = NameConstants.NULL_STRING;
        ChatColor phaseDescriptionColor = ChatColor.WHITE;
        String phaseDescription = NameConstants.NULL_STRING;
        try {
            if(map.containsKey("duration")) {
                length = (Integer) map.get("duration");
            }
            if(map.containsKey("bar_color")) {
                barColor = BarColor.valueOf(map.get("bar_color").toString());
            }
            if(map.containsKey("bar_style")) {
                barStyle = BarStyle.valueOf(map.get("bar_style").toString());
            }
            if(map.containsKey("messages_to_send")) {
                messagesToSendPlayers = (List<String>) map.get("messages_to_send");
            }
            if(map.containsKey("title_color")) {
                phaseTitleColor = ChatColor.valueOf(map.get("title_color").toString());
            }
            if(map.containsKey("description_color")) {
                phaseDescriptionColor = ChatColor.valueOf(map.get("description_color").toString());
            }
            if(map.containsKey("title")) {
                phaseTitle = (String) map.get("title");
            }
            if(map.containsKey("description")) {
                phaseDescription = (String) map.get("description");
            }
        } catch(IllegalArgumentException e) {
            Logger.error("Could not initialize phase info: " + e.getMessage());
        }
        return new PhaseProps(length, barColor, barStyle, messagesToSendPlayers, phaseTitleColor, phaseTitle, phaseDescriptionColor, phaseDescription);
    }
}
