package dev.mikan.gui;

import com.massivecraft.factions.Faction;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.api.gui.AltairGUI;
import dev.mikan.altairkit.api.gui.PageUtils;
import dev.mikan.altairkit.utils.ItemBuilder;
import dev.mikan.altairkit.utils.NBTUtils;
import dev.mikan.modules.faction.FactionModule;
import dev.mikan.modules.faction.MFaction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BombersSelectionGUI extends AltairGUI implements InventoryHolder {

    private final Faction faction;
    private final FactionModule module;
    private final int page;
    public static final String HEAD_KEY = "uuid";
    public static final String NEXT_PAGE_KEY = "BOMBERS_NEXT_PAGE";
    public static final String PREVIOUS_PAGE_KEY = "BOMBERS_PREVIOUS_PAGE";


    public BombersSelectionGUI(String title, int size, Faction faction, int page) {
        super(title.replace("%faction%",faction.getTag()), size);
        this.faction = faction;
        this.page = page;
        this.module = FactionModule.instance();

        loadFillerItem();
        this.build(this.fillerItem,this,false);

        /*
        * Inside those items nbt will be like:
        *   selectedPageIsValid:currentPage
        * */

        byte position = (byte) this.module.getConfig().getInt("gui.bombers.items.next_page.position");
        this.gui.setItem(position,NBTUtils.set(loadItem("next_page"),NEXT_PAGE_KEY,1));

        position = (byte) this.module.getConfig().getInt("gui.bombers.items.previous_page.position");
        this.gui.setItem(position,NBTUtils.set(loadItem("previous_page"),PREVIOUS_PAGE_KEY,1));

        byte freeSpace = getFreeSpace();
        int lowerBound = PageUtils.getLowerBound(page,freeSpace);
        loadMemberHeadsTest(lowerBound,freeSpace);
    }

    private ItemStack loadItem(String node){
        FileConfiguration config = module.getConfig();

        String[] typeTokens = config.getString("gui.bombers.items."+node+".type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        String name = AltairKit.colorize(config.getString("gui.bombers.items."+node+".name"));
        List<String> lore = config.getStringList("gui.bombers.items."+node+".lore");
        List<String> colorizedLore = new ArrayList<>();
        lore.forEach(line -> colorizedLore.add(AltairKit.colorize(line)));


        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);

        return new ItemBuilder(startingItem)
                .setName(name)
                .setLore(colorizedLore)
                .toItemStack();

    }


    private void loadFillerItem(){
        String[] typeTokens = module.getConfig().getString("gui.bombers.items.filler.type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        String name = AltairKit.colorize(module.getConfig().getString("gui.bombers.items.filler.name"));
        List<String> lore = module.getConfig().getStringList("gui.bombers.items.filler.lore");
        List<String> colorizedLore = new ArrayList<>();
        lore.forEach(line -> colorizedLore.add(AltairKit.colorize(line)));


        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);
        this.fillerItem = new ItemBuilder(startingItem)
                .setName(name)
                .setLore(colorizedLore)
                .toItemStack();
    }

    private byte getFreeSpace(){
        byte freeSpace = 0;
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null || gui.getItem(i).getType() == Material.AIR) {
                freeSpace++;
            }
        }
        return freeSpace;
    }

    /*
    * Since in PageUtils from AltairKit we have a lower and an upper bound
    * The lower stands for the bound between the first and the current page
    * thi means that technically starting the loop from the lowerbound should start
    * placing the heads from the "chunk" needed and the j counter will stop it
    * when needed
    * */
    private void loadMemberHeads(int lowerBound,byte freeSlots){
        MFaction mFaction = MFaction.MFactions.getByFaction(faction);

        List<String> lore = module.getConfig().getStringList("gui.bombers.bomber.lore");
        String active = AltairKit.colorize(module.getConfig().getString("gui.bombers.bomber.type.active"));
        String inactive = AltairKit.colorize(module.getConfig().getString("gui.bombers.bomber.type.inactive"));
        List<String> colorizedLore = new ArrayList<>();



        for (int i = lowerBound, j = 0; i < faction.getOnlinePlayers().size(); i++,j++) {

            if (j == freeSlots) break;

            Player onlinePlayer = faction.getOnlinePlayers().get(i);

            lore.forEach(line ->
                    colorizedLore.add(AltairKit.colorize(line
                            .replace(
                                    "%isActive%",
                                    mFaction.getBombers().contains(onlinePlayer.getUniqueId()) ? active : inactive
                            ))));

            ItemStack head = AltairKit.head(onlinePlayer);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setDisplayName(ChatColor.BLUE +onlinePlayer.getDisplayName());
            meta.setLore(colorizedLore);

            head.setItemMeta(meta);

            this.gui.addItem(head);
        }
    }

    private void loadMemberHeadsTest(int lowerBound,byte freeSlots){
        MFaction mFaction = MFaction.MFactions.getByFaction(faction);

        List<String> lore = module.getConfig().getStringList("gui.bombers.bomber.lore");
        String active = AltairKit.colorize(module.getConfig().getString("gui.bombers.bomber.type.active"));
        String inactive = AltairKit.colorize(module.getConfig().getString("gui.bombers.bomber.type.inactive"));
        List<String> colorizedLore = new ArrayList<>();



        for (int i = lowerBound, j = 0; i < 90; i++,j++) {

            if (j == freeSlots) break;


            lore.forEach(line ->
                    colorizedLore.add(AltairKit.colorize(line
                            .replace(
                                    "%isActive%",
                                    mFaction.getBombers().contains(UUID.randomUUID()) ? active : inactive
                            ))));

            ItemStack head = new ItemStack(Material.SKULL_ITEM,1, (short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA +"n"+i);
            meta.setLore(colorizedLore);

            head.setItemMeta(meta);

            this.gui.addItem(head);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.gui;
    }
}
