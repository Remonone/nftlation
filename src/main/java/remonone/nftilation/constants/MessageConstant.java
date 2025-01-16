package remonone.nftilation.constants;

import org.bukkit.ChatColor;

public class MessageConstant {
    
    public static final String PERMISSION_LOCKED = "У вас нет прав использовать эту команду";
    public static final String STATE_NOT_IDLE = "You cannot use this command while the stage is not idle.";
    public static final String ADMIN_ROOM_SET = "Admin room has been set to ";
    public static final String LOBBY_ROOM_SET = "Lobby room has been set to ";
    public static final String NO_PERMISSION_TO_JOIN = "На данный момент вы не можете зайти на этот сервер";
    public static final String NO_TOURNAMENT_PRESENTED = "Ваша команда не представлена на турнире!";
    public static final String ALREADY_LOGGED_IN = "Вы уже авторизованы!";
    public static final String SUCCESSFUL_LOGIN = "Вы успешно авторизовались!";
    public static final String JOIN_GAME = " присоединился к серверу.";
    public static final String UNKNOWN_KICK = "Непредвиденная ошибка во время авторизации! Свяжитесь с администрацией для решения проблемы!";
    public static final String ROLE_RESERVED = "Эта роль уже занята!";
    public static final String ROLE_ERROR = "Ошибка при выборе роли. Попробуйте еще раз...";
    public static final String RUNE_ERROR = "Ошибка при выборе руны. Попробуйте еще раз...";
    public static final String SUCCESSFUL_SELECTION = "Вы выбрали ";
    public static final String TEAM_NOT_EQUALS = "Failed to start Lobby stage! Team count is not equal to server preset!";
    public static final String CORE_INVULNERABLE = "Ядро на данный момент неуязвимо! Вы не можете ему навредить сейчас!";
    public static final String CORE_DESTROYED_TITLE = "Ваше ядро было уничтожено!";
    public static final String CORE_DESTROYED_SUBTITLE = "Теперь вы не можете возродиться после смерти!";
    public static final String OTHER_CORE_DESTROYED_TITLE = "Ядро команды %s было уничтожено!";
    public static final String OTHER_CORE_DESTROYED_SUBTITLE = "Вы теперь можете уничтожить их!";
    public static final String CORE_DESTROYED_BROADCAST = "Команда %s потеряло своё ядро!";
    public static final String DIE_CENTER_COMMAND = "Center position for dead player has been set.";
    public static final String PLAYER_ON_DIE = "Вы были убиты!";
    public static final String NOT_ENOUGH_MONEY = "У вас недостаточно токенов для покупки!";
    public static final String ITEM_COOLDOWN = "Предмет на перезарядке! Попробуйте позже. Перезарядка через: ";
    public static final String START_RECALL = "Возвращение на базу началось!";
    public static final String INCORRECT_UPGRADE_LEVEL = "Вы не можете купить конкретный апгрейд! Попробуйте другой!";
    public static final String INCORRECT_STAGE_FOR_UPGRADE = "Этот апгрейд недоступен для покупки на этой фазе!";
    public static final String CANNOT_HEAL_CORE = "Вы не можете исцелить ядро вашей команды сейчас!";
    public static final String CANNOT_SPAWN_BLAZE = "Невозможно заспавнить блейзов! Попробуйте еще раз!";
    public static final String LINE_SEPARATOR = ChatColor.DARK_RED + "====================";
    public static final String LINE_STARTED = ChatColor.DARK_RED + "[o] " + ChatColor.RESET;
    public static final String TEAM_DAMAGED_MESSAGE = "Вашему ядру нанесли урон! Проучите мерзавца посягнувшего на ваше святилище!";
    public static final String GAME_NOT_START_YET = "Game not started yet!";
    public static final String GAME_PAUSED = "Game is paused!";
    public static final String GAME_RESUMED = "Game is resumed!";
    public static final String TOKEN_TRANSFER_INCORRECT_RECIPIENT = "Неверный ник получателя!";
    public static final String TOKEN_TRANSFER_INSUFFICIENT_AMOUNT = "Неверное количество токенов для перевода!";
    public static final String TOKEN_TRANSFER_IMPOSSIBLE_TO_TRANSFER = "Невозможно перевести токены этому игроку!";
    public static final String TOKEN_TRANSFER_TOO_FAR = "Игрок слишком далеко! Нельзя сделать перевод!";
    public static final String TOKEN_TRANSFER_INCORRECT_STAGE = "Вы не можете совершать переводы в данный момент!";
    public static final String TEAM_UPGRADE = " купил командное улучшение: ";
    public static final String TEAM_UPGRADE_RESOURCE_INCOME = "Увеличенный приток";
    public static final String TEAM_UPGRADE_PASSIVE_INCOME = "Пасивная прибыль";
    public static final String TEAM_UPGRADE_UTILITY = "Вспомогательные инструменты";
    public static final String TEAM_UPGRADE_CORE = "Укрепление ядра";
    public static final String EVENT_PURCHASE = " купил событие: ";
    public static final String PESTILENCE_KILL = "Мор пожрал еще одну душу...";
    public static final String PESTILENCE_START = "Ваше состояние удручающее. Ваша жизнь начала гаснуть...";
    public static final String EMPTY_TARGET = "Цель не выбрана!";
    public static final String PLAYER_TARGET_REQUIRED = "Выбранная цель не является игроком!";
    public static final String PLAYER_IN_SAME_TEAM = "Выбранный игрок находится в вашей команде!";
}
