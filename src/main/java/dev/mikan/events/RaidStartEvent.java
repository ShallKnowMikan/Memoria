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

public class RaidStartEvent extends Event implements Cancellable {

    /*
    * Called from FactionsListeners onRaidProposalGUIClick
    * */

    private final MFaction attackingFaction;
    private final MFaction defendingFaction;
    private final Memoria plugin;


    public RaidStartEvent(MFaction attackingFaction, MFaction defendingFaction, Memoria plugin) {
        this.attackingFaction = attackingFaction;
        this.defendingFaction = defendingFaction;
        this.plugin = plugin;
    }


    public void run(){
        if (cancelled) return;

        updateData();

        int taskID = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin.getBootstrap(), () -> {
            int taskId = MFaction.MFactions.getRaidTasksCache().remove(attackingFaction.getRaidId());
            // TODO: set instances to grace and update
        },144000L).getTaskId();

        // JUST one faction of the 2 in raid is enough, since they are still bond by the raid ID
        MFaction.MFactions.getRaidTasksCache().put(attackingFaction.getRaidId(), taskID);


        String message = AltairKit.colorize("&cRaid started");
        String subMessage = AltairKit.colorize("&4watch out");

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin.getBootstrap(), () -> {
            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(attackingFaction.getId())),message,subMessage);
            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(defendingFaction.getId())),message,subMessage);

        },4);



        // TODO: start raid
    }

    private void updateData(){
        attackingFaction.setOpponentId(defendingFaction.getId());
        defendingFaction.setOpponentId(attackingFaction.getId());

        attackingFaction.setState(State.RAID);
        defendingFaction.setState(State.RAID);

        attackingFaction.setRole(Role.ATTACKERS);
        defendingFaction.setRole(Role.ATTACKERS);

        String nextState = TimeUtils.next(0,0,0,2,0,0);

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
