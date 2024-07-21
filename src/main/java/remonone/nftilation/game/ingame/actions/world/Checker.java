package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.EntityList;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class Checker implements IAction, Listener {
    
    private final static Material REQUIRED_TYPE = Material.RED_ROSE;
    private final static int REQUIRED_AMOUNT = 5;
    private Map<String, Boolean> teamsCompletion;
    
    @Override
    public void Init(Map<String, Object> params) {
        teamsCompletion = new HashMap<>();
        List<TeamData> teams = Store.getInstance().getDataInstance().getTeamData();
        for(TeamData data : teams) {
            teamsCompletion.put(data.getTeamName(), false);
            Location loc = GameInstance.getInstance().getTeamSpawnPoint(data.getTeamName()).getCheckerChestPosition();
            if(loc != null) {
                Block block = loc.getBlock();
                block.setType(Material.CHEST);
                Chest chest = (Chest) block.getState();
                chest.setCustomName(ChatColor.AQUA + NameConstants.CHECKER_CHEST);
                chest.update();
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                FailTask();
            }
        }.runTaskLater(Nftilation.getInstance(), 3 * DataConstants.TICKS_IN_MINUTE);
        getServer().getPluginManager().registerEvents(this, Nftilation.getInstance());
    }

    @Override
    public String getTitle() {
        return "Checker";
    }

    @Override
    public String getDescription() {
        return "5 ромашек на базе соберешь, аирдроп заберешь";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_FIREWORK_BLAST;
    }
    
    @EventHandler
    public void onChestInventoryUpdate(InventoryClickEvent e) {
        if(e.getView().getTopInventory().getName().equals(ChatColor.AQUA + NameConstants.CHECKER_CHEST)) {
            Inventory chestInventory = e.getView().getTopInventory();
            new BukkitRunnable() {
                @SuppressWarnings("StatementWithEmptyBody")
                @Override
                public void run() {
                    List<ItemStack> requiredType = new ArrayList<>();
                    Spliterator<ItemStack> itemStackSpliterator = chestInventory.spliterator();
                    while(itemStackSpliterator.tryAdvance(itemStack -> {
                        if(itemStack == null) return;
                        if(itemStack.getType().equals(REQUIRED_TYPE)) {
                            requiredType.add(itemStack);
                        }
                    }));
                    int amount = requiredType.stream().mapToInt(ItemStack::getAmount).sum();
                    if(amount >= REQUIRED_AMOUNT) {
                        chestInventory.removeItem(requiredType.toArray(new ItemStack[0]));
                        Player player = (Player) e.getWhoClicked();
                        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());

                        teamsCompletion.put(team, true);
                        AwardTeam(team);
                    }
                }
            }.runTaskLater(Nftilation.getInstance(), 2);
        }
    }

    private void AwardTeam(String team) {
        TeamSpawnPoint point = GameInstance.getInstance().getTeamSpawnPoint(team);
        Block block = point.getCheckerChestPosition().getBlock();
        block.setType(Material.AIR);
        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();
        chest.setCustomName(NameConstants.CHECKER_CHEST);
        chest.getBlockInventory().setItem(13, new ItemStack(Material.DIAMOND));
        Firework fw = (Firework) point.getCheckerChestPosition().getWorld().spawnEntity(point.getCheckerChestPosition(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    private void FailTask() {
        for(Map.Entry<String, Boolean> entry : teamsCompletion.entrySet()) {
            if(entry.getValue()) continue;
            String team = entry.getKey();
            Location loc = GameInstance.getInstance().getTeamSpawnPoint(team).getCheckerChestPosition();
            GameInstance.getInstance().getTeamPlayers(team).forEach(playerModel -> {
                Player player = playerModel.getReference();
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_HURT, 1f, .3f);
                player.sendTitle("Вы были прокляты Браяном!", "", 10, 60, 10);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * DataConstants.TICKS_IN_MINUTE, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 5 * DataConstants.TICKS_IN_MINUTE, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * DataConstants.TICKS_IN_MINUTE, 1));
            });
            loc.getBlock().setType(Material.AIR);
            loc.getChunk().load();
            WitherSkeleton skeleton = loc.getWorld().spawn(loc, WitherSkeleton.class);
            EntityList.addEntity(skeleton);
            skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100F);
            skeleton.setHealth(100F);
            skeleton.setCustomName(NameConstants.CHECKER_NAME);
            skeleton.setRemoveWhenFarAway(false);
            EntityHandleComponent.setEntityBounty(skeleton, 100);
            EntityHandleComponent.setEntityUnloadLocked(skeleton);
            EntityHandleComponent.setEntityHostile(skeleton);
            GiveArmorToBoss(skeleton);
        }
    }

    private void GiveArmorToBoss(LivingEntity boss) {
        EntityEquipment equipment = boss.getEquipment();
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        SetItemMeta(helmet, "brian_helmet");
        equipment.setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        SetItemMeta(chestplate, "brian_chestplate");
        equipment.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        SetItemMeta(leggings, "brian_leggings");
        equipment.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.LEATHER_HELMET);
        SetItemMeta(boots, "brian_boots");
        equipment.setBoots(boots);
        
        ItemStack shears = new ItemStack(Material.SHEARS);
        SetItemMeta(shears, "Bryan's Razor");
        equipment.setItemInMainHand(shears);
    }

    private void SetItemMeta(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
    }
}
