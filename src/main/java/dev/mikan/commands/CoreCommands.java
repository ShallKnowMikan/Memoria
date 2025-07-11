package dev.mikan.commands;

import dev.mikan.altairkit.api.commands.AltairCMD;
import dev.mikan.altairkit.api.commands.SenderType;
import dev.mikan.altairkit.api.commands.actors.CMDActor;
import dev.mikan.altairkit.api.commands.annotations.Command;
import dev.mikan.altairkit.api.commands.annotations.Description;
import dev.mikan.altairkit.api.commands.annotations.Sender;
import dev.mikan.altairkit.utils.ItemBuilder;
import dev.mikan.modules.core.CoreModule;
import dev.mikan.modules.core.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class CoreCommands {

    private final CoreModule module;
    private final FileConfiguration config;
    private final FileConfiguration lang;

    public CoreCommands(CoreModule module) {
        this.module = module;
        this.config = module.getConfig();
        this.lang = module.getPlugin().getLang();
    }

    @Command("core get")
    @Sender(SenderType.PLAYER)
    @Description("gives core to the executor.")
    public void get(AltairCMD cmd, CMDActor actor){
        ItemBuilder builder = new ItemBuilder(module.getCoreBaseItem())
                .setName(module.getCoreBaseItem().getItemMeta().getDisplayName()
                        .replace("%core_level%",Level.Levels.defaultLevel.name()));
        ItemStack coreItem = builder.toItemStack();
        actor.asPlayer().getInventory().addItem(
                coreItem
        );

        module.info("Lore: {}",coreItem.getItemMeta().getLore());
        actor.reply(lang.getString("cores.on_command.core_get"));
    }

}
