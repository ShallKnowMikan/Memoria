package dev.mikan.database.module.impl;

import com.google.gson.Gson;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.ModuleDatabase;
import dev.mikan.modules.core.Core;
import dev.mikan.modules.core.CoreModule;
import dev.mikan.modules.core.Level;
import dev.mikan.modules.faction.FactionModule;
import dev.mikan.modules.faction.MFaction;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class CoreDatabase extends ModuleDatabase implements Singleton {

    public CoreDatabase(SQLiteManager sql, Logger logger) {
        super(sql, logger);
    }

    @Override
    public void setup() {
        final String createCoreTable = """
                CREATE TABLE IF NOT EXISTS Cores(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    factionId INT NOT NULL,
                    level INT DEFAULT %default%,
                    location VARCHAR(1024) NOT NULL,
                    FOREIGN KEY(factionId) REFERENCES Factions(factionId)
                )
                """
                .replace("%default%",
                        String.valueOf(Level.Levels.defaultLevel.id()));

        this.sql.update(createCoreTable);
    }

    public void update(Core core){
        if (core == null) return;

        final String query = "UPDATE Cores SET level = ? WHERE id = ?";

        this.sql.updateAsync(query,core.getLevel().id(),core.getId()).whenComplete(
                (success,error) -> {
                    CoreModule.instance().info(
                            success ? "Level of {} successfully updated." :
                                    "Error while updating {}'s level.",
                            core.getId()
                    );
                }
        );
    }

    public void delete(Core core){
        final int id = core.getId();
        final String query = "DELETE FROM Cores WHERE id = ?";
        this.sql.updateAsync(query,id)
                .whenComplete((success,error) -> {
                    Core.Cores.destruct(id);
                    final String message = success ? "deleted successfully." : "error while deleting.";
                    FactionModule.instance().info("Core: {} -> {}",id,message);
                });


    }

    public CompletableFuture<Integer> insert(Level level, Location loc, MFaction faction){
        final int factionId = faction.getId();
        final String query = "INSERT INTO Cores(factionId,location,level) VALUES(?,?,?)";

        final String locString = new Gson().toJson(loc.serialize());

        return this.sql.updateAsyncWithId(query,factionId,locString,level.id());
    }

    public void loadCores(){
        final String query = "SELECT * FROM Cores";

        this.sql.queryAsync(query).whenComplete((result,error) -> {
            if (error != null) {
                logger.error("Error in loadCores()", error);
                return;
            }
            try {
                while (result.next()){
                    int id = result.getInt("id");
                    int factionId = result.getInt("factionId");
                    Level level = Level.Levels.cache.get(result.getInt("level"));
                    Location loc = Location.deserialize(new Gson().fromJson(
                            result.getString("location"),
                            HashMap.class
                    ));

                    Hologram hologram = DHAPI.getHologram(String.valueOf(id));
                    if (hologram != null) hologram.delete();
                    Core.Cores.instance(id,factionId,loc,level);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });


    }

    public static CoreDatabase instance(){
        return Singleton.getInstance(CoreDatabase.class,() -> null);
    }

}
