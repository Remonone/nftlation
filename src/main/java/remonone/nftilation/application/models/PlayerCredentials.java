package remonone.nftilation.application.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerCredentials {
    public String login;
    public String password;
}
