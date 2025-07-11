package dev.mikan.events.cores;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.modules.core.Core;
import dev.mikan.modules.core.CoreModule;
import dev.mikan.modules.core.Level;
import eu.decentsoftware.holograms.api.DHAPI;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class CoreLevelUpEvent extends Event implements Cancellable {

    /*
    * The success variable depends on the money
    * if the player has permission and enough money
    * then success is true
    *
    * if success is true the level will be actually updated
    * on database and core instance
    * */

    private final Core core;
    private final Level oldLevel;
    private final Level newLevel;
    private boolean success;
    private final Economy economy;
    private final Player player;
    private final FileConfiguration lang;
    private final CoreModule module;

    public CoreLevelUpEvent(Core core, Player player) {
        this.core = core;
        this.oldLevel = core.getLevel();
        this.newLevel = Level.Levels.cache.get(oldLevel.next());
        this.module = CoreModule.instance();
        this.economy = module.getEconomy();
        this.player = player;
        this.lang = module.getPlugin().getLang();
    }

    public boolean run(){
        if (cancelled) return false;

        if (oldLevel.next() == 0) return false;

        Faction massive = Factions.getInstance().getFactionById(String.valueOf(core.getFaction().getId()));

        if (take(player,oldLevel.upgradeCost())) {
            massive.sendMessage(AltairKit.colorize(lang.getString("cores.on_level_up.success").replace("%level%", newLevel.name())));
            update(core);
            // If it has reached max level will return true
            return newLevel.next() == 0;
        }
        else player.sendMessage(AltairKit.colorize(lang.getString("cores.on_level_up.failure")));

        return false;
    }

    public boolean take(OfflinePlayer player, double amount) {
        if (economy.has(player, amount)) {
            economy.withdrawPlayer(player, amount);
        } else {
            return false;
        }
        return true;
    }

    private void update(Core core){
        this.core.setLevel(newLevel);
        this.module.getDatabase().update(core);

        core.setHealth(newLevel.health());

        core.updateHologramLines();

        DHAPI.setHologramLines(core.getHologram(),core.getHologramLines());
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
