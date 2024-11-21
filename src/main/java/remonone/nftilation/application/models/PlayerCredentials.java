package remonone.nftilation.application.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class  PlayerCredentials {
    private String login;
    private String password;
}