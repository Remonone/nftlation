package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.meta.RoleInfo;
import remonone.nftilation.game.models.*;
import remonone.nftilation.utils.*;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

@Getter
public abstract class Role implements Cloneable, Listener, IDamageContainer, IInventoryHelder {

    private final Material material;
    private final String name;
    private final List<String> description;
    private final int index;
    protected final Map<String, Object> meta;
    
    private static AbilityItemsContainer HANDLER = new AbilityItemsContainer();
    
    @Getter
    private final static List<Role> roles = new ArrayList<>();

    private AbilityItemsHandler handler;

    public boolean checkForRoleAccess(Role role) {
        return role.equals(this);
    }

    public abstract String getRoleID();

    @SuppressWarnings("unchecked")
    protected void setPlayer(Player player, Map<String, Object> params) {
        if(!params.containsKey(PropertyConstant.PLAYER_LEVEL_PARAM)) {
            Logger.error("Cannot set player: " + player.getDisplayName() + " with role: " + name);
            return;
        }
        player.setHealthScaled(true);
        int level = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        Object attributeRaw = getMetaInfo(MetaConstants.META_STATS_ATTRIBUTES, level);
        if(attributeRaw != null) {
            List<AttributeModifier> modifiers = (List<AttributeModifier>) attributeRaw;
            try {
                modifiers.forEach(modifier -> {
                    Attribute attribute = Attribute.valueOf(modifier.getAttributeName());
                    Object levelBasedValue = NestedObjectFetcher.getLevelBasedObject(level, modifier.getAttributeValue());
                    player.getAttribute(attribute).setBaseValue((double) levelBasedValue);
                    if(attribute.equals(Attribute.GENERIC_MAX_HEALTH)) {
                        player.setHealthScale((double) levelBasedValue);
                    }
                });
            } catch (Exception e) {
                Logger.error("Configuration value is corrupted: " + e.getMessage());
            }
        }
        Object potionRaw = getMetaInfo(MetaConstants.META_STATS_EFFECTS, level);
        if(potionRaw != null) {
            List<EffectPotion> effects = (List<EffectPotion>) potionRaw;
            effects.forEach(potion -> {
                PotionEffectType type = PotionEffectType.getByName(potion.getEffect());
                if(type == null) {
                    Logger.warn("Cannot give effect " + potion.getEffect() + " for role " + name + ". Skipping...");
                    return;
                }
                Object levelBasedValue = NestedObjectFetcher.getLevelBasedObject(level, potion.getStrength());
                player.addPotionEffect(new PotionEffect(type, DataConstants.CONSTANT_POTION_DURATION, (int)levelBasedValue, false, false));
            });
        }
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
    }
    
    protected void killPlayer(Player player) {}
    
    protected Role(String id) {
        RoleInfo info = MetaConfig.getInstance().getRoles().stream().filter(roleInfo -> roleInfo.getRoleId().equals(id)).findFirst().orElse(null);
        if(info == null) {
            Logger.error("Cannot find info for role: " + this.getClass().getSimpleName());
            this.material = Material.AIR;
            this.name = "";
            this.description = new ArrayList<>();
            this.index = 0;
            this.meta = new HashMap<>();
            return;
        }
        this.material = Material.getMaterial(info.getMaterialName());
        this.name = info.getRoleName();
        this.description = info.getDescription();
        this.index = info.getRoleIndex();
        this.meta = info.getMetaInfo();
    }
    
    public static void updatePlayerAbilities(PlayerModel model) {
        ResetUtils.globalResetPlayerStats(model.getReference());
        Map<String, Object> params = model.getParameters();
        if(!PlayerUtils.validateParams(params)) {
            Logger.error("Cannot update player abilities for: " + model.getReference().getDisplayName());
            return;
        }
        Role role = getRoleByID(params.get(PropertyConstant.PLAYER_ROLE_ID).toString());
        new BukkitRunnable() {
            @Override
            public void run() {
                role.setPlayer(model.getReference(), params);
            }
        }.runTaskLater(Nftilation.getInstance(), 1);
    }

