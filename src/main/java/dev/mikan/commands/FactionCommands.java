package dev.mikan.commands;

import dev.mikan.altairkit.api.commands.AltairCMD;
import dev.mikan.altairkit.api.commands.actors.CMDActor;
import dev.mikan.altairkit.api.commands.annotations.Command;
import dev.mikan.altairkit.api.commands.annotations.Description;
import dev.mikan.listeners.FactionsListeners;

public class FactionCommands {

    @Command(FactionsListeners.F_CMD_FAKE_ROOT + " bombers")
    public void bombers(AltairCMD cmd, CMDActor actor){
        actor.reply("Ok ok got it");
    }


    @Command(FactionsListeners.F_CMD_FAKE_ROOT + " data")
    @Description("Shows a faction raid data")
    public void data(AltairCMD cmd, CMDActor actor,String faction){

    }
}
