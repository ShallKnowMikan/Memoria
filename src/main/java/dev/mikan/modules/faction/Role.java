package dev.mikan.modules.faction;

import java.util.List;

public final class Role {

    private final int id;
    private final String desc;

    private Role(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public final static class Roles{
        public final int ATTACK = 0;
        public final int DEFENSE = 1;
        public final int NONE = 2;

        public static final List<Role> all = List.of(
                new Role(0,"attack"),
                new Role(1,"defense"),
                new Role(2,"none")
        );
    }
}
