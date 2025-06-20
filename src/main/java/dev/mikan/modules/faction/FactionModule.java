package dev.mikan.modules.faction;

import dev.mikan.Memoria;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.Module;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.commands.FactionCommands;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.impl.FactionsDB;
import dev.mikan.listeners.FactionsListeners;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

/*
* Module for factions features here you can
* manage all settings which are factions related
*
* */
public final class FactionModule extends Module implements Singleton {

    private final Memoria plugin;
    private final FactionsDB database;

    public FactionModule(Memoria plugin, String name, Logger logger) {
        super(plugin.getBootstrap(), name, logger);
        this.plugin = plugin;
        database = Singleton.getInstance(FactionsDB.class,() -> new FactionsDB(new SQLiteManager(logger,"factions.db"),logger));
    }

    @Override
    public void onEnable() {
        loadConfig();
        registerListeners(plugin.getBootstrap());
        registerCommands(plugin.getBootstrap());
    }

    @Override
    public void onReload() {
        loadConfig();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void loadConfig() {
        database.setup();
        database.loadFactions();
    }

    @Override
    public void registerCommands(Plugin plugin) {
        AltairKit.registerCommands(new FactionCommands());
    }

    @Override
    public void registerListeners(Plugin plugin) {
        listen(new FactionsListeners(database));
    }

    public static FactionModule instance(){
        return Singleton.getInstance(FactionModule.class,() -> null);
    }
}
