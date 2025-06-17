package dev.mikan.database.pools;

import com.zaxxer.hikari.HikariConfig;
import dev.mikan.database.ConnectionPool;
import dev.mikan.database.Vendor;


public class SQLiteConnectionPool extends ConnectionPool {

    public SQLiteConnectionPool() {
        super(Vendor.SQLITE);
    }

    @Override
    protected HikariConfig configurate() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:plugins/MemoriaCore/Databases/database.db");
        config.setMaximumPoolSize(5); // Pool più piccolo per SQLite
        config.setConnectionTimeout(30000); // 30 secondi
        return config;
    }
}
