package remonone.nftilation.game.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import remonone.nftilation.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SerializableAs("Requisites")
@Data
@AllArgsConstructor
public class RequisiteContainer implements ConfigurationSerializable {
    private List<Requisite> requisites;
    
    public boolean checkForRequisites(Map<String, Object> paramsList) {
        List<Requisite> requisites = new ArrayList<>(this.requisites);
        for (Requisite req : requisites) {
            if(!paramsList.containsKey(req.getName())) {
                return false;
            }
            Object value = paramsList.get(req.getName());
            if(!req.isRequisiteFulfilled(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        return Collections.emptyMap();
    }
    
    @SuppressWarnings("unchecked")
    public static RequisiteContainer deserialize(Map<String, Object> map) {
        List<Requisite> requisites;
        if(map.containsKey("list")) {
           requisites = (ArrayList<Requisite>) map.get("list");
        } else {
            requisites = new ArrayList<>();
        }
        return new RequisiteContainer(requisites);
    }
}
