package dev.mikan.gui.cores;

import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.api.gui.AltairGUI;
import dev.mikan.altairkit.utils.ItemBuilder;
import dev.mikan.altairkit.utils.NBTUtils;
import dev.mikan.modules.core.CoreModule;
import dev.mikan.modules.core.Level;
import dev.mikan.modules.faction.MFaction;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoreGUI extends AltairGUI {

    private final CoreModule module;
    private final FileConfiguration config;
    private final MFaction faction;
    private final Level nextLevel;

    public final static String UPGRADE_ITEM_KEY = "upgrade";
    public final static String REMOVE_ITEM_KEY = "remove";

    private final Map<String,Integer> nbtCache;

    public CoreGUI(String title, int slots, MFaction faction) {
        super(title, slots);
        this.module = CoreModule.instance();
        this.config = module.getConfig();
        this.faction = faction;

        boolean isMaxLevel = faction.getCore().getLevel().next() == 0 ;

        this.nextLevel = isMaxLevel ? null : Level.Levels.cache.get(faction.getCore().getLevel().next());

        this.nbtCache = Map.of(
                REMOVE_ITEM_KEY,faction.getId(),
                UPGRADE_ITEM_KEY,faction.getId()
        );

        this.loadFillerItem();

        this.build(fillerItem,this,true);

        setItems(REMOVE_ITEM_KEY,faction.getId());

        if (isMaxLevel) setUpgradeToMaxLevelItem(this.gui,this.faction);
        else setItems(UPGRADE_ITEM_KEY,faction.getId());
    }

    private void setItems(String node,int nbtValue){
        List<Byte> slots = config.getByteList("gui.item."+node+".slots");
        for (Byte slot : slots) {
            this.gui.setItem(slot, NBTUtils.set(loadItem(node),node,nbtValue));
        }
    }

    public static void setUpgradeToMaxLevelItem(Inventory gui,MFaction faction){
        FileConfiguration config = CoreModule.instance().getConfig();
        String[] typeTokens = config.getString("gui.item.max_level.type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        String name = AltairKit.colorize(config.getString("gui.item.max_level.name"));
        List<String> lore = AltairKit.colorize(config.getStringList("gui.item.max_level.lore"));

        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);
        ItemStack item =  new ItemBuilder(startingItem)
                .setName(name)
                .setLore(lore)
                .toItemStack();
        List<Byte> slots = CoreModule.instance().getConfig().getByteList("gui.item.upgrade.slots");

        for (Byte slot : slots) {
            gui.setItem(slot,NBTUtils.set(item,UPGRADE_ITEM_KEY,faction.getId()));
        }
    }


    private ItemStack loadItem(String node){
        String[] typeTokens = config.getString("gui.item."+node+".type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        String name = AltairKit.colorize(config.getString("gui.item."+node+".name"));
        List<String> lore = AltairKit.colorize(config.getStringList("gui.item."+node+".lore"));
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            processedLore.add(AltairKit.colorize(line
                            .replace("%next%", nextLevel == null ? "" : nextLevel.name()))
                    .replace("%cost%",String.valueOf(faction.getCore().getLevel().upgradeCost()))
            );
        }
        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);
        return new ItemBuilder(startingItem)
                .setName(name)
                .setLore(processedLore)
                .toItemStack();
    }


    private void loadFillerItem(){
        String[] typeTokens = config.getString("gui.item.filler.type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        String name = AltairKit.colorize(config.getString("gui.item.filler.name"));
        List<String> lore = AltairKit.colorize(config.getStringList("gui.item.filler.lore"));

        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);
        this.fillerItem = new ItemBuilder(startingItem)
                .setName(name)
                .setLore(lore)
                .toItemStack();

    }

    @Override
    public Inventory getInventory() {
        return this.gui;
    }
}
