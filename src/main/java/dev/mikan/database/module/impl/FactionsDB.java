package dev.mikan.database.module.impl;


import com.massivecraft.factions.Faction;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.ModuleDatabase;
import dev.mikan.modules.faction.FactionModule;
import dev.mikan.modules.faction.MFaction;
import dev.mikan.modules.faction.Role;
import dev.mikan.modules.faction.State;
import org.slf4j.Logger;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
* Bombers and factions are 2 tables, in order to get bombers for each
* I'm using an inner join from factions while loading each
* */
public final class FactionsDB extends ModuleDatabase implements Singleton {

 private final SQLiteManager manager; private final Logger logger;


    public FactionsDB(SQLiteManager manager, Logger logger) {
        super(manager,logger);

        this.manager = manager;
        this.logger = logger;
    }

    @Override
    public void setup() {
        final String createFactionTable = """
                CREATE TABLE IF NOT EXISTS Factions(
                    id INTEGER PRIMARY KEY,
                    state VARCHAR(32) DEFAULT %peace%,
                    role VARCHAR(32) DEFAULT %role%,
                    victories INT DEFAULT 0,
                    defeats INT DEFAULT 0,
                    opponentId INT DEFAULT NULL,
                    FOREIGN KEY(opponentId) REFERENCES Factions(id)
                )
                """
                .replace("%peace%",State.PEACE.name())
                .replace("%role%",Role.NONE.name());

        final String createBombersTable = """
                CREATE TABLE IF NOT EXISTS Bombers(
                    UUID VARCHAR(48) PRIMARY KEY,
                    name CHAR(16) NOT NULL,
                    factionId INT NOT NULL,
                    FOREIGN KEY(factionId) REFERENCES Factions(id)
                )
                """;
        final String createCoreTable = """
                CREATE TABLE IF NOT EXISTS Cores(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    factionId INT DEFAULT NULL,
                    level INT DEFAULT 0,
                    location VARCHAR(1024) NOT NULL,
                    FOREIGN KEY(factionId) REFERENCES Factions(factionId)
                )
                """;


        this.sql.update(createFactionTable);
        this.sql.update(createBombersTable);
        this.sql.update(createCoreTable);


    }

    public void insert(Faction faction){
        final int id = Integer.parseInt(faction.getId());

        final String query = "INSERT INTO Factions(id) VALUES(?)";

        this.sql.updateAsync(query,id)
                .whenComplete((success,error) -> {
                    final String message = success ? "added successfully." : "error while adding.";
                    FactionModule.instance().info("Faction: {} -> {}",id,message);
                });
    }

    public void delete(Faction faction){
        final int id = Integer.parseInt(faction.getId());
        final String query = "DELETE FROM Factions WHERE id = ?";
        this.sql.updateAsync(query,id)
                .whenComplete((success,error) -> {
                    final String message = success ? "deleted successfully." : "error while deleting.";
                    FactionModule.instance().info("Faction: {} -> {}",id,message);
                });


    }

    public Set<MFaction> loadFactions(){
        final String query = "SELECT * FROM Factions";

        Set<MFaction> factions = new HashSet<>();


        try (CachedRowSet result = this.sql.query(query)){
            while (result.next()){
                int id = result.getInt("id");
                int opponentId = result.getInt("opponentId");
                Role role = Role.valueOf(result.getString("role").toUpperCase());
                State state = State.valueOf(result.getString("state").toUpperCase());

                int victories = result.getInt("victories");
                int defeats = result.getInt("defeats");
                factions.add(MFaction.MFactions.instance(id, role, state, victories, defeats, opponentId,loadBombers(id)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return factions;
    }

    private Set<UUID> loadBombers(int id){
        final Set<UUID> bomberUUIDs = new HashSet<>();
        final String query = "SELECT UUID FROM Factions f JOIN Bombers b ON f.id = b.factionId AND f.id = ?";
        try (CachedRowSet results = this.sql.query(query,id)) {
            while (results.next()){
                bomberUUIDs.add(UUID.fromString(results.getString("UUID")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return bomberUUIDs;
    }


    public static FactionsDB instance(){
        return Singleton.getInstance(FactionsDB.class,() -> null);
    }
}
