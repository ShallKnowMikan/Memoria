package dev.mikan.database.module.impl;

import com.google.gson.Gson;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.ModuleDatabase;
import dev.mikan.modules.core.Core;
import dev.mikan.modules.core.CoreModule;
import dev.mikan.modules.core.Level;
import dev.mikan.modules.faction.FactionModule;
import org.bukkit.Location;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;

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

    public void insert(Core core){
        final int id = core.getId();
        final int factionId = core.getFactionId();
        final String query = "INSERT INTO Factions(id,factionId,location) VALUES(?,?,?)";

        final String loc = new Gson().toJson(core.getLocation().serialize());

        this.sql.updateAsync(query,id,factionId,loc)
                .whenComplete((success,error) -> {
                    final String message = success ? "added successfully." : "error while adding.";
                    CoreModule.instance().info("Core: {} -> {}",id,message);
                });
    }

    public void loadCores(){
        final String query = "SELECT * FROM Cores";

        this.sql.queryAsync(query).whenComplete((result,error) -> {
            try {
                while (result.next()){
                    int id = result.getInt("id");
                    int factionId = result.getInt("factionId");
                    Level level = Level.Levels.cache.get(result.getInt("level"));
                    Location loc = Location.deserialize(new Gson().fromJson(
                            result.getString("location"),
                            HashMap.class
                    ));

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
