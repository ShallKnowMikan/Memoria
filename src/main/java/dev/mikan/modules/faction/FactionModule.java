package dev.mikan.modules.faction;

import dev.mikan.Memoria;
import dev.mikan.altairkit.utils.Module;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.database.module.impl.FactionsDB;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

public final class FactionModule extends Module implements Singleton {

    private final Memoria plugin;
    private final FactionsDB database = Singleton.getInstance(FactionsDB.class);

    public FactionModule(Memoria plugin, String name, Logger logger) {
        super(plugin.getBootstrap(), name, logger);
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
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
