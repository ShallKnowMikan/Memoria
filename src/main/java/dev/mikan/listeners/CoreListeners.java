package dev.mikan.listeners;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.ItemBuilder;
import dev.mikan.altairkit.utils.NBTUtils;
import dev.mikan.database.module.impl.CoreDatabase;
import dev.mikan.events.cores.CoreCreateEvent;
import dev.mikan.events.cores.CoreLevelUpEvent;
import dev.mikan.gui.cores.CoreGUI;
import dev.mikan.modules.core.Core;
import dev.mikan.modules.core.CoreConstructor;
import dev.mikan.modules.core.CoreModule;
import dev.mikan.modules.core.Level;
import dev.mikan.modules.faction.MFaction;
import dev.mikan.modules.faction.Role;
import dev.mikan.modules.faction.State;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CoreListeners implements Listener {

    private final CoreModule module;
    private final FileConfiguration lang;
    private final CoreDatabase database;

    public CoreListeners(CoreModule module) {
        this.module = module;
        this.lang = module.getPlugin().getLang();
        this.database = module.getDatabase();
    }


    /*
    * --------------------------------
    * Core place listening down there
    * --------------------------------
    * */
    // No-faction players cannot place cores
    @EventHandler public void onCorePlace(BlockPlaceEvent e){
        if (e.getItemInHand() == null || !NBTUtils.hasKey(e.getItemInHand(),CoreModule.CORE_ITEM_KEY)) return;
        e.setBuild(false);
        MFaction faction = MFaction.MFactions.getByPlayer(e.getPlayer());
        if (faction == null) {
            e.getPlayer().sendMessage(
                    AltairKit.colorize(lang.getString("cores.on_place.prohibition.while_in_no_faction"))
            );
            return;
        }
        Faction massive = Board.getInstance().getFactionAt(new FLocation(e.getBlock()));
        if (MFaction.MFactions.isDefault(massive) || !massive.getId().equals(String.valueOf(faction.getId()))){
            e.getPlayer().sendMessage(
                    AltairKit.colorize(lang.getString("cores.on_place.prohibition.invalid_location"))
            );
            return;
        }

        if (faction.hasCore()) {
            e.getPlayer().sendMessage(
                    AltairKit.colorize(lang.getString("cores.on_place.prohibition.core_already_placed"))
            );
            return;
        }

        int levelId = (int) NBTUtils.value(e.getItemInHand(),CoreModule.CORE_ITEM_KEY, Integer.class);
        Level level = Level.Levels.cache.get(levelId);
        Location loc = e.getBlock().getLocation();

        Bukkit.getScheduler().runTask(module.getPlugin().getBootstrap(), () -> {
            CoreConstructor constructor = new CoreConstructor(e.getBlock());
            module.info("Core constructor is valid: {}",constructor.isValid());
            if (!constructor.isValid()) {
                e.getPlayer().sendMessage(
                        AltairKit.colorize(lang.getString("cores.on_place.prohibition.invalid_location"))
                );
                return;
            }

            CoreCreateEvent event = new CoreCreateEvent(e.getPlayer(),level,loc,faction);
            event.run();
        });

    }

    @EventHandler public void onCoreBlockBreak(BlockBreakEvent e){

        if (e.getBlock() == null) return;
        MFaction faction = MFaction.MFactions.getByPlayer(e.getPlayer());

        if (faction == null || !faction.hasCore()) return;

        Core core = faction.getCore();
        if (core.getConstructor().getBlocks().contains(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler public void onCoreBlockBreak(EntityExplodeEvent e){
        MFaction faction = MFaction.MFactions.getByFaction(Board.getInstance().getFactionAt(new FLocation(e.getLocation())));
        if (faction == null || !faction.hasCore()) return;

        Core core = faction.getCore();

        record BlockData(Material material, byte data){}
        Map<Block, BlockData> blockSet = new ConcurrentHashMap<>();

        for (Block block : e.blockList()) {
            if (core.getConstructor().getBlocks().contains(block))
                blockSet.put(block,new BlockData(block.getType(),block.getData()));
        }

        Bukkit.getScheduler().runTaskLater(module.getPlugin().getBootstrap(),
                () ->
                    blockSet.forEach((block, blockData) -> {
                        block.setType(blockData.material);
                        block.setData(blockData.data);
                    }),2);
    }


    @EventHandler public void onRightClickCore(PlayerInteractEvent e){
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        Faction massive = Board.getInstance().getFactionAt(new FLocation(block));
        FPlayer player = FPlayers.getInstance().getByPlayer(e.getPlayer());

        // has no admin bypass
        // isn't in right territory, or it's not his core
        boolean cannotOpenGUI = !player.isAdminBypassing() &&
                (!player.isInOwnTerritory() || !player.getFactionId().equals(massive.getId()));
        if (MFaction.MFactions.isDefault(massive) || cannotOpenGUI) return;

        MFaction faction = MFaction.MFactions.getByFaction(massive);

        if (faction == null || !faction.hasCore()) return;

        boolean isCoreBlock = faction.getCore().getConstructor().getBlocks().contains(block);
        if (!isCoreBlock) return;

        // Prevents from opening this gui while under raid
        if (faction.getRole() == Role.DEFENDERS && faction.getState() == State.RAID) {
            player.sendMessage(AltairKit.colorize(lang.getString("cores.on_raid.prohibition.open_gui")));
            e.setCancelled(true);
            return;
        }

        String title = AltairKit.colorize(module.getConfig()
                .getString("gui.title")
                .replace("%faction%",massive.getTag()));
        int size = module.getConfig().getInt("gui.size");

        CoreGUI gui = new CoreGUI(title,size,faction);
        gui.show(e.getPlayer());
    }


    /*
    * Prevents player from unclaiming lands when it
    * involves faction's core
    * */
    @EventHandler public void onUnclaim(LandUnclaimEvent e){
        MFaction faction = MFaction.MFactions.getByFaction(e.getfPlayer().getFaction());
        if (faction == null || !faction.hasCore()) return;

        Core core = faction.getCore();
        if (core.getLocation().getChunk() != e.getLocation().getChunk()) return;

        e.setCancelled(true);
        e.getfPlayer().sendMessage(
                AltairKit.colorize(lang.getString("cores.on_unclaim.when_core_land"))
        );
    }

    @EventHandler public void onUnclaimAll(LandUnclaimAllEvent e){
        MFaction faction = MFaction.MFactions.getByFaction(e.getfPlayer().getFaction());
        if (faction == null || !faction.hasCore()) return;

        e.setCancelled(true);
        e.getfPlayer().sendMessage(
                AltairKit.colorize(lang.getString("cores.on_unclaim_all.on_unclaim_all"))
        );
    }



    /*
    * -----------------------------------------------
    * Core GUI management down there
    * -----------------------------------------------
    * */

    @EventHandler public void onUpgradeItemClick(InventoryClickEvent e){
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof CoreGUI)) return;

        ItemStack item = e.getCurrentItem();
        if (!NBTUtils.hasKey(item,CoreGUI.UPGRADE_ITEM_KEY)) return;

        MFaction faction = MFaction.MFactions.getById((int) NBTUtils.value(item,CoreGUI.UPGRADE_ITEM_KEY, Integer.class));
        if (!faction.hasCore()) return;

        CoreLevelUpEvent event = new CoreLevelUpEvent(faction.getCore(), (Player) e.getWhoClicked());
        boolean isMaxLevel = event.run();

        if (isMaxLevel) {
            CoreGUI.setUpgradeToMaxLevelItem(e.getClickedInventory(),faction);
        }


    }
    @EventHandler public void onRemoveItemClick(InventoryClickEvent e){
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof CoreGUI)) return;

        ItemStack item = e.getCurrentItem();
        if (!NBTUtils.hasKey(item,CoreGUI.REMOVE_ITEM_KEY)) return;

        MFaction faction = MFaction.MFactions.getById((int) NBTUtils.value(item,CoreGUI.REMOVE_ITEM_KEY, Integer.class));
        if (!faction.hasCore()) return;

        Player player = (Player) e.getWhoClicked();
        player.closeInventory();
        Inventory gui = player.getInventory();
        int emptySlots = (int) Arrays.stream(gui.getContents())
                .filter(current -> current == null || current.getType() == Material.AIR)
                .count();

        if (emptySlots == 0) {
            player.sendMessage(AltairKit.colorize(lang.getString("cores.on_remove.when_inventory_is_full")));
            return;
        }

        Core core = faction.getCore();
        faction.setCore(null);
        core.destruct(false);
        ItemBuilder builder = new ItemBuilder(module.getCoreBaseItem())
                .setName(module.getCoreBaseItem().getItemMeta().getDisplayName()
                        .replace("%core_level%",Level.Levels.defaultLevel.name()));
        ItemStack coreItem = NBTUtils.set(builder.toItemStack(),CoreModule.CORE_ITEM_KEY,core.getLevel());

        gui.addItem(coreItem);
        player.sendMessage(AltairKit.colorize(lang.getString("cores.on_remove.success")));

    }

    @EventHandler public void onCoreLevelUpWhileInRaid(CoreLevelUpEvent e){
        MFaction faction = e.getCore().getFaction();
        if (faction.getState() == State.RAID && faction.getRole() == Role.DEFENDERS){
            e.getPlayer()
                    .sendMessage(AltairKit.colorize(module
                            .getPlugin().getLang()
                            .getString("cores.on_level_up.prohibition.while_under_raid")));
            e.setCancelled(true);
        }
    }

}
