package dev.mikan.modules.faction;

public final class MFaction {

    private final int id;
    private final String name;
    private final int victories;
    private final int defeats;
    private final int opponentId;

    private MFaction(int id, String name, int victories, int defeats, int opponentId) {
        this.id = id;
        this.name = name;
        this.victories = victories;
        this.defeats = defeats;
        this.opponentId = opponentId;
    }

    public final static class MFactions{

    }

}
