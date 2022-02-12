package de.vertedge.ssicapabilitymanager.capabilitymgmt;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "capmgmt_jobs")
@TypeConverters(Joblisting.Converters.class)
public class Joblisting {

    public static class Converters {

        @TypeConverter
        public static Date toDate(Long dateLong){
            return dateLong == null ? null: new Date(dateLong);
        }

        @TypeConverter
        public static Long fromDate(Date date){
            return date == null ? null : date.getTime();
        }

        @TypeConverter
        public static ArrayList<Long> fromInteger(String value) {
            Type listType = new TypeToken<ArrayList<Long>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromIntArrayList(ArrayList<Long> list) {
            Gson gson = new Gson();
            return gson.toJson(list);
        }
    }

    @Dao
    public interface Jobs_Dao {
        @Query("SELECT * FROM capmgmt_jobs ORDER BY _starred DESC")
        List<Joblisting> getAll();

        @Query("SELECT * FROM capmgmt_jobs WHERE _uid LIKE :uid LIMIT 1")
        Joblisting get(long uid);

        @Insert
        void insertAll(Joblisting... jobs);

        @Insert
        long insert(Joblisting job);

        @Query("UPDATE capmgmt_jobs SET _starred=:starred WHERE _uid = :uid")
        void update(long uid, boolean starred);

        @Delete
        void delete(Joblisting jobs);
    }

    @PrimaryKey(autoGenerate = true) private int _uid;
    private final long _company_id;
    private final String _link;
    private final Date _validUntil;
    private final ArrayList<Long> minRequirements;
    private final ArrayList<Long> bonusRequirements;
    private boolean _starred;

    /** constructor for creating from database
     *
     * @param _company_id       uid of the organisation
     * @param _validUntil       when this listing will be delisted
     * @param minRequirements   capabilities that you need to have to compete
     * @param bonusRequirements capabilties that will improve the competetiveness of the applicant
     * @param _link             weblink to the joblisting / company website
     * @param _starred          flagged in the GUI as favourite
     */
    public Joblisting(long _company_id, Date _validUntil, ArrayList<Long> minRequirements, ArrayList<Long> bonusRequirements, String _link, boolean _starred) {
        this._company_id = _company_id;
        this._validUntil = _validUntil;
        this.minRequirements = minRequirements;
        this.bonusRequirements = bonusRequirements;
        this._link = _link;
        this._starred = _starred;
    }

    public void set_uid(int _uid) {
        this._uid = _uid;
    }

    public int get_uid() {
        return _uid;
    }

    public long get_company_id() {
        return _company_id;
    }

    public Date get_validUntil() {
        return _validUntil;
    }

    public ArrayList<Long> getMinRequirements() {
        return minRequirements;
    }

    public ArrayList<Long> getBonusRequirements() {
        return bonusRequirements;
    }

    public String get_link() {
        return _link;
    }

    public void set_starred(boolean _starred) {
        this._starred = _starred;
    }

    public boolean is_starred() {
        return _starred;
    }

    /** DEMO static data
     *
     * @return  list of hardcoded DEMO jobs
     */
    public static Joblisting[] prepopulatedData(long cap1, long cap2, long cap3, long cap4) {
        ArrayList<Long> minReqs = new ArrayList<>();
        minReqs.add(cap1);
        ArrayList<Long> bonusReqs = new ArrayList<>();
        bonusReqs.add(cap2);
        bonusReqs.add(cap3);
        ArrayList<Long> maxRequireents = new ArrayList<>();
        maxRequireents.add(cap1);
        maxRequireents.add(cap2);
        maxRequireents.add(cap3);
        maxRequireents.add(cap4);

        String link1 = "https://www.fernuni-hagen.de/uniintern/arbeitsthemen/karriere/stellen/index.shtml";
        String link2 = "https://www.bsi.bund.de/DE/Karriere/Stellenangebote/stellenangebot_node.html";

        return new Joblisting[] {

                new Joblisting(1, new Date(2022, 5, 1), minReqs, bonusReqs, link1, false),
                new Joblisting(1, new Date(2022, 5, 1), maxRequireents, null, link2, false)
        };
    }
}
