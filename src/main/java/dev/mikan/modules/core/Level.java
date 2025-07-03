package dev.mikan.modules.core;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record Level(int id, boolean isDefault, String name, int next, int health, int rewardPoints, int upgradeCost,
                    int regenAmountPerSecond, int regenStartDelayMinutes) {


    @UtilityClass
    public static final class Levels {

        public static Level defaultLevel;
        public final static Map<Integer, Level> cache = new ConcurrentHashMap<>();

        /*
         * Loads the default
         * */
        public void load(){
            ConfigurationSection section = CoreModule.instance().getConfig().getConfigurationSection("level");
            for (String key : section.getKeys(false)) {
                if (!section.getBoolean(key + ".default")) continue;

                int id = Integer.parseInt(key);
                String name = section.getString(key + ".name");
                int next = section.getInt(key + ".next");
                int health = section.getInt(key + ".health");
                int rewardPoints = section.getInt(key + ".reward_points");
                int upgradeCost = section.getInt(key + ".upgrade_cost");
                int regenAmountPerSecond = section.getInt(key + ".regen.per_second");
                int regenStartDelayMinutes = section.getInt(key + ".regen.start_delay_minutes");


                defaultLevel = new Level(
                        id,
                        true,
                        name,
                        next,
                        health,
                        rewardPoints,
                        upgradeCost,
                        regenAmountPerSecond,
                        regenStartDelayMinutes
                );
                break;
            }
            cache.put(defaultLevel.id,defaultLevel);
            Level current = defaultLevel;
            for (int i = 0; i < section.getKeys(false).size() - 1; i++) {
                int id = current.next;
                String name = section.getString(current.next + ".name");
                int next = section.getInt(current.next + ".next");
                int health = section.getInt(current.next + ".health");
                int rewardPoints = section.getInt(current.next + ".reward_points");
                int upgradeCost = section.getInt(current.next + ".upgrade_cost");
                int regenAmountPerSecond = section.getInt(current.next + ".regen.per_second");
                int regenStartDelayMinutes = section.getInt(current.next + ".regen.start_delay_minutes");

                cache.put(id,new Level(
                        id,
                        false,
                        name,
                        next,
                        health,
                        rewardPoints,
                        upgradeCost,
                        regenAmountPerSecond,
                        regenStartDelayMinutes
                ));
            }

        }
    }


}
