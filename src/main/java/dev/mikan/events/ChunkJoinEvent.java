package dev.mikan.events;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ChunkJoinEvent extends Event implements Cancellable {

    /*
    * Not an ordinary event
    * if cancelled will not be cancelled at a bukkit level
    * but will teleport back the player of BLOCKS_BACK blocks.
    * */

    private static final HandlerList HANDLERS = new HandlerList();
    private final Faction factionTo;
    private final Faction factionFrom;
    private final Player player;
    private final FPlayer fPlayer;
    private final Location from;
    private final Location to;
    private boolean cancelled;

    // Blocks to tp the player back
    private final int BLOCKS_BACK = 5;

    public ChunkJoinEvent(Faction factionTo, Faction factionFrom, Player player, Location from, Location to) {
        this.factionTo = factionTo;
        this.factionFrom = factionFrom;
        this.player = player;
        this.fPlayer = FPlayers.getInstance().getByPlayer(player);
        this.from = from;
        this.to = to;
    }


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
        cancelMovement();
        cancelled = b;
    }

    private void cancelMovement() {
        // Calculates a vector pointing to the opposite distance
        double dx = from.getX() - to.getX();
        double dz = from.getZ() - to.getZ();

        // normalize it
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length != 0) {
            dx /= length;
            dz /= length;
        }

        // Get the new position multiplying the distances * 2 once per block to tp back
        double finalDx = dx;
        double finalDz = dz;

        Location newLocation = to.clone().add(finalDx * BLOCKS_BACK, 0, finalDz * BLOCKS_BACK);
        newLocation.setYaw(from.getYaw());
        newLocation.setPitch(from.getPitch());

        player.teleport(newLocation);
    }
}
