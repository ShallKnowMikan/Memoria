package dev.mikan;


import dev.mikan.altairkit.AltairKit;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Memoria {

    private @Getter final Bootstrap bootstrap;
    private @Getter final Logger logger;
    private @Getter final ConfigManager configManager;
    private @Getter FileConfiguration lang;
    private @Getter FileConfiguration general;

    private @Getter final Map<String,Module> modules = new ConcurrentHashMap<>();

    public Memoria(Bootstrap bootstrap) {

        this.bootstrap = bootstrap;
        this.logger = LoggerFactory.getLogger(bootstrap.getClass());
        this.configManager = new ConfigManager(bootstrap);

        AltairKit.enableGUIManager(this.bootstrap);

        modules.put("faction",Singleton.getInstance(FactionModule.class,() -> new FactionModule(this,"Faction", logger)));
        modules.put("core",Singleton.getInstance(CoreModule.class,() -> new CoreModule(this,"Core",logger)));
        modules.put("regenblock",Singleton.getInstance(RegenblockModule.class,() -> new RegenblockModule(this,"RegenBlock",logger)));

        loadFiles();

        loadModules();
    }

    // Loads general files only
    @SneakyThrows public void loadFiles(){
        for (String fileName : List.of("general.yml", "lang.yml")) {
            this.configManager.load(fileName,bootstrap.getResource(fileName));
        }
        this.general = configManager.get("general.yml");
        this.lang = configManager.get("lang.yml");
    }

    // Loads every module
    private void loadModules(){
        modules.values().forEach(Module::onEnable);
    }

}
