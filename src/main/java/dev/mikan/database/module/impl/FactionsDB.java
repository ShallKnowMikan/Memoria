package dev.mikan.database.module.impl;


import dev.mikan.altairkit.utils.Singleton;
import dev.mikan.database.SQLiteManager;
import dev.mikan.database.module.ModuleDatabase;
import org.slf4j.Logger;

public final class FactionsDB extends ModuleDatabase implements Singleton {

    private final SQLiteManager manager;
    private final Logger logger;

    public FactionsDB(SQLiteManager manager, Logger logger) {
        super(manager,logger);

        this.manager = manager;
        this.logger = logger;
    }

    @Override
    public void setup() {
        final String createRoleTable = """
                CREATE TABLE IF NOT EXISTS Roles(
                    id INT NOT NULL,
                    `desc` VARCHAR(128) NOT NULL,
                    PRIMARY KEY(id,`desc`)
                )
                """;
        final String createStatesTable = """
                CREATE TABLE IF NOT EXISTS States(
                    id INT NOT NULL,
                    `desc` VARCHAR(128) NOT NULL,
                    PRIMARY KEY(id,`desc`)
                )
                """;
        final String createFactionTable = """
                CREATE TABLE IF NOT EXISTS Factions(
                    id INT PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(32) NOT NULL,
                    victories INT DEFAULT 0,
                    defeats INT DEFAULT 0,
                    state INT NOT NULL,
                    role INT NOT NULL,
                    opponentId INT NOT NULL.
                    FOREIGN KEY(state) REFERENCES States(id),
                    FOREIGN KEY(role) REFERENCES Roles(id),
                    FOREIGN KEY(opponentId) REFERENCES Factions(id)
                )
                """;
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


        this.sql.update(createRoleTable);
        this.sql.update(createStatesTable);
        this.sql.update(createFactionTable);
        this.sql.update(createBombersTable);
        this.sql.update(createCoreTable);


    }

}
