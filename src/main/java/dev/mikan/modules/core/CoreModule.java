package dev.mikan.modules.core;

import dev.mikan.Memoria;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.ItemBuilder;
import dev.mikan.altairkit.utils.Module;
import dev.mikan.altairkit.utils.NBTUtils;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.commands.CoreCommands;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.impl.CoreDatabase;
import dev.mikan.listeners.CoreListeners;
import lombok.Getter;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.slf4j.Logger;

import java.util.List;

public final class CoreModule extends Module {

    public static final String CORE_ITEM_KEY = "dev.mikan.core_item";
    public static final int CORE_ITEM_VALUE = 1;

    private @Getter final Memoria plugin;
    private @Getter FileConfiguration config;
    private @Getter final CoreDatabase database;
    private @Getter ItemStack coreBaseItem;
    private @Getter Economy economy;

    public CoreModule(Memoria plugin, String name, Logger logger) {
        super(plugin.getBootstrap(), name, logger);
        this.plugin = plugin;
        this.database = Singleton.getInstance(CoreDatabase.class,() -> new CoreDatabase(new SQLiteManager(logger,"factions.db"),logger));
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp != null) {
            this.economy = rsp.getProvider();
        }

    }

    @Override
    public void onEnable() {
        this.loadConfig();
        this.registerCommands(plugin.getBootstrap());
        this.registerListeners(plugin.getBootstrap());
    }

    @Override
    public void onReload() {
        /*
        * On reload since core instances are already inside the
        * cache it will just reload them, without creating new ones
        * the Cores.instance method will return the instance present
        * in cache
        * */
        this.loadConfig();
        Core.Cores.all().forEach(Core::reload);
        info("Reloaded.");
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
        this.loadBaseItem();

    }

    @Override
    public void registerCommands(Plugin plugin) {
        AltairKit.registerCommands(new CoreCommands(this));
        info("Commands registered.");
    }

    @Override
    public void registerListeners(Plugin plugin) {
        listen(new CoreListeners(this));
        info("Listeners registered.");
    }


    private void loadBaseItem(){
        String[] materialTokens = config.getString("item.type").split(":");
        int materialId = Integer.parseInt(materialTokens[0]);
        byte data = materialTokens.length > 1 ? Byte.parseByte(materialTokens[1]) : 0;
        Material material = Material.getMaterial(materialId);

        String itemName = AltairKit.colorize(config.getString("item.name"));
        List<String> lore = AltairKit.colorize(config.getStringList("item.lore"));

        this.coreBaseItem = NBTUtils.set(
                new ItemBuilder(new ItemStack(material,1,data))
                        .setName(itemName)
                        .setLore(lore)
                        .toItemStack(),
                CORE_ITEM_KEY,
                CORE_ITEM_VALUE
        );
    }


    public static CoreModule instance(){
        return Singleton.getInstance(CoreModule.class,() -> null);
    }

}

