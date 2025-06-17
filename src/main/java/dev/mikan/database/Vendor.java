package dev.mikan.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public enum Vendor {

    MARIADB("mariadb", "org.mariadb.jdbc.Driver"),
    SQLITE("sqlite", "org.sqlite.JDBC");

    private final String protocol;
    private final String driverClassName;

    public static Vendor getVendor(@Nullable final String name) {
        if (name == null) return null;

        try {
            return Vendor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
