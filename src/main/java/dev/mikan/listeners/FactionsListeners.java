package dev.mikan.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.event.FactionAutoDisbandEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import dev.mikan.database.module.impl.FactionsDB;
import dev.mikan.events.ChunkJoinEvent;
import dev.mikan.gui.RaidProposalGUI;
import dev.mikan.modules.faction.FactionModule;
import dev.mikan.modules.faction.MFaction;
import dev.mikan.modules.faction.State;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Set;

public class FactionsListeners implements Listener {

    private final FactionsDB database;
    private final FactionModule module;
    private final FileConfiguration factionConfig;

    public final static String F_CMD_FAKE_ROOT = "emmikanquelloreal";
    private final static Set<String> F_COMMANDS = Set.of(
            "bombers"
    );

    public FactionsListeners(FactionsDB database) {
        this.database = database;
        this.module = FactionModule.instance();
        this.factionConfig = module.getConfig();
    }

    // Inserts the just created faction into the database
    @EventHandler public void onCreate(FactionCreateEvent e){
        database.insert(e.getFaction());
    }
    // Deletes the just created faction from the database
    @EventHandler public void onDisband(FactionDisbandEvent e){
        database.delete(e.getFaction());
    }

    // Deletes the just created faction from the database
    @EventHandler public void onDisband(FactionAutoDisbandEvent e){
        database.delete(e.getFaction());
    }

    /*
     * --------------------------------------------------
     * F commands adapters stuff down here
     * --------------------------------------------------
     * */

    @EventHandler public void onFCommand(PlayerCommandPreprocessEvent e){
        if (!e.getMessage().startsWith("/f")
                || !(e.getMessage().split(" ").length >= 2
                    && F_COMMANDS.contains(e.getMessage().split(" ")[1].toLowerCase()))) return;

        String command = e.getMessage().replaceFirst("f",F_CMD_FAKE_ROOT).substring(1);
        e.setCancelled(true);
        e.getPlayer().performCommand(command);
    }

    /*
    * --------------------------------------------------
    * Claim change stuff down here
    * --------------------------------------------------
    * */

    /*
    * Join others claim while in raid -> tp back
    * Join others claim while in grace -> tp back
    *
    * Join others claim while they are in raid -> tp back
    * Join others claim while they are in grace -> tp back
    *
    * Join others claim while in no faction AND faction to is not wilderness -> tp back
    *
    * */
    @EventHandler public void onClaimJoin(ChunkJoinEvent e){

        // Join others claim while in no faction AND faction to is not wilderness -> tp back
        if (e.getFPlayer().getFaction().isWilderness() && (!e.getFactionTo().isWilderness()) && !e.getFactionTo().isSafeZone() && !e.getFactionTo().isWarZone()) {
            e.setCancelled(true);
            return;
        }

        MFaction playersFaction = MFaction.MFactions.getByPlayer(e.getFPlayer());

        if (playersFaction != null && playersFaction.getState() != State.PEACE) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.GRAY + "Busy message!");
            return;
        }

        MFaction factionTo = MFaction.MFactions.getByFaction(e.getFactionTo());

        // If factionto is wilderness safezone or warzone OR is own faction -> return
        if (factionTo == null || factionTo.getId() == playersFaction.getId()) return;

        if (factionTo.getState() == State.PEACE) {
            e.setCancelled(true);

            String title = factionConfig.getString("gui.raid-proposal.title")
                    .replace("%faction%",e.getFactionTo().getTag());
            int size = factionConfig.getInt("gui.raid-proposal.size");
            RaidProposalGUI gui = new RaidProposalGUI(title,size,module);

            gui.show(e.getPlayer());
            return;
        }
    }




    @EventHandler public void onLandChange(PlayerMoveEvent e){
        boolean changedBlock = !(e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ());

        boolean changedChunk = !e.getFrom().getChunk().toString().equals(e.getTo().getChunk().toString());



        if (!changedBlock || !changedChunk) {
            return;
        }

        ChunkJoinEvent event = new ChunkJoinEvent(
                Board.getInstance().getFactionAt(new FLocation(e.getTo())),
                Board.getInstance().getFactionAt(new FLocation(e.getFrom())),
                e.getPlayer(),
                e.getFrom(),
                e.getTo()
        );
        Bukkit.getPluginManager().callEvent(event);
    }






    /*
    * TODO: implement this listener in order to make possible raid declaration
    *  then build up the whole raid system. Good luck, you're the best.
    * */
    @EventHandler public void onRaidProposalGUIClick(InventoryClickEvent e){

    }
}
