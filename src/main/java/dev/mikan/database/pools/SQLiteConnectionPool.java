package dev.mikan.database.pools;

import com.zaxxer.hikari.HikariConfig;
import dev.mikan.database.ConnectionPool;
import dev.mikan.database.Vendor;

import java.io.File;
import java.io.IOException;


public class SQLiteConnectionPool extends ConnectionPool {

    private final String dir = "plugins/MemoriaCore/databases/";
    private final String dbFile;

    public SQLiteConnectionPool(String dbFileName) {
        super(Vendor.SQLITE);
        dbFile = dir + dbFileName;
        prepareDatabaseFile();
    }

    private void prepareDatabaseFile() {
        File directory = new File(dir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + dir);
        }

        File db = new File(dbFile);
        if (!db.exists()) {
            try {
                if (!db.createNewFile()) {
                    throw new RuntimeException("Failed to create database file");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create SQLite database file", e);
            }
        }
    }

    @Override
    protected HikariConfig configurate() {
        HikariConfig config = new HikariConfig();
        config.setConnectionTestQuery("SELECT 1");
        config.setJdbcUrl("jdbc:sqlite:"+dbFile);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);
        return config;
    }



}
