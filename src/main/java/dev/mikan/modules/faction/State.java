package dev.mikan.modules.faction;


import java.util.List;

public final class State {

    private final int id;
    private final String desc;

    private State(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public final static class States{
        public final int PEACE = 0;
        public final int RAID = 1;
        public final int GRACE = 2;

        public static final List<State> all = List.of(
                new State(0,"peace"),
                new State(1,"raid"),
                new State(2,"grace")
        );
    }

}
