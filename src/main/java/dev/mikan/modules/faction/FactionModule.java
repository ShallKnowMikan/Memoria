package dev.mikan.modules.faction;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import dev.mikan.Memoria;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.Module;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.altairkit.utils.TimeUtils;
import dev.mikan.commands.FactionCommands;
import dev.mikan.commands.MemoriaCommands;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.impl.FactionsDB;
import dev.mikan.listeners.FactionsListeners;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
* Module for factions features here you can
* manage all settings which are factions related
*
* */
public final class FactionModule extends Module implements Singleton {

    private @Getter final Memoria plugin;
    private @Getter final FactionsDB database;
    private @Getter final Map<Player, RecognitionCache> recognitionCache = new ConcurrentHashMap<>();
    private @Getter FileConfiguration config;

    private @Getter byte curfewStart;
    private @Getter byte curfewEnd;

    public FactionModule(Memoria plugin, String name, Logger logger) {
        super(plugin.getBootstrap(), name, logger);
        this.plugin = plugin;
        database = Singleton.getInstance(FactionsDB.class,() -> new FactionsDB(new SQLiteManager(logger,"factions.db"),logger));
    }

    @Override
    public void onEnable() {
        loadConfig();
        registerListeners(plugin.getBootstrap());
        registerCommands(plugin.getBootstrap());
        startTasks();
    }

    @Override
    public void onReload() {
        loadConfig();
        info("Reloaded.");
    }

    @Override
    public void onDisable() {
        for (Faction f : Factions.getInstance().getAllFactions()) {
            if (MFaction.MFactions.isDefault(f)) continue;
            MFaction faction = MFaction.MFactions.getByFaction(f);
            database.update(faction);
        }
    }

    @Override @SneakyThrows
    public void loadConfig() {
        database.setup();
        database.loadFactions();

        plugin.getConfigManager().load("modules/factions.yml",plugin.getBootstrap().getResource("modules/factions.yml"));
        config = plugin.getConfigManager().get("modules/factions.yml");

        this.curfewStart = Byte.parseByte(config.getString("curfew.start"));
        this.curfewEnd = Byte.parseByte(config.getString("curfew.end"));
    }

    @Override
    public void registerCommands(Plugin plugin) {
        AltairKit.registerCommands(new FactionCommands(this));
        AltairKit.registerCommands(new MemoriaCommands(this.plugin));
        AltairKit.tabComplete("memoria reload",this.plugin.getModules().keySet().toArray(new String[0]));
        AltairKit.tabComplete("memoria reset", Factions.getInstance().getFactionTags().toArray(new String[0]));
    }

    @Override
    public void registerListeners(Plugin plugin) {
        listen(new FactionsListeners(database));
    }

    public static FactionModule instance(){
        return Singleton.getInstance(FactionModule.class,() -> null);
    }

    /*
    * Restarts the next state task for each faction which is not in peace state
    * By getting the remaining time in ticks and calling a runTaskLaterAsync
    * */
    private void startTasks(){
        for (Faction f : Factions.getInstance().getAllFactions()) {
            if (MFaction.MFactions.isDefault(f)) continue;
            MFaction faction = MFaction.MFactions.getByFaction(f);

            if (faction.getState() == State.PEACE || faction.getNextState().isEmpty()) continue;
            String nextDatetime = faction.getNextState();
            long ticks;

            if (! TimeUtils.isExpired(nextDatetime)) {
                String datetimeLeft = TimeUtils.remaining(nextDatetime);
                String[] tokens = datetimeLeft.split(" ");

                int days = Integer.parseInt(tokens[0].split("-")[2]) * 3600 * 60 * 24;
                int hours = Integer.parseInt(tokens[1].split("-")[0]) * 20 * 60 * 60;
                int minutes = Integer.parseInt(tokens[1].split("-")[1]) * 20 * 60;
                int seconds = Integer.parseInt(tokens[1].split("-")[2]) * 20;

                ticks = days + hours + minutes + seconds;
            } else ticks = 20;




            int taskID = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin.getBootstrap(),() -> {
                if (faction.getState() == State.RAID) {
                    MFaction.MFactions.getRaidTasksCache().remove(faction.getRaidId());

                    MFaction.MFactions.startGrace(faction, MFaction.MFactions.getById(faction.getOpponentId()));
                } else if (faction.getState() == State.GRACE){
                    MFaction.MFactions.getGraceTasksCache().remove(faction.getId());

                    MFaction.MFactions.startPeace(faction);
                }

            },ticks).getTaskId();

            if (faction.getState() == State.RAID) {
                // JUST one faction of the 2 in raid is enough, since they are still bond by the raid ID
                MFaction.MFactions.getRaidTasksCache().put(faction.getRaidId(), taskID);

                String message = AltairKit.colorize(this.getConfig().getString("state_title.raid.title"));
                String subMessage = AltairKit.colorize(this.getConfig().getString("state_title.raid.subtitle"));;

                MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(faction.getId())),message,subMessage);
                MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(faction.getOpponentId())),message,subMessage);
            } else if (faction.getState() == State.GRACE) {
                // JUST one faction of the 2 in raid is enough, since they are still bond by the raid ID
                MFaction.MFactions.getGraceTasksCache().put(faction.getId(), taskID);


                MFaction.MFactions.startPeace(faction);

                String graceMessage = AltairKit.colorize(this.getConfig().getString("state_title.grace.title"));
                String graceSubMessage = AltairKit.colorize(this.getConfig().getString("state_title.grace.subtitle"));

                MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(faction.getId())),graceMessage,graceSubMessage);
            }

        }
    }
}
