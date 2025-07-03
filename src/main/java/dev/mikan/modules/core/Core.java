package dev.mikan.modules.core;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class Core {

    private final int id;
    private final int factionId;
    private final Location location;
    private final Level level;

    private final CoreModule module;

    public Core(int id, int factionId, Location location, Level level) {
        this.id = id;
        this.factionId = factionId;
        this.location = location;
        this.level = level;
        this.module = CoreModule.instance();
    }


    @UtilityClass
    public final static class Cores{

        private final static Map<Integer,Core> cache = new ConcurrentHashMap<>();

        public void instance(int id, int factionId, Location location, Level level){
            cache.put(id,new Core(
                    id,
                    factionId,
                    location,
                    level
            ));
        }

        public void destruct(int id){
            cache.remove(id);
        }

        public Core getById(int id){
            return cache.get(id);
        }

    }

}
