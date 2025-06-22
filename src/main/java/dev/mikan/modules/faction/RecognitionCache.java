package dev.mikan.modules.faction;

import com.massivecraft.factions.Faction;
import org.bukkit.GameMode;
import org.bukkit.Location;


public record RecognitionCache(int taskId, Faction factionIn, GameMode lastGamemode, Location location) {

}
