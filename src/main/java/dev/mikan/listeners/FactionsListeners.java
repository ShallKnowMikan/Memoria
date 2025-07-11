package dev.mikan.listeners;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.FactionAutoDisbandEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import com.shampaggon.crackshot.events.WeaponExplodeEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import com.shampaggon.crackshot.events.WeaponShootEvent;
import dev.mikan.altairkit.AltairKit;
import dev.mikan.altairkit.utils.NBTUtils;
import dev.mikan.altairkit.utils.NmsUtils;
import dev.mikan.altairkit.utils.TimeUtils;
import dev.mikan.commands.FactionCommands;
import dev.mikan.database.module.impl.FactionDatabase;
import dev.mikan.events.factions.ChunkJoinEvent;
import dev.mikan.gui.factions.BombersGUI;
import dev.mikan.gui.factions.RaidProposalGUI;
import dev.mikan.modules.faction.*;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

@Slf4j
public class FactionsListeners implements Listener {

    private final FactionDatabase database;
    private final FactionModule module;
    private final FileConfiguration factionConfig;

    // Player -> weapon title
    private final Map<Player,String> griefShootingMap = new HashMap<>();
    // Player -> weapon title
    private final Map<Player,String> sandPlacerMap = new HashMap<>();

    public FactionsListeners(FactionDatabase database) {
        this.database = database;
        this.module = FactionModule.instance();
        this.factionConfig = module.getConfig();
    }

    // Inserts the just created faction into the database
    @EventHandler public void onCreate(FactionCreateEvent e){
        database.insert(e.getFaction());
    }
    // Deletes the just created faction from the database
    @EventHandler public void onDisband(FactionDisbandEvent e){
        database.delete(e.getFaction());
    }

    // Deletes the just created faction from the database
    @EventHandler public void onDisband(FactionAutoDisbandEvent e){
        database.delete(e.getFaction());
    }

    /*
     * --------------------------------------------------
     * F commands adapters stuff down here
     * --------------------------------------------------
     * */

    @EventHandler public void onFCommand(PlayerCommandPreprocessEvent e){
        if (!e.getMessage().startsWith("/f")
                || !(e.getMessage().split(" ").length >= 2
                    && FactionCommands.F_COMMANDS.contains(e.getMessage().replaceFirst("/f ", "").toLowerCase()))) return;

        String command = e.getMessage().replaceFirst("f", FactionCommands.F_CMD_FAKE_ROOT).substring(1);
        e.setCancelled(true);
        e.getPlayer().performCommand(command);
    }

    /*
    * --------------------------------------------------
    * Claim change stuff down here
    * --------------------------------------------------
    * */

