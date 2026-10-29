package com.albertsurvival.data;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private double thirst;
    private double temperature;
    private long lastThirstDamage;
    private long lastColdDamage;
    private long lastHeatDamage;
    private long lastUpdate;
    private long lastActivity;
    private boolean dirty;

    public PlayerData(UUID uuid, double thirst, double temperature) {
        this.uuid = uuid;
        this.thirst = thirst;
        this.temperature = temperature;
        this.lastUpdate = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        this.dirty = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getThirst() {
        return thirst;
    }

    public void setThirst(double thirst) {
        this.thirst = thirst;
        this.dirty = true;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        this.dirty = true;
    }

    public long getLastThirstDamage() {
        return lastThirstDamage;
    }

    public void setLastThirstDamage(long lastThirstDamage) {
        this.lastThirstDamage = lastThirstDamage;
    }

    public long getLastColdDamage() {
        return lastColdDamage;
    }

    public void setLastColdDamage(long lastColdDamage) {
        this.lastColdDamage = lastColdDamage;
    }

    public long getLastHeatDamage() {
        return lastHeatDamage;
    }

    public void setLastHeatDamage(long lastHeatDamage) {
        this.lastHeatDamage = lastHeatDamage;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void markClean() {
        this.dirty = false;
    }
}
