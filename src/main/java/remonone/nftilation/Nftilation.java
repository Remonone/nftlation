package remonone.nftilation;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import remonone.nftilation.application.services.MiddlewareService;
import remonone.nftilation.commands.*;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.game.ingame.actions.ActionType;
import remonone.nftilation.game.ingame.actions.donate.*;
import remonone.nftilation.game.ingame.actions.world.*;
import remonone.nftilation.game.models.*;
import remonone.nftilation.game.services.LobbyService;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.meta.RoleInfo;
import remonone.nftilation.game.meta.RuneInfo;
import remonone.nftilation.game.roles.*;
import remonone.nftilation.game.runes.*;
import remonone.nftilation.game.shop.ShopBuilder;
import remonone.nftilation.game.shop.content.CategoryElement;
import remonone.nftilation.game.shop.content.ItemElement;
import remonone.nftilation.game.shop.content.ServiceElement;
import remonone.nftilation.game.shop.registry.ShopItemRegistry;
import remonone.nftilation.handlers.*;
import remonone.nftilation.hints.Hint;
import remonone.nftilation.restore.PlayerCollection;
import remonone.nftilation.restore.TeamCollection;
import remonone.nftilation.restore.WorldCollection;
import remonone.nftilation.utils.CustomEntities;
import remonone.nftilation.utils.EntityList;
import remonone.nftilation.utils.Logger;

public final class Nftilation extends JavaPlugin {
    
    @Override
    public void onEnable() {
        Logger.log("Starting...");
        CustomEntities.registerEntities();
        SerializeProperties();
        ShopBuilder.getInstance().Load();
        ConfigManager.getInstance().Load();
        MetaConfig.getInstance().Load();
        InitHandlers();
        InitCommands();
        RegisterRoles();
        RegisterRunes();
        FetchRoleSkins();
        InitActions();
    }

    private void RegisterRunes() {
        Logger.log("Registering runes...");
        Rune.registerRune(GreedRune.class);
        Rune.registerRune(HasteRune.class);
        Rune.registerRune(BloodLustRune.class);
        Rune.registerRune(ReaperRune.class);
        Rune.registerRune(TimeFrostRune.class);
    }

    private void FetchRoleSkins() {
        Logger.log("Fetching role skins...");
        MiddlewareService.loadSkins();
    }

    private void InitActions() {
        Logger.log("Initializing actions...");
        ActionContainer.registerAction(ActionType.CRYPT_DROP, new CryptDrop());
        ActionContainer.registerAction(ActionType.HAMSTER, new Hamster());
        ActionContainer.registerAction(ActionType.ROBOSYBIL_ATTACK, new RoboSybil());
        ActionContainer.registerAction(ActionType.CHECKER, new Checker());
        ActionContainer.registerAction(ActionType.MONEY_RAIN, new MoneyRain());
        ActionContainer.registerAction(ActionType.TOTAL_SALE, new TotalSale());
        ActionContainer.registerAction(ActionType.HOT_SUMMER, new ColdWinter());
        ActionContainer.registerAction(ActionType.MASSIVE_DELIRIUM, new MassiveDelirium());
        ActionContainer.registerAction(ActionType.WATCHER_BOSS, new WatcherBoss());
        
        ActionContainer.registerAction(ActionType.INSPIRATION, new Inspiration());
        ActionContainer.registerAction(ActionType.AIRSTRIKE, new AirStrike());
        ActionContainer.registerAction(ActionType.AIR_DROP, new AirDrop());
        ActionContainer.registerAction(ActionType.CRYPT_ARISE, new CryptRaise());
        ActionContainer.registerAction(ActionType.METEOR_FALL, new MeteorFall());
        ActionContainer.registerAction(ActionType.DDOS_ATTACK, new DDoSAttack());
        ActionContainer.registerAction(ActionType.GOLDEN_APPLE, new GoldenApple());
    }

    private void SerializeProperties() {
        Logger.log("Serializing properties...");
        ConfigurationSerialization.registerClass(TeamSpawnPoint.class);
        ConfigurationSerialization.registerClass(CategoryElement.class);
        ConfigurationSerialization.registerClass(ItemElement.class);
        ConfigurationSerialization.registerClass(ServiceElement.class);
        ConfigurationSerialization.registerClass(RoleInfo.class);
        ConfigurationSerialization.registerClass(RuneInfo.class);
        ConfigurationSerialization.registerClass(RoleItemDispenser.EnchantInfo.class);
        ConfigurationSerialization.registerClass(AttributeModifier.class);
        ConfigurationSerialization.registerClass(EffectPotion.class);
        ConfigurationSerialization.registerClass(RequisiteContainer.class);
        ConfigurationSerialization.registerClass(Requisite.class);
        ConfigurationSerialization.registerClass(MetaConfig.ContentInfo.class);
        ConfigurationSerialization.registerClass(MetaConfig.GlobalEvent.class);
        ConfigurationSerialization.registerClass(Hint.class);
        ConfigurationSerialization.registerClass(PhaseProps.class);
        ConfigurationSerialization.registerClass(TeamCollection.class);
        ConfigurationSerialization.registerClass(PlayerCollection.class);
        ConfigurationSerialization.registerClass(WorldCollection.class);
    }

    private void RegisterRoles() {
        Logger.log("Registering roles...");
        Role.registerRole(SybilAttacker.class);
        Role.registerRole(Cryptan.class);
        Role.registerRole(Cryptomarine.class);
        Role.registerRole(RuslanEth.class);
        Role.registerRole(Indian.class);
        Role.registerRole(Monkey.class);
        Role.registerRole(CyberExpert.class);
        Role.registerRole(Watcher.class);
        Role.registerRole(Berserk.class);
    }

