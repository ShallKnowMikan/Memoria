package dev.mikan;

import com.github.retrooper.packetevents.PacketEvents;
import dev.mikan.altairkit.utils.Module;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class Bootstrap extends JavaPlugin {

    private Memoria memoria;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        //On Bukkit, calling this here is essential, hence the name "load"
        PacketEvents.getAPI().load();
    }


    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        memoria = new Memoria(this);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        if (this.memoria != null)
            memoria.getModules().values().forEach(Module::onDisable);
    }
}
