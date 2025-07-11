package dev.mikan.gui.factions;

import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.api.gui.AltairGUI;
import dev.mikan.altairkit.utils.ItemBuilder;
import dev.mikan.altairkit.utils.NBTUtils;
import dev.mikan.modules.faction.FactionModule;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RaidProposalGUI extends AltairGUI {

    public static final String ITEMS_TAG = "raid-proposal-gui";

    private final FactionModule module;
    private final FileConfiguration config;
    private final int attackingFactionId;
    private final int defendingFactionId;


    public static ItemStack confirmItem;
    public static ItemStack recognitionItem;
    public static ItemStack denyItem;



    public RaidProposalGUI(String title, int slots, FactionModule module, int attackingFactionId, int defendingFactionId) {
        super(title, slots);
        this.module = module;
        this.config = module.getConfig();
        this.attackingFactionId = attackingFactionId;
        this.defendingFactionId = defendingFactionId;

        // Building filler item

        loadFillerItem();

        this.build(this.fillerItem,this,true);

        confirmItem = loadItem("confirm");
        recognitionItem = loadItem("recognition");
        denyItem = loadItem("deny");
    }

    private ItemStack loadItem(String configName){
        String[] typeTokens = config.getString("gui.raid_proposal.items."+configName+".type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        byte position = (byte) config.getInt("gui.raid_proposal.items."+configName+".position");
        String name = AltairKit.colorize(config.getString("gui.raid_proposal.items."+configName+".name"));
        List<String> lore = config.getStringList("gui.raid_proposal.items."+configName+".lore");
        List<String> colorizedLore = new ArrayList<>();
        lore.forEach(line -> colorizedLore.add(AltairKit.colorize(line)));



        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);

        ItemStack item = new ItemBuilder(startingItem)
                .setName(name)
                .setLore(colorizedLore)
                .toItemStack();

        // value format -> "attacking-faction:1 defending-faction:2"
        item = NBTUtils.set(item,ITEMS_TAG,"attacking-faction:"+attackingFactionId+" defending-faction:"+defendingFactionId);
        this.gui.setItem(position,item);
        return item;
    }


    private void loadFillerItem(){
        String[] typeTokens = config.getString("gui.raid_proposal.items.filler.type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        String name = AltairKit.colorize(config.getString("gui.raid_proposal.items.filler.name"));
        List<String> lore = config.getStringList("gui.raid-proposal.items.filler.lore");
        List<String> colorizedLore = new ArrayList<>();
        lore.forEach(line -> colorizedLore.add(AltairKit.colorize(line)));


        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);
        this.fillerItem = new ItemBuilder(startingItem)
                .setName(name)
                .setLore(colorizedLore)
                .toItemStack();

    }


    @Override
    public Inventory getInventory() {
        return this.gui;
    }
}
