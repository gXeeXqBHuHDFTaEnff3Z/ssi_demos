package de.vertedge.ssicapabilitymanager.capabilitymgmt;

import android.content.Context;

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

import de.vertedge.ssicapabilitymanager.SSI.SSI_Representation;

@Entity(tableName = "capmgmt_messages")
@TypeConverters(Message.Converters.class)
public class Message {

    @Dao
    public interface SSIM_Dao {
        @Query("SELECT * FROM capmgmt_messages")
        List<Message> getAll();

        @Query("SELECT * FROM capmgmt_messages WHERE _uid IN (:userIds)")
        List<Message> loadAllByIds(int[] userIds);

        //@Query("SELECT * FROM ssi_messages WHERE to LIKE :identity")
        //SSI_Message findByTo(int identity, String last);

        @Query("SELECT * FROM capmgmt_messages WHERE _uid LIKE :uid")
        Message get(long uid);

        @Insert
        void insert(Message message);

        @Insert
        void insertAll(Message... messages);

        @Delete
        void delete(Message messages);
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
            String json = gson.toJson(list);
            return json;
        }
    }

    @PrimaryKey(autoGenerate = true) private long _uid;
    private String _text = "";
    private final String _from_user;
    private final String _to_user;
    private ArrayList<Long> _unsignedCapabilities = new ArrayList<>();
    private ArrayList<Long> _signedCapabilities = new ArrayList<>();
    private String _attachedRep;
    private String _signature = null;
    private boolean _unread = true;
    private boolean _isJobApplication;

    public Message(String _text, String _from_user, String _to_user, ArrayList<Long> _unsignedCapabilities, ArrayList<Long> _signedCapabilities, String _signature, boolean _unread, boolean _isJobApplication, String _attachedRep) {
        this._text = _text;
        this._from_user = _from_user;
        this._to_user = _to_user;
        this._unsignedCapabilities = _unsignedCapabilities;
        this._signedCapabilities = _signedCapabilities;
        this._signature = _signature;
        this._unread = _unread;
        this._isJobApplication= _isJobApplication;
        this._attachedRep = _attachedRep;
    }

    public long get_uid() {
        return _uid;
    }

    public void set_uid(long _uid) {
        this._uid = _uid;
    }

    public String get_text() {
        return _text;
    }

    public String get_from_user() {
        return _from_user;
    }

    public String get_to_user() {
        return _to_user;
    }

    public ArrayList<Long> get_unsignedCapabilities() {
        return _unsignedCapabilities;
    }

    public ArrayList<Long> get_signedCapabilities() {
        return _signedCapabilities;
    }

    public String get_signature() {
        return _signature;
    }

    public boolean is_unread() {
        return _unread;
    }

    public boolean is_isJobApplication() {
        return _isJobApplication;
    }

    public String get_attachedRep() {
        return _attachedRep;
    }

    @Override
    public String toString(){
        String result = "uid:" + _uid + "\n";
        result = result + "from:" + _from_user + "\n";
        result = result + "to:" + _to_user + "\n";
        result = result + "signature:" + _signature + "\n";
        result = result + _text;
        return result;
    }

    public static Message[] prepopulatedData(long cap1, long cap2, long cap3, Context context) {
        ArrayList<Long> hausarbeit = new ArrayList<>();
        hausarbeit.add( cap1 );
        ArrayList<Long> bewerbung = new ArrayList<>();
        bewerbung.add( cap2 );
        bewerbung.add( cap3 );

        return new Message[] {
                new Message("Liebes Fernuni-Team,\nnach bestandener Hausarbeit bitte ich um Signierung des Kompetenznachweises unten.\n\nVielen Dank", "ssi:fernuni-student:8685053", "ssi:fernuni-mitarbeiter", hausarbeit, null, null, true, false, SSI_Representation.prepopulatedData(context).toJSON())
        };}
}
