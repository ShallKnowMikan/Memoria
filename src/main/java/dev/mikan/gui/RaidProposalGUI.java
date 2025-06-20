package dev.mikan.gui;

import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.api.gui.AltairGUI;
import dev.mikan.altairkit.utils.ItemBuilder;
import dev.mikan.modules.faction.FactionModule;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RaidProposalGUI extends AltairGUI {

    private final FactionModule module;
    private final FileConfiguration config;

    public RaidProposalGUI(String title, int slots,FactionModule module) {
        super(title, slots);
        this.module = module;
        this.config = module.getConfig();

        // Building filler item

        loadFillerItem();

        this.build(this.fillerItem,true);

        loadItem("confirm");
        loadItem("deny");
    }

    private void loadItem(String configName){
        String[] typeTokens = config.getString("gui.raid-proposal.items."+configName+".type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        byte position = (byte) config.getInt("gui.raid-proposal.items."+configName+".position");
        String name = AltairKit.colorize(config.getString("gui.raid-proposal.items."+configName+".name"));
        List<String> lore = config.getStringList("gui.raid-proposal.items."+configName+".lore");
        lore.forEach(line -> line = AltairKit.colorize(line));

        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);

        ItemStack item = new ItemBuilder(startingItem)
                .setName(name)
                .setLore(lore)
                .toItemStack();

        this.gui.setItem(position,item);
    }


    private void loadFillerItem(){
        String[] typeTokens = config.getString("gui.raid-proposal.items.filler.type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        String name = AltairKit.colorize(config.getString("gui.raid-proposal.items.filler.name"));
        List<String> lore = config.getStringList("gui.raid-proposal.items.filler.lore");
        lore.forEach(line -> line = AltairKit.colorize(line));


        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);
        this.fillerItem = new ItemBuilder(startingItem)
                .setName(name)
                .setLore(lore)
                .toItemStack();

    }

}
