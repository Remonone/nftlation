package remonone.nftilation.application.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinProperty {
    private String name;
    private String value;
    private String signature;
}