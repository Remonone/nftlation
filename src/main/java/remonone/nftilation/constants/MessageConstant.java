package remonone.nftilation.constants;

public class MessageConstant {
    
    public static final String PERMISSION_LOCKED = "У вас нет прав использовать эту команду";
    public static final String STATE_NOT_IDLE = "You cannot use this command while the stage is not idle.";
    public static final String ADMIN_ROOM_SET = "Admin room has been set to ";
    public static final String LOBBY_ROOM_SET = "Lobby room has been set to ";
    public static final String NO_PERMISSION_TO_JOIN = "На данный момент вы не можете зайти на этот сервер";
    public static final String NO_TOURNAMENT_PRESENTED = "Ваша команда не представлена на турнире!";
    public static final String ALREADY_LOGGED_IN = "Вы уже авторизованы!";
    public static final String SUCCESSFUL_LOGIN = "Вы успешно авторизовались!";
    public static final String JOIN_GAME = " присоеденился к серверу.";
    public static final String UNKNOWN_KICK = "Непредвиденная ошибка во время авторизации! Свяжитесь с администрацией для решения проблемы!";
    public static final String ROLE_RESERVED = "Эта роль уже занята!";
    public static final String ROLE_ERROR = "Ошибка при выборе роли. Попробуйте еще раз...";
    public static final String ROLE_SELECT = "Вы выбрали ";
    public static final String TEAM_NOT_EQUALS = "Failed to start Lobby stage! Team count is not equal to server preset!";
    public static final String CORE_INVULNERABLE = "Ядро на данный момент неуязвимо! Вы не можете ему навредить сейчас!";
    public static final String CORE_DESTROYED_TITLE = "Ваше ядро было уничтожено!";
    public static final String CORE_DESTROYED_SUBTITLE = "Теперь вы не можете возрадиться после смерти!";
    public static final String OTHER_CORE_DESTROYED_TITLE = "Ядро команды %s было уничтожено!";
    public static final String OTHER_CORE_DESTROYED_SUBTITLE = "Вы теперь можете уничтожить их!";
    public static final String CORE_DESTROYED_BROADCAST = "Команда %s потеряло своё ядро!";
    public static final String DIE_CENTER_COMMAND = "Center position for dead player has been set.";
    public static final String PLAYER_ON_DIE = "Вы были убиты!";
    public static final String NOT_ENOUGH_MONEY = "У вас недостаточно токенов для покупки!";
    public static final String ITEM_COOLDOWN = "Предмет на перезарядке! Попробуйте позже. Перезарядка через: ";
    public static final String START_RECALL = "Recall has been started!";
    public static final String INCORRECT_UPGRADE_LEVEL = "You cannot buy this level! Try another one!";
    public static final String INCORRECT_STAGE_FOR_UPGRADE = "You cannot buy this upgrade at current phase!";
    public static final String CANNOT_HEAL_CORE = "You cannot heal core right now!";
    public static final String CANNOT_SPAWN_BLAZE = "Cannot spawn blazes, please try again!";
}
