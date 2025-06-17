package dev.mikan.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public abstract class ConnectionPool {

    @Getter
    private final Vendor vendor;
    private HikariDataSource dataSource;

    public boolean setup() {
        final HikariConfig config = this.configurate();
        if (config == null) {
            return false;
        }

        config.setDriverClassName(this.getVendor().getDriverClassName());
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.setPoolName("MinigamesLobby");

        this.dataSource = new HikariDataSource(config);
        return true;
    }

    protected abstract HikariConfig configurate();

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}