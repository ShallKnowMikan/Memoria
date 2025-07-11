package dev.mikan.events.cores;

import dev.mikan.altairkit.AltairKit;
import dev.mikan.database.module.impl.CoreDatabase;
import dev.mikan.modules.core.Core;
import dev.mikan.modules.core.CoreModule;
import dev.mikan.modules.core.Level;
import dev.mikan.modules.faction.MFaction;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class CoreCreateEvent extends Event implements Cancellable {

    /*
    * Called from CoreListeners#onCorePlace
    *
    * it is called when a player places a core block inside
    * their claim, and they do not have one yet
    * */

    private @Getter final Player player;
    private @Getter final Level level;
    private @Getter final Location loc;
    private @Getter final MFaction faction;
    private Core core;
    private final CoreDatabase database;

    public CoreCreateEvent(Player player, Level level, Location loc, MFaction faction) {
        this.player = player;
        this.faction = faction;
        this.loc = loc;
        this.level = level;
        this.database = CoreModule.instance().getDatabase();
    }


    public void run(){
        if (cancelled) return;

        database.insert(level,loc,faction)
                .whenComplete( (id,error) -> {
            final String message = id != -1 ? "added successfully." : "error while adding.";
            CoreModule.instance().info("Core: {} -> {}",id,message);

            if (id == -1) return;

            Core.Cores.instance(id, faction.getId(), loc,level);
            player.sendMessage(AltairKit.colorize(CoreModule.instance().getPlugin().getLang().getString("cores.on_place.success")));
        });

    }








    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
