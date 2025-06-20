package dev.mikan.modules.regenblock;

import dev.mikan.Memoria;
import dev.mikan.altairkit.utils.Module;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

public class RegenblockModule extends Module {
    private final Memoria plugin;
    public RegenblockModule(Memoria plugin, String name, Logger logger) {
        super(plugin.getBootstrap(), name, logger);
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onReload() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void loadConfig() {

    }

    @Override
    public void registerCommands(Plugin plugin) {

    }

    @Override
    public void registerListeners(Plugin plugin) {

    }
}
