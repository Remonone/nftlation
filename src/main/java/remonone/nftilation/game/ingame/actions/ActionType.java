package remonone.nftilation.game.ingame.actions;

import lombok.Getter;

@Getter
public enum ActionType {
    CHECKER(""),
    ROBOSYBIL_ATTACK(""),
    HAMSTER(""),
    CRYPT_DROP(""),
    GOLDEN_APPLE("Золотое яблоко"),
    INSPIRATION("Вдохновение"),
    AIR_DROP("Air Drop"),
    CRYPT_ARISE("Рост крипты"),
    METEOR_FALL("Падение метеорита"),
    AIRSTRIKE("Бомбардировка"),
    DDOS_ATTACK("DDoS Атака");

    private final String name;
    
    ActionType(String name) {
        this.name = name;
    }
    
}
