package dev.mikan.modules.core;

import dev.mikan.Memoria;
import dev.mikan.altairkit.utils.Module;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.impl.CoreDatabase;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

public final class CoreModule extends Module {

    private final Memoria plugin;
    private @Getter FileConfiguration config;
    private final CoreDatabase database;

    public CoreModule(Memoria plugin, String name, Logger logger) {
        super(plugin.getBootstrap(), name, logger);
        this.plugin = plugin;
        this.database = Singleton.getInstance(CoreDatabase.class,() -> new CoreDatabase(new SQLiteManager(logger,"factions.db"),logger));
    }

    @Override
    public void onEnable() {
        this.loadConfig();
    }

    @Override
    public void onReload() {

    }

    @Override
    public void onDisable() {

    }

    @Override @SneakyThrows
    public void loadConfig() {

        plugin.getConfigManager().load("modules/cores.yml",plugin.getBootstrap());
        config = plugin.getConfigManager().get("modules/cores.yml");

        Level.Levels.load();

        this.database.setup();
        this.database.loadCores();



    }

    @Override
    public void registerCommands(Plugin plugin) {

    }

    @Override
    public void registerListeners(Plugin plugin) {

    }

    public static CoreModule instance(){
        return Singleton.getInstance(CoreModule.class,() -> null);
    }

}

