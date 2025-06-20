package dev.mikan.modules.faction;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class MFaction {

    private final int id;
    private final int victories;
    private final int defeats;
    private final int opponentId;
    private final Role role;
    private final State state;
    private final Set<UUID> bombers;

    private MFaction(int id, Role role, State state, int victories, int defeats, int opponentId, Set<UUID> bombers) {
        this.id = id;
        this.victories = victories;
        this.defeats = defeats;
        this.opponentId = opponentId;
        this.role = role;
        this.state = state;
        this.bombers = bombers;
    }

    /*
    * TODO: check if factions like warzone and wildernes will be stored
    *  into the database one created
    * */
    public final static class MFactions{
        private @Getter static final Map<Integer,MFaction> factions = new ConcurrentHashMap<>();

        private static void addFaction(MFaction faction){
            factions.put(faction.getId(),faction);
        }

        public static MFaction instance(int id,Role role,State state,int victories, int defeats, int opponentId,Set<UUID> bombers){
            MFaction faction = new MFaction(id,role,state,victories,defeats,opponentId,bombers);
            addFaction(faction);
            return faction;
        }

        public static MFaction getByFaction(Faction faction){
            return factions.get(Integer.parseInt(faction.getId()));
        }

        public static MFaction getByPlayer(FPlayer player){
            return factions.get(Integer.parseInt(player.getFactionId()));
        }

        public static MFaction getByPlayer(Player player){
            return factions.get(Integer.parseInt(FPlayers.getInstance().getByPlayer(player).getFactionId()));
        }
    }

}
