package dev.mikan.events;

import com.massivecraft.factions.Factions;
import dev.mikan.Memoria;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.TimeUtils;
import dev.mikan.database.module.impl.FactionsDB;
import dev.mikan.modules.faction.MFaction;
import dev.mikan.modules.faction.Role;
import dev.mikan.modules.faction.State;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GraceStartEvent extends Event implements Cancellable {

    /*
     * Called from RaidStartEvent#Run -> RunTaskLater
     * */

    private final MFaction attackingFaction;
    private final MFaction defendingFaction;
    private final Memoria plugin;


    public GraceStartEvent(MFaction attackingFaction, MFaction defendingFaction, Memoria plugin) {
        this.attackingFaction = attackingFaction;
        this.defendingFaction = defendingFaction;
        this.plugin = plugin;
    }


    public void run(){
        if (cancelled) return;

        // 3456000 in ticks = 48 hours
        int taskID = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin.getBootstrap(), () -> {
            MFaction.MFactions.getGraceTasksCache().remove(defendingFaction.getId());

            MFaction.MFactions.startPeace(defendingFaction);
        },3456000L).getTaskId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin.getBootstrap(), () -> {
            updateData();

            // JUST one faction of the 2 in raid is enough, since they are still bond by the raid ID
            MFaction.MFactions.getGraceTasksCache().put(defendingFaction.getId(), taskID);


            MFaction.MFactions.startPeace(attackingFaction);

            String graceMessage = AltairKit.colorize("&5Grace phase started");
            String graceSubMessage = AltairKit.colorize("&dyour faction is currently in peace");

            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(defendingFaction.getId())),graceMessage,graceSubMessage);
        });
    }


    private void updateData(){
        attackingFaction.setOpponentId(-1);
        defendingFaction.setOpponentId(-1);

        attackingFaction.setState(State.PEACE);
        defendingFaction.setState(State.GRACE);

        attackingFaction.setRole(Role.NONE);
        defendingFaction.setRole(Role.NONE);

        String nextState = TimeUtils.next(0,0,2,0,0,0);

        attackingFaction.setNextState(nextState);
        defendingFaction.setNextState(nextState);

        FactionsDB.instance().update(attackingFaction);
        FactionsDB.instance().update(defendingFaction);
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
