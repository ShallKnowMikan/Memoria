package dev.mikan.gui.factions;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class BombersGUI extends AltairGUI {

    public static final String HEAD_KEY = "uuid";
    public static final String NEXT_PAGE_KEY = "BOMBERS_NEXT_PAGE";
    public static final String PREVIOUS_PAGE_KEY = "BOMBERS_PREVIOUS_PAGE";

    private final FileConfiguration config;
    private final FactionModule module;
    private final Faction massive;
    private final int page;

    private ItemStack nextPageItem;
    private ItemStack previousPageItem;

    public BombersGUI(String title, int slots, Faction massive, int page) {
        super(title.replace("%faction%",massive.getTag()), slots);
        this.module = FactionModule.instance();
        this.config = module.getConfig();
        this.massive = massive;
        this.page = page;

        this.fillerItem = loadItem("filler");
        build(this.fillerItem,this,false);

        this.nextPageItem = loadItem("next_page");
        this.previousPageItem = loadItem("previous_page");

        byte nextPageItemPosition = (byte) config.getInt("gui.bombers.items.next_page.position");
        byte previousPageItemPosition = (byte) config.getInt("gui.bombers.items.previous_page.position");

        this.gui.setItem(nextPageItemPosition,nextPageItem);
        this.gui.setItem(previousPageItemPosition,previousPageItem);

        byte freeSpace = getFreeSpace();

        boolean isNextPageValid = PageUtils.isPageValid(massive.getOnlinePlayers().size(),page + 1,freeSpace);
        boolean isPreviousPageValid = PageUtils.isPageValid(massive.getOnlinePlayers().size(),page - 1,freeSpace);

        this.gui.setItem(nextPageItemPosition, NBTUtils.set(nextPageItem,NEXT_PAGE_KEY,isNextPageValid+":"+(page + 1)));
        this.gui.setItem(previousPageItemPosition, NBTUtils.set(previousPageItem,PREVIOUS_PAGE_KEY,isPreviousPageValid+":"+(page - 1)));

        loadMemberHeads(PageUtils.getLowerBound(page,freeSpace),freeSpace);
    }


    /*
     * Since in PageUtils from AltairKit we have a lower and an upper bound
     * The lower stands for the bound between the first and the current page
     * thi means that technically starting the loop from the lowerbound should start
     * placing the heads from the "chunk" needed and the j counter will stop it
     * when needed
     * */
    private void loadMemberHeads(int lowerBound,byte freeSlots){
        MFaction mFaction = MFaction.MFactions.getByFaction(massive);

        List<String> lore = module.getConfig().getStringList("gui.bombers.bomber.lore");
        String active = AltairKit.colorize(module.getConfig().getString("gui.bombers.bomber.type.active"));
        String inactive = AltairKit.colorize(module.getConfig().getString("gui.bombers.bomber.type.inactive"));




        for (int i = lowerBound, j = 0; i < massive.getOnlinePlayers().size(); i++,j++) {

            if (j == freeSlots) break;

            List<String> colorizedLore = new ArrayList<>();
            Player onlinePlayer = massive.getOnlinePlayers().get(i);

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

            this.gui.addItem(NBTUtils.set(head,HEAD_KEY,onlinePlayer.getUniqueId().toString()));
        }
    }


    @Override
    public Inventory getInventory() {
        return this.gui;
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

    private ItemStack loadItem(String node){
        String[] typeTokens = module.getConfig().getString("gui.bombers.items."+node+".type").split(":");
        int fillerItemId = Integer.parseInt(typeTokens[0]);
        byte data = Byte.parseByte(typeTokens.length > 1 ? typeTokens[1] : "0");

        String name = AltairKit.colorize(module.getConfig().getString("gui.bombers.items."+node+".name"));
        List<String> lore = module.getConfig().getStringList("gui.bombers.items."+node+".lore");
        List<String> colorizedLore = new ArrayList<>();
        lore.forEach(line -> colorizedLore.add(AltairKit.colorize(line)));


        ItemStack startingItem = new ItemStack(Material.getMaterial(fillerItemId),1,data);
        return new ItemBuilder(startingItem)
                .setName(name)
                .setLore(colorizedLore)
                .toItemStack();
    }
}
