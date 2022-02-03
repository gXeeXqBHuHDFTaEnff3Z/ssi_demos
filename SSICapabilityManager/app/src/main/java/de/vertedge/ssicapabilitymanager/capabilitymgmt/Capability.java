package de.vertedge.ssicapabilitymanager.capabilitymgmt;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.List;

@Entity(tableName = "capmgmt_capabilities", indices = {@Index(value = {"_name"},unique = true)})
@TypeConverters(Capability.Converters.class)
public class Capability {

    public enum CapState {SUGGESTION, VOTING, ARBITRATION, ACCEPTED, BLOCKED;

        public static CapState getById(int id) {
            for(CapState e : values()) {
                if(e.ordinal() == id) return e;
            }
            throw new IndexOutOfBoundsException("A capability state with id " + id + " does not exist");
        }}

    public static class Converters{
        @TypeConverter
        public static Instant toDate(Long dateLong){
            return dateLong == null ? null: Instant.ofEpochSecond(dateLong);
        }

        @TypeConverter
        public static Long fromDate(Instant date){
            return date == null ? null : date.getEpochSecond();
        }
    }

    @Dao
    public interface Cap_Dao {
        @Query("SELECT * FROM capmgmt_capabilities")
        List<Capability> getAll();

        @Query("SELECT * FROM capmgmt_capabilities WHERE _uid LIKE :uid LIMIT 1")
        Capability get(long uid);

        @Query("SELECT * FROM capmgmt_capabilities WHERE _name LIKE :name LIMIT 1")
        Capability getByName(String name);

        @Query("SELECT COUNT(_name) FROM capmgmt_capabilities WHERE _state=:state")
        long countStates(CapState state);

        @Query("UPDATE capmgmt_capabilities SET _votesYes=_votesYes+1 WHERE _uid = :uid")
        void voteYes(long uid);

        @Query("UPDATE capmgmt_capabilities SET _votesNo=_votesNo+1 WHERE _uid = :uid")
        void voteNo(long uid);

        @Insert
        void insertAll(Capability... caps);

        @Insert
        long insert(Capability cap);

        @Delete
        void delete(Capability caps);
    }

    @PrimaryKey(autoGenerate = true) private long _uid;
    private final String _name;
    private final String _description;
    private final CapState _state;
    private final long _votesYes;
    private final long _votesNo;
    private final Instant _deadline;

    public Capability(String _name, String _description, CapState state, long votesYes, long votesNo, Instant deadline) {
        this._name = _name;
        this._description = _description;
        _state = state;
        _votesYes = votesYes;
        _votesNo = votesNo;
        _deadline = deadline;
    }

    public void set_uid(int _uid) {
        this._uid = _uid;
    }

    public long get_uid() {
        return _uid;
    }

    public String get_name() {
        return _name;
    }

    public String get_description() {
        return _description;
    }

    public CapState get_state() {
        return _state;
    }

    public long get_votesYes() {
        return _votesYes;
    }

    public long get_votesNo() {
        return _votesNo;
    }

    public Instant get_deadline() {
        return _deadline;
    }

    public static Capability[] prepopulatedData() {
        Instant deadline = Instant.parse("2023-01-01T10:15:30.345Z");

        return new Capability[] {
                new Capability("Gestaltung kooperativer Systeme", "siehe https://www.fernuni-hagen.de/mi/studium/module/koop_sys.shtml", CapState.ACCEPTED, 2048, 94, null),
                new Capability("Computerunterstützes kooperatives Lernen", "siehe https://de.wikipedia.org/wiki/Computerunterst%C3%BCtztes_kooperatives_Lernen", CapState.ACCEPTED, 1024, 299, null),
                new Capability("Grundlagen der objektorientierten Programmierung", "siehe https://de.wikipedia.org/wiki/Objektorientierte_Programmierung", CapState.ACCEPTED, 32768, 717, null),
                new Capability("Field Programmable Gate Arrays", "siehe https://de.wikipedia.org/wiki/Field_Programmable_Gate_Array", CapState.ARBITRATION, 248, 118, deadline),
                new Capability("Parallel Programming", "siehe https://en.wikipedia.org/wiki/Parallel_computing", CapState.ACCEPTED, 8096, 42, null),
                new Capability("Softwarequalitätskontrolle und -management", "siehe https://de.wikipedia.org/wiki/Softwarequalit%C3%A4t", CapState.VOTING, 0, 0, deadline)
        };
    }

    public static boolean requirementsFullfilled(List<Long> requirements, List<Long> given_capabilities){
        // if you find a requirement that is not matched, return false, else true
        for (long requirement : requirements){
            if ( !given_capabilities.contains(requirement) ) return false;
        }
        return true;
    }
}
