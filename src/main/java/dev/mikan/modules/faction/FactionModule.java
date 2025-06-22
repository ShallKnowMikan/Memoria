package dev.mikan.modules.faction;

import com.massivecraft.factions.Factions;
import dev.mikan.Memoria;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.Module;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.commands.FactionCommands;
import dev.mikan.commands.MemoriaCommands;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.impl.FactionsDB;
import dev.mikan.listeners.FactionsListeners;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
* Module for factions features here you can
* manage all settings which are factions related
*
* */
public final class FactionModule extends Module implements Singleton {

    private @Getter final Memoria plugin;
    private @Getter final FactionsDB database;
    private @Getter final Map<Player, RecognitionCache> recognitionCache = new ConcurrentHashMap<>();

    private @Getter FileConfiguration config;

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
        info("Reloaded.");
    }

    @Override
    public void onDisable() {

    }

    @Override @SneakyThrows
    public void loadConfig() {
        database.setup();
        database.loadFactions();

        plugin.getConfigManager().load("modules/factions.yml",plugin.getBootstrap().getResource("modules/factions.yml"));
        config = plugin.getConfigManager().get("modules/factions.yml");
    }

    @Override
    public void registerCommands(Plugin plugin) {
        AltairKit.registerCommands(new FactionCommands());
        AltairKit.registerCommands(new MemoriaCommands(this.plugin));
        AltairKit.tabComplete("memoria reload",this.plugin.getModules().keySet().toArray(new String[0]));
        AltairKit.tabComplete("memoria reset", Factions.getInstance().getFactionTags().toArray(new String[0]));
    }

    @Override
    public void registerListeners(Plugin plugin) {
        listen(new FactionsListeners(database));
    }

    public static FactionModule instance(){
        return Singleton.getInstance(FactionModule.class,() -> null);
    }
}
