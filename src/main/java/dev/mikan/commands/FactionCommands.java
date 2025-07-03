package dev.mikan.commands;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.perms.Role;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.api.commands.AltairCMD;
import dev.mikan.altairkit.api.commands.SenderType;
import dev.mikan.altairkit.api.commands.actors.CMDActor;
import dev.mikan.altairkit.api.commands.annotations.Command;
import dev.mikan.altairkit.api.commands.annotations.Description;
import dev.mikan.altairkit.api.commands.annotations.Sender;
import dev.mikan.altairkit.utils.TimeUtils;
import dev.mikan.gui.BombersGUI;
import dev.mikan.modules.faction.FactionModule;
import dev.mikan.modules.faction.MFaction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FactionCommands {

    private final FactionModule module;
    private final FileConfiguration config;

    public FactionCommands(FactionModule module) {
        this.module = module;
        this.config = module.getConfig();
    }

    public final static String F_CMD_FAKE_ROOT = "emmikanquelloreal";
    public final static Set<String> F_COMMANDS = Set.of(
            "bombers",
            "data"
    );

    @Command(F_CMD_FAKE_ROOT + " bombers")
    @Sender(SenderType.PLAYER)
    public void bombers(AltairCMD cmd, CMDActor actor){
        FPlayer player = FPlayers.getInstance().getByPlayer(actor.asPlayer());

        if (!player.getRole().isAtLeast(Role.COLEADER)) {
            player.sendMessage(AltairKit.colorize(
                    module.getPlugin().getLang().getString("factions.on_bombers_cmd_prohibition")
            ));
            return;
        }

        String title = AltairKit.colorize(module.getConfig().getString("gui.bombers.title"));
        int size = module.getConfig().getInt("gui.bombers.size");
        BombersGUI bombersGUI = new BombersGUI(title, size,FPlayers.getInstance().getByPlayer(actor.asPlayer()).getFaction(),1);


        bombersGUI.show(actor.asPlayer());
    }

    @Command(F_CMD_FAKE_ROOT + " bombers show")
    public void bombersShow(AltairCMD cmd, CMDActor actor){

    }


    @Command(F_CMD_FAKE_ROOT + " data")
    @Description("Shows a faction raid data")
    public void data(AltairCMD cmd, CMDActor actor,String factionName){
        MFaction faction;
        if (factionName.isEmpty()){
            if (actor.isConsole()) return;
            faction = MFaction.MFactions.getByPlayer(actor.asPlayer());

        } else faction = MFaction.MFactions.getByName(factionName);
        processData(faction,actor.asPlayer());
    }




    private void processData(MFaction faction,Player target){
        if (faction == null || target == null) return;
        Faction massive = Factions.getInstance().getFactionById(String.valueOf(faction.getId()));
        String fName = massive.getTag();
        Set<Player> onlineBombers = new HashSet<>();
        for (UUID uuid : faction.getBombers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            onlineBombers.add(player);
        }

        String time = switch (faction.getState()){
            case PEACE -> "0";
            case GRACE,RAID -> TimeUtils.formatDatetime(faction.getNextState(),false);
        };

        target.sendMessage(ChatColor.GRAY + "next state: " + faction.getNextState());

        String timeLeft = switch (faction.getState()){
            case PEACE -> "0";
            case RAID, GRACE -> TimeUtils.formatDatetime(faction.getNextState(),true);
        };

        int onlineNumber = massive.getOnlinePlayers().size();

        List<String> holder = switch (faction.getState()){
            case PEACE -> config.getStringList("cmd.data.peace");
            case RAID -> config.getStringList("cmd.data.raid");
            case GRACE -> config.getStringList("cmd.data.grace");
        };
        for (String line : holder) {
            target.sendMessage(AltairKit.colorize(
                    line
                            .replace("%faction%",fName)
                            .replace("%state%",faction.getState().name().toLowerCase())
                            .replace("%online-members%",String.valueOf(onlineNumber))
                            .replace("%online-bombers%",String.valueOf(onlineBombers.size()))
                            .replace("%max-bombers%",String.valueOf(faction.maxBombers()))
                            .replace("%time%",time)
                            .replace("%time-left%",timeLeft)
            ));
        }

    }


}