    private void InitHandlers() {
        Logger.log("Registering event handlers...");
        getServer().getPluginManager().registerEvents(new PlayerLoginHandler(), this);
        getServer().getPluginManager().registerEvents(new JoinPlayerHandler(), this);
        getServer().getPluginManager().registerEvents(new StageHandler(), this);
        getServer().getPluginManager().registerEvents(new OnChatEvent(), this);
        getServer().getPluginManager().registerEvents(new LobbyService(), this);
        getServer().getPluginManager().registerEvents(new RoleSelectHandler(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveEvent(), this);
        getServer().getPluginManager().registerEvents(new PhaseUpdateHandler(), this);
        getServer().getPluginManager().registerEvents(new OnBlockDestroyHandler(), this);
        getServer().getPluginManager().registerEvents(new OnLobbyItemInteractHandler(), this);
        getServer().getPluginManager().registerEvents(new OnItemManipulateHandler(), this);
        getServer().getPluginManager().registerEvents(new ShopKeeperInteract(), this);
        getServer().getPluginManager().registerEvents(new OnEntityDieHandler(), this);
        getServer().getPluginManager().registerEvents(new ShopInteractHandler(), this);
        getServer().getPluginManager().registerEvents(new ExplosionDestructionDisable(), this);
        getServer().getPluginManager().registerEvents(new OnChunkUnloadHandler(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerKillHandler(), this);
        getServer().getPluginManager().registerEvents(new OnTokenGainHandler(), this);
        getServer().getPluginManager().registerEvents(new MoneyRainPickupHandler(), this);
        getServer().getPluginManager().registerEvents(new PlayerPerformLogin(), this);
        getServer().getPluginManager().registerEvents(new OnCoreHandler(), this);
    }
    
    private void InitCommands() {
        Logger.log("Instantiating commands...");
        this.getCommand("switchStage").setExecutor(new StageSwitchCommand());
        this.getCommand("login").setExecutor(new AuthLoginCommand());
        this.getCommand("setAdminRoom").setExecutor(new SetAdminRoomCommand());
        this.getCommand("setLobby").setExecutor(new SetLobbyCommand());
        this.getCommand("addTeamPosition").setExecutor(new AddTeamPositionCommand());
        this.getCommand("removeTeamPosition").setExecutor(new RemoveTeamPositionCommand());
        this.getCommand("setTeamCore").setExecutor(new SetTeamCoreBlockCommand());
        this.getCommand("getTeamPositions").setExecutor(new GetTeamInfoCommand());
        this.getCommand("startLobbyStage").setExecutor(new StartLobbyCommand());
        this.getCommand("setShopKeeper").setExecutor(new SetShopKeeperCommand());
        this.getCommand("setDieCenter").setExecutor(new SetDieCenterSpawnCommand());
        this.getCommand("addTokens").setExecutor(new SetTokenCommand());
        this.getCommand("skipPhase").setExecutor(new SkipPhaseCommand());
        this.getCommand("setCenterPosition").setExecutor(new SetCenterPositionCommand());
        this.getCommand("addDiamondPosition").setExecutor(new AddDiamondPlaceSpawnCommand());
        this.getCommand("addRoboSybilPos").setExecutor(new SetRoboSybilSpawnPointCommand());
        this.getCommand("addIronGolemPos").setExecutor(new AddIronGolemPositionCommand());
        this.getCommand("setCheckerTeamPosition").setExecutor(new CheckerChestCommand());
        this.getCommand("startDonationEvent").setExecutor(new StartDonationEventCommand());
        this.getCommand("addAirDropPos").setExecutor(new AddAirDropCommand());
        this.getCommand("setTeamCoreHealth").setExecutor(new SetTeamCoreHealth());
        this.getCommand("giveTokensToPlayer").setExecutor(new GiveTokenToPlayer());
        this.getCommand("reloadProperties").setExecutor(new ReloadConfigCommand());
        this.getCommand("transfer").setExecutor(new TokenTransferCommand());
        this.getCommand("move").setExecutor(new MoveToPlayer());
        this.getCommand("rbfbsafa").setExecutor(new BossReservationCommand());
        this.getCommand("addHint").setExecutor(new AddHintCommand());
        this.getCommand("pauseGame").setExecutor(new PauseGameCommand());
        this.getCommand("setPhase").setExecutor(new SetPhaseCommand());
        this.getCommand("createBackup").setExecutor(new CreateBackupCommand());
        this.getCommand("restore").setExecutor(new RestoreGameCommand());
    }
    
    public static Nftilation getInstance() {
        return getPlugin(Nftilation.class);
    }

    @Override
    public void onDisable() {
        Logger.log("Disabling...");
        EntityList.clearEntities();
        if(GameInstance.getInstance().getCounter() != null) {
            GameInstance.getInstance().getCounter().getBarWorker().getBar().setVisible(false);
        }
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer("Сервер в данный момент перезагружается, перезайдите позже...");
        }
        CustomEntities.unregisterEntities();
    }
    
    
    public void ReloadProperties() {
        Role.getRoles().forEach(Role::resetListeners);
        Role.getRoles().clear();
        Rune.getRunes().clear();
        ShopItemRegistry.clearRegister();
        ShopBuilder.getInstance().Load();
        ConfigManager.getInstance().Load();
        MetaConfig.getInstance().Load();
        
        RegisterRoles();
        RegisterRunes();
    }
}
