package dev.mikan.modules.faction;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashSet;
import java.util.Set;


public class FactionTop extends BukkitRunnable {

    // TODO: do not use runnable, make cores module and update with victory and defeats instead.
    private final Set<MFaction> orderedFactions = new LinkedHashSet<>();

    @Override
    public void run() {

    }
}