    /*
    * Join others claim while in raid -> tp back
    * Join others claim while in grace -> tp back
    *
    * Join others claim while they are in raid -> tp back
    * Join others claim while they are in grace -> tp back
    *
    * Join others claim while in no faction AND faction to is not wilderness -> tp back
    *
    * TODO: check if not in curfew before showing raid GUI
    * */
    @EventHandler public void onClaimJoin(ChunkJoinEvent e){

        if (e.getFPlayer().isAdminBypassing()) return;
        // Join others claim while in no faction AND faction to is not wilderness -> tp back
        if (e.getFPlayer().getFaction().isWilderness() && (!e.getFactionTo().isWilderness()) && !e.getFactionTo().isSafeZone() && !e.getFactionTo().isWarZone()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(AltairKit.colorize(module.getPlugin().getLang().getString("factions.on_player_enter_others_claim.on_own_faction_is_wilderness")));
            return;
        }

        // If joining a different land from the one saved in cache while in recognition
        if (module.getRecognitionCache().containsKey(e.getPlayer())){
            RecognitionCache cache = module.getRecognitionCache().get(e.getPlayer());
            if (!e.getFactionTo().getId().equals(cache.factionIn().getId())){
                e.getPlayer().setGameMode(cache.lastGamemode());
                e.getPlayer().teleport(cache.location());
                Bukkit.getScheduler().cancelTask(cache.taskId());
                module.getRecognitionCache().remove(e.getPlayer());
            }
        }


        MFaction playersFaction = MFaction.MFactions.getByPlayer(e.getFPlayer());

        if (playersFaction == null){
            e.setCancelled(true);
            e.getPlayer().sendMessage(AltairKit.colorize(module.getPlugin().getLang().getString("factions.on_player_enter_others_claim.on_own_faction_is_wilderness")));
            return;
        }

        MFaction factionTo = MFaction.MFactions.getByFaction(e.getFactionTo());
        // If factionto is wilderness safezone or warzone OR is own faction -> return
        if (factionTo == null || factionTo.getId() == playersFaction.getId()) return;

        if (factionTo.getState() == State.GRACE) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(AltairKit.colorize(module.getPlugin().getLang().getString("factions.on_player_enter_others_claim.on_grace_faction")).replace("%time-left%", TimeUtils.formatDatetime(factionTo.getNextState(),true)));
            return;
        }
        if (playersFaction.getState() != State.PEACE && !MFaction.MFactions.isDefault(e.getFactionTo())) {
            if (playersFaction.getState() == State.RAID && playersFaction.getRole() == Role.ATTACKERS
                    && factionTo.getState() == State.RAID && factionTo.getRole() == Role.DEFENDERS) return;
            e.setCancelled(true);
            e.getPlayer().sendMessage(AltairKit.colorize(module.getPlugin().getLang().getString("factions.on_player_enter_others_claim.on_own_faction_in_raid_or_grace").replace("%state%",playersFaction.getState().name().toLowerCase())));
            return;
        }





        if (factionTo.getState() == State.RAID && factionTo.getOpponentId() != playersFaction.getId()){
            e.setCancelled(true);
            e.getPlayer().sendMessage(AltairKit.colorize(module.getPlugin().getLang().getString("factions.on_player_enter_others_claim.on_raid_faction")));
            return;
        }

