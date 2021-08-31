package net.nuggetmc.tplus.bot.agent.botagent;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class Goal { //TODO - Horse, rename this to LegacyGoal if you need to
    private LivingEntity livingEntity;
    private Location locationGoal;

    public Goal(LivingEntity target){
        this.livingEntity = target;
        this.locationGoal = null;
    }
    public Goal(Location location){
        this.locationGoal = location;
        this.livingEntity = null;
    }
    public Location getLocation(){
        if (isLocationGoal()) {
            return locationGoal;
        }
        return livingEntity.getLocation();
    }
    public boolean isLocationGoal(){
        return locationGoal != null;
    }
    public boolean isEntityGoal(){
        return livingEntity != null && livingEntity.isValid();
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public Location getLocationGoal() {
        return locationGoal;
    }

    public void setLivingEntity(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    public void setLocationGoal(Location locationGoal) {
        this.locationGoal = locationGoal;
    }
}
