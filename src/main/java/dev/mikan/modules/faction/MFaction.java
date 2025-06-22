package dev.mikan.modules.faction;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import dev.mikan.altairkit.utils.NmsUtils;
import dev.mikan.database.module.impl.FactionsDB;
import dev.mikan.events.GraceStartEvent;
import dev.mikan.events.PeaceStartEvent;
import dev.mikan.events.RaidStartEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
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
    private @Setter int opponentId;
    private @Setter Role role;
    private @Setter State state;
    private final Set<UUID> bombers;
    private @Setter String nextState;

    private MFaction(int id, Role role, State state, int victories, int defeats,String nextState, int opponentId, Set<UUID> bombers) {
        this.id = id;
        this.victories = victories;
        this.defeats = defeats;
        this.opponentId = opponentId;
        this.role = role;
        this.state = state;
        this.bombers = bombers;
        this.nextState = nextState;
    }

    @Override
    public String toString() {
        return String.valueOf(this.getId());
    }

    /*
    * Special id gotten from faction and opponent ids
    * to get it both must be in raid state and in raid
    * with each other.
    *
    * format -> "ATTACKERS-FACTION-ID:DEFENDERS-FACTION-ID"
    * */
    public String getRaidId(){
        MFaction opponent = MFactions.getById(opponentId);
        if (opponent == null) return "";

        if (opponent.state != State.RAID
                || this.state != State.RAID
                || opponent.opponentId != this.id) return "";

        if (this.role == Role.ATTACKERS && opponent.role == Role.DEFENDERS)
            return this.id + ":" + opponentId;
        else if (this.role == Role.DEFENDERS && opponent.role == Role.ATTACKERS)
            return opponentId + ":" + this.id;

        return "";
    }

    public int maxBombers(){
        int members = Factions.getInstance().getFactionById(String.valueOf(id)).getFPlayers().size();
        return (members / 100 * 15) + 3;
    }

    /*
    * TODO: check if factions like warzone and wildernes will be stored
    *  into the database one created
    * */
    @UtilityClass
    public final static class MFactions{
        private @Getter final Map<Integer,MFaction> factions = new ConcurrentHashMap<>();

        // Faction raid id -> Bukkit task id
        private @Getter final Map<String, Integer> raidTasksCache = new ConcurrentHashMap<>();

        // Faction id -> Bukkit task id
        private @Getter final Map<Integer, Integer> graceTasksCache = new ConcurrentHashMap<>();

        private void updateFaction(MFaction faction){
            factions.put(faction.getId(),faction);
        }

        public MFaction instance(int id,Role role,State state,int victories, int defeats,String nextState, int opponentId,Set<UUID> bombers){
            MFaction faction = new MFaction(id,role,state,victories,defeats,nextState,opponentId,bombers);
            updateFaction(faction);
            return faction;
        }

        public MFaction getByFaction(Faction faction){
            return factions.get(Integer.parseInt(faction.getId()));
        }

        public MFaction getByPlayer(FPlayer player){
            return factions.get(Integer.parseInt(player.getFactionId()));
        }

        public MFaction getByPlayer(Player player){
            return factions.get(Integer.parseInt(FPlayers.getInstance().getByPlayer(player).getFactionId()));
        }

        public MFaction getByName(String name){
            Faction faction = Factions.getInstance().getByTag(name);
            return faction != null ? factions.get(Integer.parseInt(faction.getId())) : null;
        }

        public MFaction getById(int id){
            return factions.get(id);
        }

        public void destruct(int id){
            factions.remove(id);
        }

        public void startRaid(MFaction attackingFaction,MFaction defendingFaction){
            RaidStartEvent event = new RaidStartEvent(attackingFaction,defendingFaction,FactionModule.instance().getPlugin());
            Bukkit.getPluginManager().callEvent(event);
            event.run();
        }

        public void startGrace(MFaction attackingFaction,MFaction defendingFaction){
            GraceStartEvent event = new GraceStartEvent(attackingFaction,defendingFaction,FactionModule.instance().getPlugin());
            Bukkit.getPluginManager().callEvent(event);
            event.run();
        }

        public void startPeace(MFaction faction){
            PeaceStartEvent event = new PeaceStartEvent(faction,FactionModule.instance().getPlugin());
            Bukkit.getPluginManager().callEvent(event);
            event.run();
        }


        // Reset raid role,state and opponent id
        // If in raid state will do it for opponent as well (raidTasksCache included)
        public void reset(MFaction faction){
            if (faction == null) return;
            if (faction.state == State.RAID){
                MFaction opponent = getById(faction.opponentId);

                opponent.state = State.PEACE;
                opponent.opponentId = -1;
                opponent.role = Role.NONE;

                faction.state = State.PEACE;
                faction.opponentId = -1;
                faction.role = Role.NONE;

                faction.nextState = "";
                opponent.nextState = "";

                Bukkit.getScheduler().cancelTask(raidTasksCache.remove(faction.getRaidId()));

                FactionsDB.instance().update(faction);
                FactionsDB.instance().update(opponent);

                return;
            }

            faction.state = State.PEACE;
            faction.opponentId = -1;
            faction.role = Role.NONE;
            faction.nextState = "";
            FactionsDB.instance().update(faction);
        }

        public void sendTitle(Faction faction,String title, String subtitle){
            for (FPlayer player : faction.getFPlayersWhereOnline(true)) {
                NmsUtils.sendTitle(player.getPlayer(),title);
                NmsUtils.sendSubtitle(player.getPlayer(),subtitle);
            }
        }
    }

}
