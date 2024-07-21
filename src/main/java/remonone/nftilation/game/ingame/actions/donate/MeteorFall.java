package remonone.nftilation.game.ingame.actions.donate;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LargeFireball;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.Logger;

import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class MeteorFall implements IAction, Listener {
    
    public boolean isListening;
    
    @Override
    public void Init(Map<String, Object> params) {
        if(!params.containsKey(PropertyConstant.ACTION_REGION_X) || !params.containsKey(PropertyConstant.ACTION_REGION_Z)) {
            throw new NullPointerException("Couldn't initiate MeteorFall action. Coordinates are missing!");
        }
        double x = (Double) params.get(PropertyConstant.ACTION_REGION_X);
        double z = (Double) params.get(PropertyConstant.ACTION_REGION_Z);
        double y = 91;
        Vector position = new Vector(x,y,z);
        StartFall(position);
        if(!isListening) {
            getServer().getPluginManager().registerEvents(this, Nftilation.getInstance());
        }
    }

    private void StartFall(Vector position) {
        Vector spawnPosition = position.clone().add(new Vector(30, 50, 30));
        Logger.broadcast(spawnPosition.toString());
        Vector direction = position.clone().subtract(spawnPosition);
        Logger.broadcast(direction + " " + position);
        World world = Store.getInstance().getDataInstance().getMainWorld();
        Location spawnLocation = new Location(world, spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());
        LargeFireball fireball = world.spawn(spawnLocation, LargeFireball.class);
        fireball.setDirection(position.subtract(spawnPosition));
        
        fireball.setMetadata("meteor", new FixedMetadataValue(Nftilation.getInstance(), 25F));
    }

    @Override
    public String getTitle() {
        return "Падение метеорита";
    }

    @Override
    public String getDescription() {
        return "От такого умерли динозавры";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_GUARDIAN_ATTACK;
    }
}
