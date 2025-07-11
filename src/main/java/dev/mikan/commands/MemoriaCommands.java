package dev.mikan.commands;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import dev.mikan.Memoria;
import dev.mikan.altairkit.api.commands.AltairCMD;
import dev.mikan.altairkit.api.commands.SenderType;
import dev.mikan.altairkit.api.commands.actors.CMDActor;
import dev.mikan.altairkit.api.commands.annotations.Command;
import dev.mikan.altairkit.api.commands.annotations.Description;
import dev.mikan.altairkit.api.commands.annotations.Permission;
import dev.mikan.altairkit.api.commands.annotations.Sender;
import dev.mikan.altairkit.utils.Module;
import dev.mikan.modules.core.Core;
import dev.mikan.modules.faction.MFaction;

import java.util.List;

public class MemoriaCommands {

    private final Memoria plugin;

    private final static List<String> helpLines = List.of(
            "------------------------------------------------------------------------------------------",
            "/memoria reload  &8-->&7 reloads MemoriaCore plugin.",
            "/f data <faction> &8-->&7 shows a faction's raid data.",
            "/f data &8-->&7 shows own faction's raid data.",
            "/f bombers &8-->&7 allows a faction leader and coleader to select bombers.",
            "/f raid stop &8-->&7 stops the raid.",
            "/f reset <faction> &8-->&7 resets specified faction and opponent's states.",
            "/f reset &8-->&7 resets own faction and opponent states.",
            "/f core give &8-->&7 gives the core lv1 item.",
            "/f core remove &8-->&7 removes the core giving its item to the player.",
            "/poligono add &8-->&7 spawns a shooting range npc",
            "/poligono remove &8-->&7 removes all the sr-npcs within a 20 block area or the one selected.",
            "/poligono sel (looking to an npc) &8-->&7 selects an npc.",
            "/poligono rotate &8-->&7 rotates a sr-npcs",
            "/warpbalancer <balancer-name> &8-->&7 send a player to a warp inserted in a balancer list, selecting the one with less online.",
            "/warpbalancer <balancer-name> <player> &8-->&7 send a player to a warp inserted in a balancer list, selecting the one with less online.",
            "/plevel <number> &8-->&7 sets the level of the shooting range npc's armor protection.",
            "/plevel <number> <player> &8-->&7 sets the level for a targeted player.",
            "------------------------------------------------------------------------------------------"

    );

    public MemoriaCommands(Memoria plugin) {
        this.plugin = plugin;
    }

    @Command("memoria help")
    @Description("help command.")
    public void help(AltairCMD cmd, CMDActor actor){
        helpLines.forEach(actor::reply);
    }


    @Command("memoria reload")
    @Permission("dev.mikan.reload.memoria")
    public void reload(AltairCMD cmd, CMDActor actor,String moduleName){
        if (!moduleName.isEmpty()){

            Class<? extends Module> moduleClass = this.plugin.getModuleNames().get(moduleName.toLowerCase());
            Module module = this.plugin.getModules().get(moduleClass);
            if (module == null) {
                actor.reply("&7Specified module not found.");
                return;
            }
            module.onReload();
            return;
        }

        this.plugin.loadFiles();
        this.plugin.getModules().values().forEach(Module::onReload);
        actor.reply("&7All modules have been reloaded.");

    }

//    @Command("memoria test")
//    public void test(AltairCMD cmd, CMDActor actor,int years,
//                     int months,
//                     int days,
//                     int hours,
//                     int minutes,
//                     int seconds){
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//        actor.reply(formatter.format(ZonedDateTime.now(ZoneId.of("Europe/Rome"))));
//        String date = TimeUtils.next(years,months,days,hours,minutes,seconds);
//        actor.reply(date);
//        actor.reply(String.valueOf(TimeUtils.isExpired(date)));
//
//
//    }

    @Command("memoria reset")
    public void reset(AltairCMD cmd, CMDActor actor, String factionName){

        Faction f = Factions.getInstance().getByTag(factionName);
        if (f == null) return;

        MFaction faction = MFaction.MFactions.getById(Integer.parseInt(f.getId()));

        MFaction.MFactions.reset(faction);
        actor.reply("&9Faction reset.");

    }

    @Command("memoria testing")
    @Sender(SenderType.PLAYER)
    public void test(AltairCMD cmd, CMDActor actor){

        MFaction faction = MFaction.MFactions.getByPlayer(actor.asPlayer());
        if (faction == null || !faction.hasCore()) return;

        Core core = faction.getCore();
        core.destruct(false);

    }


}
