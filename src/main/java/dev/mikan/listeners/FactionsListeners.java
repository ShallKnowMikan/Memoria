package dev.mikan.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.FactionAutoDisbandEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.NBTUtils;
import dev.mikan.altairkit.utils.NmsUtils;
import dev.mikan.database.module.impl.FactionsDB;
import dev.mikan.events.ChunkJoinEvent;
import dev.mikan.gui.RaidProposalGUI;
import dev.mikan.modules.faction.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
    * TODO: check if not in curfew before showing raid GUI
    * */
    @EventHandler public void onClaimJoin(ChunkJoinEvent e){

        // Join others claim while in no faction AND faction to is not wilderness -> tp back
        if (e.getFPlayer().getFaction().isWilderness() && (!e.getFactionTo().isWilderness()) && !e.getFactionTo().isSafeZone() && !e.getFactionTo().isWarZone()) {
            e.setCancelled(true);
            return;
        }

        // If joining a different land from the one saved in cache while in recognition
        if (module.getRecognitionCache().containsKey(e.getPlayer())){
            RecognitionCache cache = module.getRecognitionCache().get(e.getPlayer());
            if (!e.getFactionTo().getId().equals(cache.factionIn().getId())){
                e.getPlayer().setGameMode(cache.lastGamemode());
                e.getPlayer().teleport(cache.location());
                Bukkit.getScheduler().cancelTask(cache.taskId());
                module.getRecognitionCache().remove(e.getPlayer());
            }
        }


        MFaction playersFaction = MFaction.MFactions.getByPlayer(e.getFPlayer());

        if (playersFaction != null && playersFaction.getState() != State.PEACE) {
            if (playersFaction.getState() == State.RAID && playersFaction.getRole() == Role.ATTACKERS) return;
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
            RaidProposalGUI gui = new RaidProposalGUI(title,size,module, playersFaction.getId(), factionTo.getId());

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
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR || !NBTUtils.hasKey(e.getCurrentItem(),RaidProposalGUI.ITEMS_TAG)) return;

        // confirm item clicked
        // else deny item was clicked
        if (e.getCurrentItem().toString()
                .equals(RaidProposalGUI.confirmItem.toString())){

            e.getWhoClicked().closeInventory();

            int attackingFactionID = Integer.parseInt(NBTUtils.value(e.getCurrentItem(), RaidProposalGUI.ITEMS_TAG, String.class).toString().split(" ")[0].split(":")[1]);
            int defendingFactionID = Integer.parseInt(NBTUtils.value(e.getCurrentItem(), RaidProposalGUI.ITEMS_TAG, String.class).toString().split(" ")[1].split(":")[1]);

            MFaction attackingFaction = MFaction.MFactions.getFactions().get(attackingFactionID);
            MFaction defendingFaction = MFaction.MFactions.getFactions().get(defendingFactionID);

            MFaction.MFactions.startRaid(attackingFaction,defendingFaction);

        } else if (e.getCurrentItem().toString()
                .equals(RaidProposalGUI.recognitionItem.toString())) {
            e.getWhoClicked().closeInventory();
            Player player = (Player) e.getWhoClicked();
            int defendingFactionID = Integer.parseInt(NBTUtils.value(e.getCurrentItem(), RaidProposalGUI.ITEMS_TAG, String.class).toString().split(" ")[1].split(":")[1]);

            // 3600 = 3 minutes in ticks
            int taskId = Bukkit.getScheduler().runTaskLater(module.getPlugin().getBootstrap(),() -> {
                RecognitionCache cache = module.getRecognitionCache().remove(player);
                player.setGameMode(cache.lastGamemode());
                player.teleport(cache.location());
            },3600).getTaskId();

            module.getRecognitionCache().put(player,new RecognitionCache(taskId, Factions.getInstance().getFactionById(String.valueOf(defendingFactionID)), player.getGameMode(),player.getLocation()));
            player.setGameMode(GameMode.SPECTATOR);

            Location ahead = player.getLocation().clone().add(player.getLocation().getDirection().normalize().multiply(7));
            player.teleport(ahead);

            NmsUtils.sendActionBar(player, AltairKit.colorize("&9Recognition started."));
        } else if (e.getCurrentItem().toString()
                .equals(RaidProposalGUI.denyItem.toString())) {
            e.getWhoClicked().closeInventory();
        }
    }

}
