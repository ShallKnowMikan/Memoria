package dev.mikan;


import dev.mikan.altairkit.api.yml.ConfigManager;
import dev.mikan.altairkit.utils.Module;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.modules.core.CoreModule;
import dev.mikan.modules.faction.FactionModule;
import dev.mikan.modules.regenblock.RegenblockModule;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class Memoria {

    private @Getter final Bootstrap bootstrap;
    private @Getter final Logger logger;
    private @Getter final ConfigManager configManager;
    private @Getter final FileConfiguration lang;
    private @Getter final FileConfiguration general;

    private @Getter final Set<? extends Module> modules;

    public Memoria(Bootstrap bootstrap) {

        this.bootstrap = bootstrap;
        this.logger = LoggerFactory.getLogger(bootstrap.getClass());
        this.configManager = new ConfigManager(bootstrap);

        this.general = configManager.get("general.yml");
        this.lang = configManager.get("lang.yml");

        modules = Set.of(
                Singleton.getInstance(FactionModule.class,() -> new FactionModule(this,"Faction", logger)),
                Singleton.getInstance(CoreModule.class,() -> new CoreModule(this,"Core",logger)),
                Singleton.getInstance(RegenblockModule.class,() -> new RegenblockModule(this,"RegenBlock",logger))
                );


        loadFiles();

        loadModules();
    }

    // Loads general files only
    @SneakyThrows private void loadFiles(){
        for (String fileName : List.of("general.yml", "lang.yml")) {
            this.configManager.load(fileName,bootstrap.getResource(fileName));
        }

    }

    // Loads every module
    private void loadModules(){
        for (Module module : modules) {
            module.onEnable();
        }
    }

}
