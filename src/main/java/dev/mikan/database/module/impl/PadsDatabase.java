package dev.mikan.database.module.impl;


import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.ModuleDatabase;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class PadsDatabase extends ModuleDatabase {

    /*
    * For now this class is just meant to handle teleport pads
    * Has 2 tables:
    *   teleportPadUsers & teleportPads
    *   in relation 1 to many
    * */

    private @Getter final Set<Object> teleportPads = new HashSet<>();

    public PadsDatabase(SQLiteManager sql, Logger logger) {
        super(sql, logger);
    }

    @Override
    public void setup() {
        sql.update("""
                CREATE TABLE IF NOT EXISTS teleportPads (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    world VARCHAR(128) NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    z INT NOT NULL,
                    destination VARCHAR(256),
                    current VARCHAR(256),
                    userUUID VARCHAR(48) NOT NULL
                )
                """);
    }

//    public CompletableFuture<Boolean> deletePad(TeleportPad pad) {
//        return sql.updateAsync("DELETE FROM teleportPads WHERE id=?", pad.getId());
//    }
//
//    public CompletableFuture<Boolean> updatePad(TeleportPad pad) {
//        final Block block = pad.getBlock();
//        return sql.updateAsync("UPDATE teleportPads SET world= ?, x=?, y=? , z=? , destination=?, current=? WHERE id=?",
//                block.getWorld().getName(),
//                block.getX(),
//                block.getY(),
//                block.getZ(),
//                pad.getDestination().getName(),
//                pad.getCurrent().getName(),
//                pad.getId()
//        );
//    }
//
//    public CompletableFuture<Boolean> insertPad(final Block block, final  UUID uuid) {
//        return sql.updateAsync("""
//                        INSERT INTO teleportPads (world, x, y,z ,destination,current,userUUID)
//                        VALUES (?, ?, ?, ?, ?, ?, ?)
//                        """,
//                block.getWorld().getName(),
//                block.getX(),
//                block.getY(),
//                block.getZ(),
//                WoolColors.NO_COLOR.nick(),
//                WoolColors.NO_COLOR.nick(),
//                uuid
//        );
//    }
//
//    public int fromPosition(Player player,Position position){
//        final String query = "SELECT id FROM teleportPads WHERE userUUID = ? AND current = ?";
//        try (CachedRowSet set = sql.query(query,
//                player.getUniqueId().toString(),
//                position.getName()
//                )) {
//            if (set.next()) return set.getInt("id");
//            return -1;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public Set<TeleportPad> loadPads(){
//        final String query = "SELECT * FROM teleportPads;";
//        final Set<TeleportPad> pads = new HashSet<>();
//        try(CachedRowSet set = sql.query(query)) {
//            while (set.next()){
//                final int id = set.getInt("id");
//                final String worldName = set.getString("world");
//                final int x = set.getInt("x");
//                final int y = set.getInt("y");
//                final int z = set.getInt("z");
//                final String destination = set.getString("destination");
//                final String current = set.getString("current");
//                final UUID playerId = UUID.fromString(set.getString("userUUID"));
//
//                final World world = Bukkit.getWorld(worldName);
//                if (world == null) continue;
//
//                pads.add(new TeleportPad(world.getBlockAt(x,y,z),id,new Position( WoolColors.fromNick(destination)),new Position( WoolColors.fromNick(current)),playerId));
//            }
//            return pads;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public int getId(Block block,UUID uuid){
//        final String query = "SELECT id FROM teleportPads WHERE world = ? AND x = ? AND y = ? AND z = ? AND userUUID = ?";
//        final Location location = block.getLocation();
//        try(CachedRowSet set = sql.query(query,
//                location.getWorld().getName(),
//                location.getX(),
//                location.getY(),
//                location.getZ(),
//                uuid.toString())) {
//            if (set.next())
//                return set.getInt("id");
//
//            return -1;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public boolean exists(Location location){
//        final String query = "SELECT * FROM teleportPads WHERE world = ? AND x = ? AND y = ? AND z = ?";
//        try(CachedRowSet set = sql.query(query,
//                location.getWorld().getName(),
//                location.getX(),
//                location.getY(),
//                location.getZ())) {
//            return set.next();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
}