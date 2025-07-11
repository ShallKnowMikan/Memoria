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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Memoria {

    private @Getter final Bootstrap bootstrap;
    private @Getter final Logger logger;
    private @Getter final ConfigManager configManager;
    private @Getter FileConfiguration lang;
    private @Getter FileConfiguration general;

    private @Getter final Map<Class<? extends Module>,Module> modules = new LinkedHashMap<>();
    private @Getter final Map<String,Class<? extends Module>> moduleNames = new LinkedHashMap<>();

    public Memoria(Bootstrap bootstrap) {

        this.bootstrap = bootstrap;
        this.logger = LoggerFactory.getLogger(bootstrap.getClass());
        this.configManager = new ConfigManager(bootstrap);

        AltairKit.enableGUIManager(this.bootstrap);

        modules.put(FactionModule.class,Singleton.getInstance(FactionModule.class,() -> new FactionModule(this,"Faction", logger)));
        moduleNames.put("faction", FactionModule.class);

        modules.put(CoreModule.class,Singleton.getInstance(CoreModule.class,() -> new CoreModule(this,"Core", logger)));
        moduleNames.put("core", CoreModule.class);

        modules.put(RegenblockModule.class,Singleton.getInstance(RegenblockModule.class,() -> new RegenblockModule(this,"RegenBlock",logger)));
        moduleNames.put("regenblock",RegenblockModule.class);

        loadFiles();

        loadModules();
    }

    // Loads general files only
    @SneakyThrows public void loadFiles(){
        for (String fileName : List.of("general.yml", "lang.yml")) {
            this.configManager.load(fileName,bootstrap);
        }
        this.general = configManager.get("general.yml");
        this.lang = configManager.get("lang.yml");
    }

    // Loads every module
    private void loadModules(){
        modules.values().forEach(Module::onEnable);
    }

}
