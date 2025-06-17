package dev.mikan.database;


import dev.mikan.database.adapters.ObjectAdapter;
import dev.mikan.database.adapters.impl.InventoryAdapter;
import dev.mikan.database.pools.SQLiteConnectionPool;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLiteManager {

    @Getter
    private final Logger logger;

    private final Map<Class<?>, ObjectAdapter<?>> adapters;
    private ConnectionPool pool;
    private ExecutorService executor;

    public SQLiteManager(Logger logger) {
        this.logger = logger;
        this.adapters = new HashMap<>();
        this.adapters.put(ItemStack[].class, new InventoryAdapter());
    }

    public void init() {
        ConnectionPool newPool = new SQLiteConnectionPool();
        if (newPool.setup()) {
            this.shutdown();
            this.pool = newPool;
            this.executor = Executors.newFixedThreadPool(4); // Thread pool fisso per SQLite
        } else {
            logger.error("Failed to initialize SQLite connection pool");
        }
    }

    public void shutdown() {
        if (pool != null) {
            pool.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    public CompletableFuture<CachedRowSet> queryAsync(String query, Object... objects) {
        return CompletableFuture.supplyAsync(() -> query(query, objects), executor);
    }

    public CompletableFuture<Boolean> updateAsync(String query, Object... objects) {
        return CompletableFuture.supplyAsync(() -> update(query, objects), executor);
    }

    public CachedRowSet query(String query, Object... objects) {
        try (Connection connection = pool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            bindParameters(preparedStatement, objects);

            ResultSet resultSet = preparedStatement.executeQuery();
            CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
            cachedRowSet.populate(resultSet);

            return cachedRowSet;
        } catch (SQLException e) {
            logger.error("Failed to execute query.", e);
            return null;
        }
    }

    public boolean update(String query, Object... objects) {
        try (Connection connection = pool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            bindParameters(preparedStatement, objects);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to execute update.", e);
            return false;
        }
    }

    private void bindParameters(PreparedStatement stmt, Object... objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            populateStatement(stmt, i + 1, objects[i]);
        }
    }

    public <T> ObjectAdapter<T> getObjectAdapter(Class<T> clazz) {
        return (ObjectAdapter<T>) adapters.get(clazz);
    }

    private void populateStatement(PreparedStatement stmt, int index, Object object) throws SQLException {
        if (object == null) {
            stmt.setNull(index, Types.NULL);
            return;
        }

        switch (object) {
            case String s -> stmt.setString(index, s);
            case Integer i -> stmt.setInt(index, i);
            case Double d -> stmt.setDouble(index, d);
            case Long l -> stmt.setLong(index, l);
            case Float f -> stmt.setFloat(index, f);
            case Short s -> stmt.setShort(index, s);
            case Byte b -> stmt.setByte(index, b);
            case Boolean b -> stmt.setBoolean(index, b);
            case Character c -> stmt.setString(index, c.toString());
            case UUID uuid -> stmt.setString(index, uuid.toString());
            default -> populateStatementWithObject(stmt, index, object);
        }
    }

    private <T> void populateStatementWithObject(PreparedStatement stmt, int index, T obj) throws SQLException {
        ObjectAdapter<T> adapter = this.getObjectAdapter((Class<T>) obj.getClass());
        if (adapter != null) {
            stmt.setString(index, adapter.serialize(obj));
        } else {
            stmt.setObject(index, obj);
        }
    }
}