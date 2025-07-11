package dev.mikan.events.factions;

import com.massivecraft.factions.Factions;
import dev.mikan.Memoria;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.TimeUtils;
import dev.mikan.database.module.impl.FactionDatabase;
import dev.mikan.modules.faction.FactionModule;
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
    private final FactionModule module;


    public RaidStartEvent(MFaction attackingFaction, MFaction defendingFaction, Memoria plugin) {
        this.attackingFaction = attackingFaction;
        this.defendingFaction = defendingFaction;
        this.plugin = plugin;
        this.module = FactionModule.instance();
    }


    public void run(){
        if (cancelled) return;

        // 144000 in ticks = 2 hours
        int taskID = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin.getBootstrap(), () -> {
            int taskId = MFaction.MFactions.getRaidTasksCache().remove(attackingFaction.getRaidId());

            module.info("Start grace for: {} and {}",attackingFaction.getId(),defendingFaction.getId());
            MFaction.MFactions.startGrace(attackingFaction,defendingFaction);
        },144000).getTaskId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin.getBootstrap(), () -> {
            updateData();
            // JUST one faction of the 2 in raid is enough, since they are still bond by the raid ID
            MFaction.MFactions.getRaidTasksCache().put(attackingFaction.getRaidId(), taskID);


            String message = AltairKit.colorize(module.getConfig().getString("state_title.raid.title"));
            String subMessage = AltairKit.colorize(module.getConfig().getString("state_title.raid.subtitle"));;

            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(attackingFaction.getId())),message,subMessage);
            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(defendingFaction.getId())),message,subMessage);
        });

    }

    private void updateData(){
        attackingFaction.setOpponentId(defendingFaction.getId());
        defendingFaction.setOpponentId(attackingFaction.getId());

        attackingFaction.setState(State.RAID);
        defendingFaction.setState(State.RAID);

        attackingFaction.setRole(Role.ATTACKERS);
        defendingFaction.setRole(Role.DEFENDERS);

        String nextState = TimeUtils.next(0,0,0,0,0,10);

        attackingFaction.setNextState(nextState);
        defendingFaction.setNextState(nextState);

        FactionDatabase.instance().update(attackingFaction);
        FactionDatabase.instance().update(defendingFaction);
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
