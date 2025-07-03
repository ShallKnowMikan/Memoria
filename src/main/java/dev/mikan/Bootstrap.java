package dev.mikan;

import dev.mikan.altairkit.utils.Module;
import org.bukkit.plugin.java.JavaPlugin;

public class Bootstrap extends JavaPlugin {

    private Memoria memoria;

    @Override
    public void onLoad() {
        super.onLoad();
    }


    @Override
    public void onEnable() {
        memoria = new Memoria(this);
    }

    @Override
    public void onDisable() {
        if (this.memoria != null)
            memoria.getModules().values().forEach(Module::onDisable);
    }
}
