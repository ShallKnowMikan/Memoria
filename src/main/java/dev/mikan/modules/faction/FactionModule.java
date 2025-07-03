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
import dev.mikan.database.module.impl.FactionDatabase;
import dev.mikan.listeners.FactionsListeners;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
* Module for factions features here you can
* manage all settings which are factions related
*
* */
public final class FactionModule extends Module {

    private @Getter final Memoria plugin;
    private @Getter final FactionDatabase database;
    private @Getter final Map<Player, RecognitionCache> recognitionCache = new ConcurrentHashMap<>();
    private @Getter FileConfiguration config;

    private @Getter byte curfewStart;
    private @Getter byte curfewEnd;

    public FactionModule(Memoria plugin, String name, Logger logger) {
        super(plugin.getBootstrap(), name, logger);
        this.plugin = plugin;
        database = Singleton.getInstance(FactionDatabase.class,() -> new FactionDatabase(new SQLiteManager(logger,"factions.db"),logger));

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
            if (faction == null) {
                warning("Null: {}",f.getTag());
                return;
            }
            database.update(faction);
        }
        database.updateServerStopLog();
    }

    @Override @SneakyThrows
    public void loadConfig() {
        database.setup();
        database.loadFactions();

        plugin.getConfigManager().load("modules/factions.yml",plugin.getBootstrap());
        config = plugin.getConfigManager().get("modules/factions.yml");

        this.curfewStart = Byte.parseByte(config.getString("curfew.start"));
        this.curfewEnd = Byte.parseByte(config.getString("curfew.end"));
    }

    @Override
    public void registerCommands(Plugin plugin) {
        AltairKit.registerCommands(new FactionCommands(this));
        AltairKit.registerCommands(new MemoriaCommands(this.plugin));
        AltairKit.tabComplete("memoria reload",this.plugin.getModuleNames().keySet().toArray(new String[0]));
        AltairKit.tabComplete("memoria reset", Factions.getInstance().getFactionTags().toArray(new String[0]));
    }

    @Override
    public void registerListeners(Plugin plugin) {
        listen(new FactionsListeners(database));
    }

    public static FactionModule instance(){
        return Singleton.getInstance(FactionModule.class,() -> null);
    }


    private void startTasks(){
        // Since in raid factions should not be processed twice
        // I save here the processed ones and then continue the loop if present
        Set<MFaction> processedFactions = new HashSet<>();

        for (Faction f : Factions.getInstance().getAllFactions()) {
            if (MFaction.MFactions.isDefault(f)) continue;
            MFaction faction = MFaction.MFactions.getByFaction(f);
            if (faction == null || faction.getState() == State.PEACE) return;

            processedFactions.add(faction);

            // Prevents double processing on the same faction
            if (faction.getState() == State.RAID && processedFactions.contains(faction.getOpponent())) continue;

            String stopDatetime = database.getServerStopLog();

            if (stopDatetime.isEmpty()) {
                warning("stop date time is null.");
                return;
            }
            String nextDatetime = faction.getNextState();

            if (nextDatetime.isEmpty()) {
                warning("next date time is null.");
                return;
            }
            /*
            * Since I am considering the time "froze" once the server
            * is shut down I'm calculating how much time was left from the
            * next faction state.
            * In order to start the task correctly and put the next state to the new
            * updated datetime
            * */
            String differentialDatetime = TimeUtils.remaining(stopDatetime,nextDatetime);

            info("nextDatetime: {} ", nextDatetime);
            info("stopDatetime: {} ", stopDatetime);
            info("Differential date time: {} ", differentialDatetime);
            info("Current time: {} ", TimeUtils.current());
            info("Next state: {} ", TimeUtils.add(differentialDatetime));

            faction.setNextState(TimeUtils.add(differentialDatetime));

            long ticks = getTicks(differentialDatetime);
            manageTasks(faction,ticks);

        }
    }


    private void manageTasks(MFaction faction,long ticks){
        int taskId = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin.getBootstrap(),() -> {
            if (faction.getState() == State.RAID) {
                info("Removing raid task id");
                MFaction.MFactions.getRaidTasksCache().remove(faction.getRaidId());

                MFaction.MFactions.startGrace(faction, MFaction.MFactions.getById(faction.getOpponentId()));
            } else if (faction.getState() == State.GRACE){
                info("Removing grace task id");
                MFaction.MFactions.getGraceTasksCache().remove(faction.getId());

                MFaction.MFactions.startPeace(faction);
            }
        },ticks).getTaskId();

        if (faction.getState() == State.RAID) {
            info("putting raid task id");
            // JUST one faction of the 2 in raid is enough, since they are still bond by the raid ID
            MFaction.MFactions.getRaidTasksCache().put(faction.getRaidId(), taskId);

            String message = AltairKit.colorize(this.getConfig().getString("state_title.raid.title"));
            String subMessage = AltairKit.colorize(this.getConfig().getString("state_title.raid.subtitle"));

            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(faction.getId())),message,subMessage);
            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(faction.getOpponentId())),message,subMessage);
        } else if (faction.getState() == State.GRACE) {
            info("putting grace task id");
            // JUST one faction of the 2 in raid is enough, since they are still bond by the raid ID
            MFaction.MFactions.getGraceTasksCache().put(faction.getId(), taskId);

            String graceMessage = AltairKit.colorize(this.getConfig().getString("state_title.grace.title"));
            String graceSubMessage = AltairKit.colorize(this.getConfig().getString("state_title.grace.subtitle"));

            MFaction.MFactions.sendTitle(Factions.getInstance().getFactionById(String.valueOf(faction.getId())),graceMessage,graceSubMessage);
        }
    }


    private long getTicks(String differentialDatetime) {
        String[] tokens = differentialDatetime.split(" ");

        // All those values are expressed in ticks (*20 each second)
        int days = Integer.parseInt(tokens[0].split("-")[2]) * 3600 * 60 * 24;
        int hours = Integer.parseInt(tokens[1].split(":")[0]) * 20 * 60 * 60;
        int minutes = Integer.parseInt(tokens[1].split(":")[1]) * 20 * 60;
        int seconds = Integer.parseInt(tokens[1].split(":")[2]) * 20;

        return days + hours + minutes + seconds;
    }
}
