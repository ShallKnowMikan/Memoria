package dev.mikan.modules.core;

import com.massivecraft.factions.Factions;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.modules.faction.MFaction;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class Core {

    private final int id;
    private final int factionId;
    private final MFaction faction;
    private final Location location;
    private @Setter Level level;
    private final CoreConstructor constructor;
    private final CoreDestructor destructor;

    private Set<Spiral> spirals;
    private List<String> hologramLines;
    private @Setter int health;

    private final CoreModule module;
    private Hologram hologram;

    private Core(int id,int factionId, Location location, Level level) {
        this.id = id;
        this.factionId = factionId;
        this.faction = MFaction.MFactions.getById(factionId);
        this.location = location;
        this.level = level;
        this.module = CoreModule.instance();
        this.constructor = new CoreConstructor(location.getBlock());
        this.destructor = new CoreDestructor(this);
        this.health = level.health();
        spirals = Set.of(
                new Spiral(location.clone().add(0.5,0,0.5), 0,6.5,4.5),
                new Spiral(location.clone().add(0.5,0,0.5), 90,6.5,4.5),
                new Spiral(location.clone().add(0.5,0,0.5), 180,6.5,4.5),
                new Spiral(location.clone().add(0.5,0,0.5), 270,6.5,4.5),

                new Spiral(location.clone().add(0.5,0,0.5), 0,3,1.5),
                new Spiral(location.clone().add(0.5,0,0.5), 90,3,1.5),
                new Spiral(location.clone().add(0.5,0,0.5), 180,3,1.5),
                new Spiral(location.clone().add(0.5,0,0.5), 270,3,1.5)
        );

        this.updateHologramLines();

        this.create();

    }


    public void create(){
        this.constructor.whenFinished(this::spawnHologram);
        this.constructor.start();
        this.spirals.forEach(Spiral::start);
    }

    public void destruct(boolean animation){
        this.spirals.forEach(Spiral::stop);
        this.destructor.setAnimated(animation);
        this.destructor.whenFinished(() -> {
            this.hologram.delete();
            this.hologram.destroy();
            module.info("Core: {} successfully destructed.",this.id);
        });
        this.destructor.start();
        this.module.getDatabase().delete(this);
    }

    public void updateHologramLines(){
        List<String> lines = module.getConfig().getStringList("hologram");
        this.hologramLines = new ArrayList<>();

        for (String line : lines) {
            hologramLines.add(AltairKit.colorize(line
                            .replace("%faction%", Factions.getInstance().getFactionById(String.valueOf(factionId)).getTag()))
                    .replace("%health%",String.valueOf(health))
                    .replace("%level%",level.name())
                    .replace("%max_health%",String.valueOf(level.health()))

            );
        }
    }

    public void reload(){
        this.spirals.forEach(Spiral::stop);
        this.spirals = Set.of(
                new Spiral(location.clone().add(0.5,0,0.5), 0,6.5,4.5),
                new Spiral(location.clone().add(0.5,0,0.5), 90,6.5,4.5),
                new Spiral(location.clone().add(0.5,0,0.5), 180,6.5,4.5),
                new Spiral(location.clone().add(0.5,0,0.5), 270,6.5,4.5),

                new Spiral(location.clone().add(0.5,0,0.5), 0,3,1.5),
                new Spiral(location.clone().add(0.5,0,0.5), 90,3,1.5),
                new Spiral(location.clone().add(0.5,0,0.5), 180,3,1.5),
                new Spiral(location.clone().add(0.5,0,0.5), 270,3,1.5)
        );
        this.spirals.forEach(Spiral::start);

        this.updateHologramLines();
        this.hologram.delete();
        this.hologram = null;
        this.spawnHologram();

    }

    // Must call this#loadHologramLines before
    public void spawnHologram(){

        this.hologram = DHAPI.createHologram(String.valueOf(this.id),this.location.clone().add(0.5,7 + (hologramLines.size() * 0.3),0.5));

        DHAPI.setHologramLines(hologram,hologramLines);
    }

    @UtilityClass
    public final static class Cores{

        private final static Map<Integer,Core> cache = new ConcurrentHashMap<>();

        public Collection<Core> all(){
            return cache.values();
        }

        public void instance(int id, int factionId, Location location, Level level){
            if (cache.containsKey(id)) {
                return;
            }

            MFaction faction = MFaction.MFactions.getById(factionId);
            Bukkit.getScheduler().runTask(CoreModule.instance().getPlugin().getBootstrap(), () -> {
                Core core = new Core(
                        id,
                        factionId,
                        location,
                        level
                );
                faction.setCore(core);
                cache.put(id,core);
            });

        }

        public void destruct(int id){
            Core core = cache.remove(id);
            core.getFaction().setCore(null);
        }

        public Core getById(int id){
            return cache.get(id);
        }

    }

}