        if (factionTo.getState() == State.PEACE) {
            e.setCancelled(true);

            String title = factionConfig.getString("gui.raid_proposal.title")
                    .replace("%faction%",e.getFactionTo().getTag());
            int size = factionConfig.getInt("gui.raid_proposal.size");
            RaidProposalGUI gui = new RaidProposalGUI(title,size,module, playersFaction.getId(), factionTo.getId());

            gui.show(e.getPlayer());
            return;
        }



    }




    @EventHandler public void onLandChange(PlayerMoveEvent e){
        boolean changedBlock = !(e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ());

        boolean changedChunk = !e.getFrom().getChunk().toString().equals(e.getTo().getChunk().toString());



        if (!changedBlock || !changedChunk) {
            return;
        }

        ChunkJoinEvent event = new ChunkJoinEvent(
                Board.getInstance().getFactionAt(new FLocation(e.getTo())),
                Board.getInstance().getFactionAt(new FLocation(e.getFrom())),
                e.getPlayer(),
                e.getFrom(),
                e.getTo()
        );
        Bukkit.getPluginManager().callEvent(event);
    }





    /*
    * -------------------
    *   GUI MANAGERS
    * -------------------
    * */


    @EventHandler public void onRaidProposalGUIClick(InventoryClickEvent e){
        if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof RaidProposalGUI)) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR || !NBTUtils.hasKey(e.getCurrentItem(),RaidProposalGUI.ITEMS_TAG)) return;

        // confirm item clicked
        // else deny item was clicked

        // It means RaidProposalGUI has never been initialized yet.
        if (RaidProposalGUI.confirmItem == null) return;

        // Start raid then
        if (e.getCurrentItem().toString()
                .equals(RaidProposalGUI.confirmItem.toString())){

            e.getWhoClicked().closeInventory();

            int attackingFactionID = Integer.parseInt(NBTUtils.value(e.getCurrentItem(), RaidProposalGUI.ITEMS_TAG, String.class).toString().split(" ")[0].split(":")[1]);
            int defendingFactionID = Integer.parseInt(NBTUtils.value(e.getCurrentItem(), RaidProposalGUI.ITEMS_TAG, String.class).toString().split(" ")[1].split(":")[1]);

            MFaction attackingFaction = MFaction.MFactions.getCache().get(attackingFactionID);
            if (attackingFaction.getBombers().isEmpty()){
                e.getWhoClicked().sendMessage(AltairKit.colorize(
                        module.getPlugin().getLang().getString("factions.on_raid_attempt_without_bombers_prohibition")
                ));
                return;
            }
            MFaction defendingFaction = MFaction.MFactions.getCache().get(defendingFactionID);

            MFaction.MFactions.startRaid(attackingFaction,defendingFaction);

        } else if (e.getCurrentItem().toString()
                .equals(RaidProposalGUI.recognitionItem.toString())) {
            e.getWhoClicked().closeInventory();
            // Recognition stuff

            Player player = (Player) e.getWhoClicked();
            int defendingFactionID = Integer.parseInt(NBTUtils.value(e.getCurrentItem(), RaidProposalGUI.ITEMS_TAG, String.class).toString().split(" ")[1].split(":")[1]);

            // 3600 = 3 minutes in ticks
            int taskId = Bukkit.getScheduler().runTaskLater(module.getPlugin().getBootstrap(),() -> {
                RecognitionCache cache = module.getRecognitionCache().remove(player);
                player.setGameMode(cache.lastGamemode());
                player.teleport(cache.location());
            },3600).getTaskId();

            module.getRecognitionCache().put(player,new RecognitionCache(taskId, Factions.getInstance().getFactionById(String.valueOf(defendingFactionID)), player.getGameMode(),player.getLocation()));
            player.setGameMode(GameMode.SPECTATOR);

            Location ahead = player.getLocation().clone().add(player.getLocation().getDirection().normalize().multiply(7));
            player.teleport(ahead);

            NmsUtils.sendActionBar(player, AltairKit.colorize("&9Recognition started."));
        } else if (e.getCurrentItem().toString()
                .equals(RaidProposalGUI.denyItem.toString())) {
            e.getWhoClicked().closeInventory();
        }
    }

    @EventHandler public void onBombersGUIClick(InventoryClickEvent e){
        if (e.getCurrentItem() == null) return;
        if (e.getClickedInventory() == null || e.getCurrentItem().getType() == Material.AIR) return;
        if (!(e.getClickedInventory().getHolder() instanceof BombersGUI)) return;

        Player player = (Player) e.getWhoClicked();
        MFaction faction = MFaction.MFactions.getByPlayer(player);

        if (!NBTUtils.hasKey(e.getCurrentItem(), BombersGUI.HEAD_KEY)) {
            if (NBTUtils.hasKey(e.getCurrentItem(), BombersGUI.NEXT_PAGE_KEY)) {
                String[] tokens = NBTUtils.value(e.getCurrentItem(), BombersGUI.NEXT_PAGE_KEY,String.class).toString().split(":");
                int nextPage = Integer.parseInt(tokens[1]);
                boolean isValid = Boolean.parseBoolean(tokens[0]);

                if (isValid) {
                    BombersGUI gui = new BombersGUI(e.getInventory().getTitle(),
                            e.getInventory().getSize(),
                            Factions.getInstance().getFactionById(String.valueOf(faction.getId())),
                            nextPage);

                    player.closeInventory();
                    gui.show(player);
                }

            }
            else if (NBTUtils.hasKey(e.getCurrentItem(), BombersGUI.PREVIOUS_PAGE_KEY)) {
                String[] tokens = NBTUtils.value(e.getCurrentItem(), BombersGUI.PREVIOUS_PAGE_KEY,String.class).toString().split(":");
                int previousPage = Integer.parseInt(tokens[1]);
                boolean isValid = Boolean.parseBoolean(tokens[0]);

                if (isValid) {
                    BombersGUI gui = new BombersGUI(e.getInventory().getTitle(),
                            e.getInventory().getSize(),
                            Factions.getInstance().getFactionById(String.valueOf(faction.getId())),
                            previousPage);

                    player.closeInventory();
                    gui.show(player);
                }
            }
            return;
        }
        else if (player == null || faction == null) return;

        ItemStack head = e.getCurrentItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        List<String> lore = new ArrayList<>();
        String active = AltairKit.colorize(module.getConfig().getString("gui.bombers.bomber.type.active"));
        String inactive = AltairKit.colorize(module.getConfig().getString("gui.bombers.bomber.type.inactive"));

        UUID clickedUUID =UUID.fromString((String) NBTUtils.value(head,BombersGUI.HEAD_KEY, String.class));
        boolean wasPresent = faction.getBombers().contains(clickedUUID);
        for (String line : meta.getLore()) {
            if (wasPresent){
                lore.add(line.replace(active,inactive));
                faction.getBombers().remove(clickedUUID);
                database.removeBomber(clickedUUID, faction.getId());
            } else {
                lore.add(line.replace(inactive,active));
                faction.getBombers().add(clickedUUID);
                database.insertBomber(clickedUUID,faction.getId());
            }

        }

        meta.setLore(lore);
        head.setItemMeta(meta);

    }

    /*
    ---------------------
    * Crackshot listeners
    ---------------------
    * */

    // CURFEW
    @EventHandler public void onShootCheckCurfew(WeaponPreShootEvent e){
        if (module.getConfig().getStringList("grief_weapons").contains(e.getWeaponTitle())
                && MFaction.MFactions.isCurfewEnabled()) {
            e.setCancelled(true);
            String msg = AltairKit.colorize(module.getPlugin().getLang().getString("factions.on_curfew_shoot_prohibition"));
            e.getPlayer().sendMessage(msg);
        }
    }

    // Prevents normal player from shooting with grief weapon wile not in default factions or own and not selected as bomber of own faction
    @EventHandler public void onNotABomber(WeaponPreShootEvent e){
        FPlayer player = FPlayers.getInstance().getByPlayer(e.getPlayer());
        // Is grief weapon
        if (module.getConfig().getStringList("grief_weapons").contains(e.getWeaponTitle())
                // is not default faction
                && !MFaction.MFactions.isDefault(player.getFaction())
                // Is bomber
                && !MFaction.MFactions.getByPlayer(e.getPlayer()).getBombers().contains(e.getPlayer().getUniqueId())
                // not in wilderness
                && player.isInOthersTerritory()) {
            e.setCancelled(true);
            String msg = AltairKit.colorize(module.getPlugin().getLang().getString("factions.on_shoot_prohibition"));
            e.getPlayer().sendMessage(msg);
        }
    }

    // Prevents players from use grief weapons in own territory
    @EventHandler public void onOwnTerritoryGriefWeapon(WeaponPreShootEvent e){
        FPlayer player = FPlayers.getInstance().getByPlayer(e.getPlayer());
        // Is grief weapon
        if (module.getConfig().getStringList("grief_weapons").contains(e.getWeaponTitle())
            && player.isInOwnTerritory() && !MFaction.MFactions.isDefault(player.getFaction())){

            e.setCancelled(true);
            String msg = AltairKit.colorize(module.getPlugin().getLang().getString("factions.on_shoot_prohibition"));
            e.getPlayer().sendMessage(msg);
        }
    }

    /*
    * Prevents shooters to interfere in a raid
    *
    * blocks the damage when victim is in a raid state
    * and in one of the 2 involved factions land
    * */
    @EventHandler public void onRaidInterfere(WeaponDamageEntityEvent e){
        if (!(e.getVictim() instanceof Player victim)) return;

        MFaction shootersFaction = MFaction.MFactions.getByPlayer(e.getPlayer());

        MFaction victimsFaction = MFaction.MFactions.getByPlayer(victim);
        if (victimsFaction == null || victimsFaction.getState() != State.RAID) return;
        if (shootersFaction == null || shootersFaction.getId() == victimsFaction.getOpponentId()) return;

        MFaction factionAt = MFaction.MFactions.getByFaction(Board.getInstance().getFactionAt(new FLocation(victim.getLocation())));

        // If is in own claim or in opponent's
        if (factionAt.getId() == victimsFaction.getId() || factionAt.getId() == victimsFaction.getOpponentId()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(
                    AltairKit.colorize(
                            module.getPlugin().getLang().getString("factions.on_raid_interfere_prohibition")
                    )
            );
        }

    }

    /*
    * SAND AND SLAB BUSTING DOWN HERE
    *
    * need to combo with EntityExplodeEvent
    * jesus christ I hate crackshot
    * */

    @EventHandler public void onSandAndSlabBust(WeaponExplodeEvent e){
        if (griefShootingMap.containsKey(e.getPlayer()) && griefShootingMap.get(e.getPlayer()).equals(e.getWeaponTitle())){

            // Removes sand blocks in rage starting from the source
            final int radius = 1;
            Location loc = e.getLocation();
            Block closest = this.findClosest(loc, Material.SAND,1);
            if (closest != null && closest.getType() == Material.SAND) {
                for(int y = loc.getBlockY() - radius; y <= loc.getBlockY() + radius; ++y) {
                    Block block = loc.getWorld().getBlockAt(closest.getX(), y, closest.getZ());
                    if (block.getType() == Material.SAND) {
                        block.setType(Material.AIR);
                    }
                }
            }

            Set<Material> slabs = Set.of(
                    Material.WOOD_STEP,
                    Material.STEP, // Includes all slab variants
                    Material.STONE_SLAB2
            );

            // Removes slab blocks in rage starting from the source
            for (Material slab : slabs) {
                closest = this.findClosest(loc, slab,1);
                if (closest != null && closest.getType() == slab) {
                    for(int y = loc.getBlockY() - radius; y <= loc.getBlockY() + radius; ++y) {
                        Block block = loc.getWorld().getBlockAt(closest.getX(), y, closest.getZ());
                        if (block.getType() == slab) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }

            griefShootingMap.remove(e.getPlayer());
        }
    }


    @EventHandler public void onSandPlacerExplode(WeaponExplodeEvent e){
        if (sandPlacerMap.containsKey(e.getPlayer()) && sandPlacerMap.get(e.getPlayer()).equals(e.getWeaponTitle())
            && e.getLocation().getBlockY() < 255){
            int y = e.getLocation().getBlockY();
            int x = e.getLocation().getBlockX();
            int z = e.getLocation().getBlockZ();

            World world = e.getLocation().getWorld();

            do {

                Block block = world.getBlockAt(x,y++,z);
                if (block.getType() == Material.AIR
                        || block.getType() == Material.WATER
                        || block.getType() == Material.STATIONARY_WATER
                        || block.getType() == Material.STATIONARY_LAVA
                        || block.getType() == Material.LAVA)
                    block.setType(Material.SAND);
                else break;
            }while (y < 255);
            sandPlacerMap.remove(e.getPlayer());
        }
    }

    @EventHandler public void onGriefWeaponShoot(WeaponShootEvent e){
        List<String> griefWeapon = module.getConfig().getStringList("grief_weapons");
        if (griefWeapon.contains(e.getWeaponTitle())) {
            griefShootingMap.put(e.getPlayer(),e.getWeaponTitle());
        }
        String sandPlacerWeapon = module.getConfig().getString("sand_placer_weapon");
        if (e.getWeaponTitle().equals(sandPlacerWeapon)){
            sandPlacerMap.put(e.getPlayer(),sandPlacerWeapon);
        }

    }


    public final Block findClosest(Location source, Material type,int radius) {
        Block nearestSand = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = source.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    if (block.getType() == type) {
                        double distance = source.distanceSquared(loc);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestSand = block;
                        }
                    }
                }
            }
        }

        return nearestSand;
    }
}
