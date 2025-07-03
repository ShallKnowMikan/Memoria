package dev.mikan.events;

import com.massivecraft.factions.Factions;
import dev.mikan.Memoria;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.database.module.impl.FactionDatabase;
import dev.mikan.modules.faction.FactionModule;
import dev.mikan.modules.faction.MFaction;
import dev.mikan.modules.faction.Role;
import dev.mikan.modules.faction.State;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PeaceStartEvent extends Event implements Cancellable {

    /*
     * Called from GraceStartEvent#Run -> RunTaskLater
     * */

    private final MFaction faction;

    private final Memoria plugin;
    private final FactionModule module;


    public PeaceStartEvent(MFaction faction, Memoria plugin) {
        this.faction = faction;
        this.plugin = plugin;
        this.module = FactionModule.instance();
    }


    public void run(){
        if (cancelled) return;


        Bukkit.getScheduler().runTaskAsynchronously(plugin.getBootstrap(), () -> {
            updateData();

            String peaceMessage = AltairKit.colorize(module.getConfig().getString("state_title.peace.title"));
            String peaceSubMessage = AltairKit.colorize(module.getConfig().getString("state_title.peace.subtitle"));

            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(faction.getId())),peaceMessage,peaceSubMessage);
        });
    }


    private void updateData(){
        faction.setOpponentId(-1);

        faction.setState(State.PEACE);

        faction.setRole(Role.NONE);

        faction.setNextState("");

        module.info("Peace for faction: {}",faction.getId());

        FactionDatabase.instance().update(faction);

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
