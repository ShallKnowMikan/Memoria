package dev.mikan;

import org.bukkit.plugin.java.JavaPlugin;

public class Bootstrap extends JavaPlugin {

    @Override
    public void onLoad() {
        super.onLoad();
    }


    @Override
    public void onEnable() {
        new Memoria(this);
    }

    @Override
    public void onDisable() {

    }
}