    public static void updatePlayerAbilities(Player player) {
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        updatePlayerAbilities(model);
    }
    
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        return Collections.emptyList();
    }
    
    public static void registerRole(Class<? extends Role> role) {
        try {
            Role roleToAdd = role.getDeclaredConstructor().newInstance();
            if(roles.contains(roleToAdd)) return;
            getServer().getPluginManager().registerEvents(roleToAdd, Nftilation.getInstance());
            roles.add(roleToAdd);
        } catch(Exception e) {
            Logger.error("Error during registering the role: " + e.getMessage());
        }
    }
    
    public static void refillInventoryWithItems(PlayerModel player) {
        player.getReference().getInventory().clear();
        setInventoryItems(player);
    }

    public static void setInventoryItems(PlayerModel model) {
        Player player = model.getReference();
        Inventory inventory = player.getInventory();
        clearPlayerItems(player);
        Map<String, Object> params = model.getParameters();
        if(!PlayerUtils.validateParams(params)) {
            Logger.error("Cannot set inventory items for player: " + model.getReference().getDisplayName());
        }
        Role role = getRoleByID(params.getOrDefault(PropertyConstant.PLAYER_ROLE_ID, "_").toString());
        
        ItemStack[] itemStacks = Arrays.asList(
                RoleItemDispenser.getItem(RoleItemDispenser.ItemType.SWORD, params, role.meta),
                RoleItemDispenser.getItem(RoleItemDispenser.ItemType.PICKAXE, params, role.meta),
                RoleItemDispenser.getItem(RoleItemDispenser.ItemType.AXE, params, role.meta),
                RoleItemDispenser.getItem(RoleItemDispenser.ItemType.SHOVEL, params, role.meta)).toArray(new ItemStack[0]);
        setOwner(player, itemStacks);
        for(ItemStack stack : itemStacks) {
            ItemStatModifierComponent.markItemAsUndroppable(stack);
            ItemStatModifierComponent.markItemAsUnstorable(stack);
        }
        inventory.addItem(itemStacks);
        fillEquipment(player, params);
        role.giveAbilityItems(player, params);
    }
    
    private void giveAbilityItems(Player player, Map<String, Object> params) {
        List<ItemStack> abilities = getAbilityItems(params);
        abilities.addAll(RoleItemDispenser.getCustomAbilityItems(player));
        ItemStack[] abilityItems = abilities.toArray(new ItemStack[0]);
        setOwner(player, abilityItems);
        for(int i = 0; i < abilityItems.length; i++) {
            ItemStack stack = abilityItems[i];
            ItemStack existingItem = player.getInventory().getItem(8 - i);
            ItemStatModifierComponent.markItemAsUndroppable(stack);
            ItemStatModifierComponent.markItemAsUnstorable(stack);
            ItemStatModifierComponent.markItemAsUncraftable(stack);
            player.getInventory().setItem(8 - i, stack);
            if(existingItem != null) {
                player.getInventory().addItem(existingItem);
            }
        }
    }
    
    public static void onDie(Player player, Role role) {
        ResetUtils.globalResetPlayerStats(player);
        role.killPlayer(player);
    }

    private static void setOwner(Player owner, ItemStack... itemStacks) {
        for (ItemStack stack : itemStacks) {
            if(stack == null || stack.getAmount() < 1 || stack.getType() == Material.AIR) continue;
            NBT.modify(stack, nbt -> {
                nbt.setString("owner", owner.getUniqueId().toString());
            });
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static void clearPlayerItems(Player player) {
        Spliterator<ItemStack> itemStackSpliterator = player.getInventory().spliterator();
        while(itemStackSpliterator.tryAdvance(itemStack -> {
            if(itemStack == null) return;
            String owner = NBT.get(itemStack, nbt -> (String) nbt.getString("owner"));
            if(StringUtils.isEmpty(owner)) return;
            if(!owner.equals(player.getUniqueId().toString())) return;
            player.getInventory().remove(itemStack);
        }));
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if(offHand == null || offHand.getAmount() < 1 || offHand.getType().equals(Material.AIR)) return;
        String owner = NBT.get(offHand, nbt -> (String) nbt.getString("owner"));
        if(StringUtils.isEmpty(owner)) return;
        if(!owner.equals(player.getUniqueId().toString())) return;
        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
    }

    private static void fillEquipment(Player player, Map<String, Object> params) {
        EntityEquipment equipment = player.getEquipment();
        if(!PlayerUtils.validateParams(params)) {
            Logger.error("Cannot fill equipment for player: " + player.getDisplayName());
            return;
        }
        Role role = getRoleByID(params.get(PropertyConstant.PLAYER_ROLE_ID).toString());
        ItemStack helmet = RoleItemDispenser.getItem(RoleItemDispenser.ItemType.HELMET, params, role.meta);
        ItemStack chestplate = RoleItemDispenser.getItem(RoleItemDispenser.ItemType.CHESTPLATE, params, role.meta);
        ItemStack leggings = RoleItemDispenser.getItem(RoleItemDispenser.ItemType.LEGGINGS, params, role.meta);
        ItemStack boots = RoleItemDispenser.getItem(RoleItemDispenser.ItemType.BOOTS, params, role.meta);
        setOwner(player, helmet, chestplate, leggings, boots);
        markEquipment(helmet, chestplate, leggings, boots);
        ItemStack equippedHelmet = equipment.getHelmet();
        if(equippedHelmet != null && !ItemStatModifierComponent.checkItemIfDefault(equippedHelmet)) player.getInventory().addItem(equippedHelmet.clone());
        equipment.setHelmet(helmet);
        ItemStack equippedChestplate = equipment.getChestplate();
        if(equippedChestplate != null && !ItemStatModifierComponent.checkItemIfDefault(equippedChestplate)) player.getInventory().addItem(equippedChestplate.clone());
        equipment.setChestplate(chestplate);
        ItemStack equippedLeggings = equipment.getLeggings();
        if(equippedLeggings != null && !ItemStatModifierComponent.checkItemIfDefault(equippedLeggings)) player.getInventory().addItem(equippedLeggings.clone());
        equipment.setLeggings(leggings);
        ItemStack equippedBoots = equipment.getBoots();
        if(equippedBoots != null && !ItemStatModifierComponent.checkItemIfDefault(equippedBoots)) player.getInventory().addItem(equippedBoots.clone());
        equipment.setBoots(boots);
    }

    private static void markEquipment(ItemStack... items) {
        for(ItemStack itemStack : items) {
            ItemStatModifierComponent.markItemAsDefault(itemStack);
            ItemStatModifierComponent.markItemAsUnstorable(itemStack);
            ItemStatModifierComponent.markItemAsUndroppable(itemStack);
        }
    }

    public static Role getRoleByID(String roleID) {
        return roles.stream().filter(role -> role.getRoleID().equals(roleID)).findFirst().orElse(null);
    }
    
    @Override
    public List<IDamageHandler> getDamageHandlers() {
        return Collections.emptyList();
    }

    @Override
    public List<IDamageInvoker> getDamageInvokers() {
        return Collections.emptyList();
    }

    protected void registerHandlers(Map<String, IAbilityHandler> handlerMap, String container) {
        AbilityItemsHandler handlers = AbilityItemsHandler.builder()
                .handlerMap(handlerMap)
                .maintainRole(this)
                .container(container)
                .build();
        this.handler = handlers;
        HANDLER.registerAbilityHandler(this, handlers);
    }

    @Override
    public Role clone() {
        try {
            return (Role) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    
    public Object getMetaInfo(String path, int level) {
        return NestedObjectFetcher.getNestedObject(path, this.getMeta(), level);
    }

    public void resetListeners() {
        PlayerInteractEvent.getHandlerList().unregister(HANDLER);
        PlayerInteractEvent.getHandlerList().unregister(this);
        HANDLER = new AbilityItemsContainer();
    }

    public Object getMetaByName(PlayerModel model, String key) {
        int level = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        return getMetaInfo(key, level);
    }

    
}
