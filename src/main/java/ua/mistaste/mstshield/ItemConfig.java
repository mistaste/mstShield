package ua.mistaste.mstshield;

public class ItemConfig {
    double cooldownInSeconds;

    ItemConfig(double cooldownInSeconds) {
        this.cooldownInSeconds = cooldownInSeconds;
    }

    public int getCooldownInTicks() {
        return (int) Math.round(cooldownInSeconds * 20);
    }
}