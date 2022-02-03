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
import java.util.List;

@Entity(tableName = "capmgmt_organisations")
@TypeConverters(Organisation.Converters.class)
public class Organisation {

    @Dao
    public interface Org_Dao {
        @Query("SELECT * FROM capmgmt_organisations")
        List<Organisation> getAll();

        @Query("SELECT * FROM capmgmt_organisations WHERE _uid LIKE :uid LIMIT 1")
        Organisation get(long uid);

        @Query("SELECT * FROM capmgmt_organisations WHERE _ssidid LIKE :ssidid LIMIT 1")
        Organisation getByDID(String ssidid);

        @Insert
        void insertAll(Organisation... orgs);

        @Delete
        void delete(Organisation orgs);
    }

    public static class Converters {
        @TypeConverter
        public static ArrayList<Integer> fromInteger(String value) {
            Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromArrayList(ArrayList<Integer> list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            return json;
        }
    }

    @PrimaryKey private final long _uid;
    private final String _name;
    private final String _ssidid;
    private boolean _educator = false;
    private boolean _jobsearch = true;
    private final ArrayList<Integer> _members;

    public Organisation(long _uid, String _name, String _ssidid, boolean _educator, boolean _jobsearch, ArrayList<Integer> _members) {
        this._uid = _uid;
        this._name = _name;
        this._ssidid = _ssidid;
        this._educator = _educator;
        this._jobsearch = _jobsearch;
        this._members = _members;
    }

    public long get_uid() {
        return _uid;
    }

    public String get_name() {
        return _name;
    }

    public String get_ssidid() {
        return _ssidid;
    }

    public boolean is_educator() {
        return _educator;
    }

    public boolean is_jobsearch() {
        return _jobsearch;
    }

    public ArrayList<Integer> get_members() {
        return _members;
    }

    public void set_educator(boolean _educator) {
        this._educator = _educator;
    }

    public void set_jobsearch(boolean _jobsearch) {
        this._jobsearch = _jobsearch;
    }

    public void add_member(int _member) {
        this._members.add(_member);
    }

    public void remove_member(int _member) {
        this._members.remove(_member);
    }

    public static Organisation[] prepopulatedData() {
        ArrayList<Integer> _unimembers = new ArrayList<Integer>();
        _unimembers.add(0);
        ArrayList<Integer> _fabmembers = new ArrayList<Integer>();
        _unimembers.add(2);
        return new Organisation[] {
                new Organisation(0, "Fernuniversität", "ssi:fernuni", true, false, _unimembers),
                new Organisation(1, "Die Fabrik", "ssi:fab", false, true, _fabmembers)
        };
    }
}
