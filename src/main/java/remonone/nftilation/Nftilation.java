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
import remonone.nftilation.game.ingame.actions.world.Checker;
import remonone.nftilation.game.ingame.actions.world.CryptDrop;
import remonone.nftilation.game.ingame.actions.world.Hamster;
import remonone.nftilation.game.ingame.actions.world.RoboSybil;
import remonone.nftilation.game.lobby.LobbyDisposer;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.meta.RoleInfo;
import remonone.nftilation.game.meta.RuneInfo;
import remonone.nftilation.game.roles.*;
import remonone.nftilation.game.runes.GreedRune;
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.game.shop.ShopBuilder;
import remonone.nftilation.game.shop.content.CategoryElement;
import remonone.nftilation.game.shop.content.ItemElement;
import remonone.nftilation.game.shop.content.ServiceElement;
import remonone.nftilation.game.shop.content.ShopItemPosition;
import remonone.nftilation.game.shop.registry.ShopItemRegistry;
import remonone.nftilation.handlers.*;
import remonone.nftilation.utils.CustomEntities;
import remonone.nftilation.utils.EntityList;
import remonone.nftilation.utils.Logger;


public final class Nftilation extends JavaPlugin {

        
    @Override
    public void onEnable() {
        // Plugin startup logic
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
        ConfigurationSerialization.registerClass(ShopItemPosition.class);
        ConfigurationSerialization.registerClass(RoleInfo.class);
        ConfigurationSerialization.registerClass(RuneInfo.class);
        ConfigurationSerialization.registerClass(RoleItemDispenser.EnchantInfo.class);
    }

    private void RegisterRoles() {
        Logger.log("Registering roles...");
        Role.registerRole(SybilAttacker.class);
        Role.registerRole(Cryptan.class);
        Role.registerRole(Cryptomarine.class);
        Role.registerRole(RuslanEth.class);
        Role.registerRole(Indian.class);
        Role.registerRole(Monkey.class);
        Role.registerRole(Guts.class);
    }

    private void InitHandlers() {
        Logger.log("Registering event handlers...");
        getServer().getPluginManager().registerEvents(new PlayerLoginHandler(), this);
        getServer().getPluginManager().registerEvents(new JoinPlayerHandler(), this);
        getServer().getPluginManager().registerEvents(new StageHandler(), this);
        getServer().getPluginManager().registerEvents(new OnChatEvent(), this);
        getServer().getPluginManager().registerEvents(new LobbyDisposer(), this);
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
    }
    
    public static Nftilation getInstance() {
        return getPlugin(Nftilation.class);
    }

    @Override
    public void onDisable() {
        Logger.log("Disabling...");
        if(GameInstance.getInstance().getCounter() != null) {
            GameInstance.getInstance().getCounter().bar.setVisible(false);
        }
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer("Сервер в данный момент перезагружается, перезайдите позже...");
        }
        EntityList.clearEntities();
        CustomEntities.unregisterEntities();
    }
    
    
    public void ReloadProperties() {
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
