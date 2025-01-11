package remonone.nftilation.hints;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@Builder
@AllArgsConstructor
@Getter
public class Hint implements ConfigurationSerializable {
    private String data;
    private Location location;
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("data", this.data);
        data.put("location", this.location);
        return data;
    }
    
    public static Hint deserialize(Map<String, Object> map) {
        String data = (String) map.get("data");
        Location location = (Location) map.get("location");
        return Hint.builder().data(decodeData(data)).location(location).build();
    }

    private static String decodeData(String data) {
        StringBuilder builder = new StringBuilder();
        while(data.contains("+&")) {
            int start = data.indexOf("+&");
            char ch = data.charAt(start+2);
            ChatColor color = ChatColor.getByChar(ch);
            builder.append(data, 0, start);
            builder.append(color);
            data = data.substring(start+3);
        }
        builder.append(data);
        return builder.toString();
    }
}
