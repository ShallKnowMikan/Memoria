package dev.mikan.database.module;


import dev.mikan.database.SQLiteManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@RequiredArgsConstructor
public abstract class ModuleDatabase {

    protected final SQLiteManager sql;
    protected final Logger logger;

    public abstract void setup();
}
