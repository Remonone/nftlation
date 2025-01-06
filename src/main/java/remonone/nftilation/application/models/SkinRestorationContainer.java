package remonone.nftilation.application.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinRestorationContainer {
    private String id;
    private String name;
    private List<SkinProperty> properties;
}
