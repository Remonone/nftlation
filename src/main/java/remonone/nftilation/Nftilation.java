package remonone.nftilation;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import remonone.nftilation.commands.*;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.game.ingame.actions.ActionType;
import remonone.nftilation.game.ingame.actions.world.Checker;
import remonone.nftilation.game.ingame.actions.world.CryptDrop;
import remonone.nftilation.game.ingame.actions.world.Hamster;
import remonone.nftilation.game.ingame.actions.world.RoboSybyl;
import remonone.nftilation.game.lobby.LobbyDisposer;
import remonone.nftilation.game.roles.*;
import remonone.nftilation.game.shop.ShopBuilder;
import remonone.nftilation.game.shop.content.CategoryElement;
import remonone.nftilation.game.shop.content.ItemElement;
import remonone.nftilation.game.shop.content.ServiceElement;
import remonone.nftilation.game.shop.content.ShopItemPosition;
import remonone.nftilation.handlers.*;
import remonone.nftilation.utils.CustomEntities;
import remonone.nftilation.utils.EntityList;
import remonone.nftilation.utils.Logger;


public final class Nftilation extends JavaPlugin {
        
    @Override
    public void onEnable() {
        // Plugin startup logic
        Logger.log("Starting...");
        SerializeProperties();
        ShopBuilder.getInstance().Load();
        ConfigManager.getInstance().Load();
        InitHandlers();
        InitCommands();
        RegisterRoles();
        InitActions();
        CustomEntities.registerEntities();
    }

    private void InitActions() {
        Logger.log("Initializing actions...");
        ActionContainer.registerAction(ActionType.CRYPT_DROP, new CryptDrop());
        ActionContainer.registerAction(ActionType.HAMSTER, new Hamster());
        ActionContainer.registerAction(ActionType.ROBOSYBYL_ATTACK, new RoboSybyl());
        ActionContainer.registerAction(ActionType.CHECKER, new Checker());
    }

    private void SerializeProperties() {
        Logger.log("Serializing properties...");
        ConfigurationSerialization.registerClass(TeamSpawnPoint.class);
        ConfigurationSerialization.registerClass(CategoryElement.class);
        ConfigurationSerialization.registerClass(ItemElement.class);
        ConfigurationSerialization.registerClass(ServiceElement.class);
        ConfigurationSerialization.registerClass(ShopItemPosition.class);
    }

    private void RegisterRoles() {
        Logger.log("Registering roles...");
        Role.registerRole(SybylAttacker.class);
        Role.registerRole(Cryptan.class);
        Role.registerRole(Cryptomarine.class);
        Role.registerRole(RuslanEth.class);
        Role.registerRole(Indian.class);
        Role.registerRole(Monkey.class);
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
        getServer().getPluginManager().registerEvents(new OnRoleSelectHandler(), this);
        getServer().getPluginManager().registerEvents(new OnItemManipulateHandler(), this);
        getServer().getPluginManager().registerEvents(new ShopKeeperInteract(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerDieHandler(), this);
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
        this.getCommand("addTokens").setExecutor(new AddTokenCommand());
        this.getCommand("getLevel").setExecutor(new GetUpgradeLevelCommand());
        this.getCommand("skipPhase").setExecutor(new SkipPhaseCommand());
        this.getCommand("setCenterPosition").setExecutor(new SetCenterPositionCommand());
        this.getCommand("addDiamondPosition").setExecutor(new AddDiamondPlaceSpawnCommand());
        this.getCommand("addRoboSybylPos").setExecutor(new SetRoboSybylSpawnPointCommand());
        this.getCommand("addIronGolemPos").setExecutor(new AddIronGolemPositonCommand());
        this.getCommand("setCheckerTeamPosition").setExecutor(new CheckerChestCommand());
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
        EntityList.clearEntities();
    }
    
}
