package dev.mikan;


import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.mikan.altairkit.api.yml.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Memoria {

    private @Getter final Bootstrap bootstrap;
    private @Getter final Logger logger;
    private @Getter final ConfigManager configManager;
    private @Getter final FileConfiguration lang;
    private @Getter final FileConfiguration general;

    public Memoria(Bootstrap bootstrap) {

        this.bootstrap = bootstrap;
        this.logger = LoggerFactory.getLogger(bootstrap.getClass());
        this.configManager = new ConfigManager(bootstrap);

        this.general = configManager.get("general.yml");
        this.lang = configManager.get("lang.yml");

    }

    // Loads general files only
    @SneakyThrows private void loadFiles(){
        for (String fileName : List.of("general.yml", "lang.yml")) {
            this.configManager.load(fileName,bootstrap.getResource(fileName));
        }

    }

}
