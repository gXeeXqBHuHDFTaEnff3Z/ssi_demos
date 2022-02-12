package de.vertedge.ssicapabilitymanager.capabilitymgmt;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import de.vertedge.ssicapabilitymanager.R;

@Entity(tableName = "capmgmt_users")
@TypeConverters(User.Converters.class)
public class User {

    /** database API
     *
     */
    @Dao
    public interface UserDao {
        @Query("SELECT * FROM capmgmt_users")
        List<User> getAll();

        @Query("SELECT * FROM capmgmt_users WHERE _uid LIKE :uid")
        User get(long uid);

        @Query("SELECT * FROM capmgmt_users WHERE _ssidid LIKE :ssidid LIMIT 1")
        User findByDID(String ssidid);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insert(User user);

        @Insert
        void insertAll(User... users);

        @Delete
        void delete(User user);
    }

    public static class Converters {

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

    @PrimaryKey(autoGenerate = true) private long _uid;
    private final String _firstname;
    private final String _lastname;
    private final String _ssidid;
    private final int _picture;
    private final long _organisationUID;
    private final boolean _roleCompFacilitator;
    private final boolean _roleCompOwner;
    private final boolean _roleCompDeployer;
    private ArrayList<Long> jobsAppliedFor = new ArrayList<>();
    private final ArrayList<Long> capsVotedOn;

    /** constructor for creating new user from database entry
     *
     * @param _firstname    users first name
     * @param _lastname     users last name
     * @param _ssidid       DID of the user
     * @param _picture      drawable id of the picture
     * @param _organisationUID  database uid for org the user is member of
     * @param _roleCompDeployer TRUE IFF job deployer
     * @param _roleCompOwner    TRUE IFF cap owner
     * @param _roleCompFacilitator  TRUE IFF educational facility worker
     * @param capsVotedOn   list of caps this user already voted for to prohibit double voting
     */
    public User(String _firstname, String _lastname, String _ssidid, int _picture, long _organisationUID, boolean _roleCompDeployer, boolean _roleCompOwner, boolean _roleCompFacilitator, ArrayList<Long> capsVotedOn) {
        this._firstname = _firstname;
        this._lastname = _lastname;
        this._ssidid = _ssidid;
        this._picture = _picture;
        this._organisationUID = _organisationUID;
        this._roleCompFacilitator = _roleCompFacilitator;
        this._roleCompOwner = _roleCompOwner;
        this._roleCompDeployer = _roleCompDeployer;
        this.capsVotedOn = capsVotedOn;
    }

    /** shortened constructor omitting votings for creating a new user
     *
     * @param _firstname    users first name
     * @param _lastname     users last name
     * @param _ssidid       DID of the user
     * @param _picture      drawable id of the picture
     * @param _organisationUID  database uid for org the user is member of
     * @param _roleCompDeployer TRUE IFF job deployer
     */
    @Ignore
    public User(String _firstname, String _lastname, String _ssidid, int _picture, long _organisationUID, boolean _roleCompFacilitator, boolean _roleCompOwner, boolean _roleCompDeployer) {
        this._firstname = _firstname;
        this._lastname = _lastname;
        this._ssidid = _ssidid;
        this._picture = _picture;
        this._organisationUID = _organisationUID;
        this._roleCompFacilitator = _roleCompFacilitator;
        this._roleCompOwner = _roleCompOwner;
        this._roleCompDeployer = _roleCompDeployer;
        this.capsVotedOn = new ArrayList<>();
    }

    public long get_uid() {
        return _uid;
    }

    public void set_uid(long _uid) {
        this._uid = _uid;
    }

    public String get_firstname() {
        return _firstname;
    }

    public String get_lastname() {
        return _lastname;
    }

    public int get_picture() {
        return _picture;
    }

    public long get_organisationUID() {
        return _organisationUID;
    }

    public ArrayList<Long> getJobsAppliedFor() {
        return jobsAppliedFor;
    }

    public void setJobsAppliedFor(ArrayList<Long> jobsAppliedFor) {
        this.jobsAppliedFor = jobsAppliedFor;
    }

    public String get_ssidid() {
        return _ssidid;
    }

    public ArrayList<Long> getCapsVotedOn() {
        return capsVotedOn;
    }

    public boolean is_roleCompFacilitator() {
        return _roleCompFacilitator;
    }

    public boolean is_roleCompOwner() {
        return _roleCompOwner;
    }

    public boolean is_roleCompDeployer() {
        return _roleCompDeployer;
    }

    /** DEMO static data
     *
     * @param org1  uid of a org
     * @param org2  uid of another org
     * @return  list of hardcoded DEMO users
     */
    public static User[] prepopulatedData(long org1, long org2) {

        return new User[] {
                new User("Valeria", "Morales", "fernuni-student:8685053", R.drawable.ic_certificate, -1, false, true, false),
                new User("Jamena","Untabe", "fab-worker:589746", R.drawable.ic_fabrik, org1, false, false, true),
                new User("Holger","Kaufmann", "fernuni-mitarbeiter:942", R.drawable.ic_education, org2, true, false, false)
        };
    }
}
