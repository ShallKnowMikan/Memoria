package dev.mikan.database.module.impl;


import com.massivecraft.factions.Faction;
import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.altairkit.utils.TimeUtils;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.ModuleDatabase;
import dev.mikan.modules.faction.FactionModule;
import dev.mikan.modules.faction.MFaction;
import dev.mikan.modules.faction.Role;
import dev.mikan.modules.faction.State;
import lombok.SneakyThrows;
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
public final class FactionDatabase extends ModuleDatabase implements Singleton {




    public FactionDatabase(SQLiteManager manager, Logger logger) {
        super(manager,logger);
    }

    @Override
    public void setup() {

        final String createServerStopTable = """
                CREATE TABLE IF NOT EXISTS ServerStopLog(
                    id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
                    datetime VARCHAR(24) DEFAULT NULL
                )
                """;

        final String createFactionTable = """
                CREATE TABLE IF NOT EXISTS Factions(
                    id INTEGER PRIMARY KEY,
                    state VARCHAR(32) DEFAULT %peace%,
                    role VARCHAR(32) DEFAULT %role%,
                    victories INT DEFAULT 0,
                    defeats INT DEFAULT 0,
                    nextState VARCHAR(24) DEFAULT NULL,
                    opponentId INT DEFAULT NULL,
                    FOREIGN KEY(opponentId) REFERENCES Factions(id)
                )
                """
                .replace("%peace%",State.PEACE.name())
                .replace("%role%",Role.NONE.name());

        final String createBombersTable = """
                CREATE TABLE IF NOT EXISTS Bombers(
                    UUID VARCHAR(48) PRIMARY KEY,
                    factionId INT NOT NULL,
                    FOREIGN KEY(factionId) REFERENCES Factions(id) ON DELETE CASCADE
                )
                """;



        this.sql.update(createServerStopTable);
        this.sql.update(createFactionTable);
        this.sql.update(createBombersTable);


    }

    @SneakyThrows
    public String getServerStopLog(){
        final String query = "SELECT datetime FROM ServerStopLog WHERE id = ?";
        try (CachedRowSet result = this.sql.query(query,1)){
            if (result.next()) return result.getString("datetime");
            else return "";
        }
    }

    /*
    * Checks if record exists, if not will create it
    * If record already exists it will update it with current
    * date time
    * */
    public void updateServerStopLog(){
        String query = "REPLACE INTO ServerStopLog (id, datetime) VALUES (?, ?)";
        this.sql.update(query, 1, TimeUtils.current());
    }

    public void insertBomber(UUID uuid,int factionId){
        final String query = "INSERT INTO Bombers (UUID,factionId) VALUES(?,?)";

        this.sql.updateAsync(query,uuid.toString(),factionId)
                .whenComplete((success,error) -> {
                    if (!success) FactionModule.instance().error("Error while inserting bomber!");
                });
    }

    public void removeBomber(UUID uuid,int factionId){
        final String query = "DELETE FROM Bombers WHERE UUID = ? AND factionId = ?";

        this.sql.updateAsync(query,uuid.toString(),factionId)
                .whenComplete((success,error) -> {
                    if (!success) FactionModule.instance().error("Error while deleting bomber!");
                });
    }


    public void insert(Faction faction){
        final int id = Integer.parseInt(faction.getId());

        final String query = "INSERT INTO Factions(id) VALUES(?)";

        this.sql.updateAsync(query,id)
                .whenComplete((success,error) -> {
                    final String message = success ? "added successfully." : "error while adding.";
                    FactionModule.instance().info("Faction: {} -> {}",id,message);
                    MFaction.MFactions.instance(id,Role.NONE,State.PEACE,0,0,"",-1,new HashSet<>());
                });
    }

    public void delete(Faction faction){
        final int id = Integer.parseInt(faction.getId());
        final String query = "DELETE FROM Factions WHERE id = ?";
        this.sql.updateAsync(query,id)
                .whenComplete((success,error) -> {
                    MFaction.MFactions.destruct(id);
                    final String message = success ? "deleted successfully." : "error while deleting.";
                    FactionModule.instance().info("Faction: {} -> {}",id,message);
                });


    }

    public void update(MFaction faction){
        String query = "UPDATE Factions SET state = ?,role = ?,victories = ?,defeats = ?,nextState = ?,opponentId = ? WHERE id = ?";
        this.sql.updateAsync(query,
                        faction.getState(),
                        faction.getRole(),
                        faction.getVictories(),
                        faction.getDefeats(),
                        faction.getNextState(),
                        faction.getOpponentId(),
                        faction.getId())
                .whenComplete((success,error) -> {
                    final String message = success ? "updated successfully." : "error while updating.";
                    FactionModule.instance().info("Faction: {} -> {}",faction.getId(),message);
                });
    }



    public void loadFactions(){
        final String query = "SELECT * FROM Factions";

        try (CachedRowSet result = this.sql.query(query)){
            while (result.next()){
                int id = result.getInt("id");
                int opponentId = result.getInt("opponentId") <= 0 ? -1 : result.getInt("opponentId");
                Role role = Role.valueOf(result.getString("role").toUpperCase());
                State state = State.valueOf(result.getString("state").toUpperCase());
                String nextState = result.getString("nextState");
                int victories = result.getInt("victories");
                int defeats = result.getInt("defeats");

                MFaction.MFactions.instance(id, role, state, victories, defeats, nextState,opponentId,loadBombers(id));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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




    public static FactionDatabase instance(){
        return Singleton.getInstance(FactionDatabase.class,() -> null);
    }
}
