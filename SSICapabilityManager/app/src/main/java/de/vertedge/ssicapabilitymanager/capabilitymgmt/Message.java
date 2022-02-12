package de.vertedge.ssicapabilitymanager.capabilitymgmt;

import android.content.Context;

import androidx.annotation.NonNull;
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

import de.vertedge.ssicapabilitymanager.SSI.SSI_Representation;

@Entity(tableName = "capmgmt_messages")
@TypeConverters(Message.Converters.class)
public class Message {

    /** database API
     *
     */
    @Dao
    public interface SSIM_Dao {
        @Query("SELECT * FROM capmgmt_messages")
        List<Message> getAll();

        @Query("SELECT * FROM capmgmt_messages WHERE _uid LIKE :uid")
        Message get(long uid);

        @Insert
        void insert(Message message);

        @Insert
        void insertAll(Message... messages);

        @Delete
        void delete(Message messages);
    }

    /** converters for storing model in the sqlite db
     *
     */
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
    private final String _text;
    private final String _from_user;
    private final String _to_user;
    private final ArrayList<Long> _unsignedCapabilities;
    private final ArrayList<Long> _signedCapabilities;
    private final String _attachedRep;
    private final String _signature;
    private final boolean _unread;
    private final boolean _isJobApplication;

    /** construcor for creating new model messages from database
     *
     * @param _text     text content of message
     * @param _from_user    DID of the sending user
     * @param _to_user      DID of the receiving user
     * @param _unsignedCapabilities list of not validated capabilities
     * @param _signedCapabilities   list of validated capabiltites
     * @param _signature    SHA256
     * @param _unread   TRUE IFF this is a new message
     * @param _isJobApplication TRUE IFF meant as a job application (to change button text)
     * @param _attachedRep  SSI_Representation JSON
     */
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

    /** convert the message into a multiline string
     *
     * @return string containting uid, from, to and signature
     */
    @NonNull
    @Override
    public String toString(){
        String result = "uid:" + _uid + "\n";
        result = result + "from:" + _from_user + "\n";
        result = result + "to:" + _to_user + "\n";
        result = result + "signature:" + _signature + "\n";
        result = result + _text;
        return result;
    }

    /** DEMO static data
     *
     * @param cap1  uid for one capability
     * @param context   application context to get the DB
     * @return  list of hardcoded DEMO messages
     */
    public static Message[] prepopulatedData(long cap1, Context context) {
        ArrayList<Long> hausarbeit = new ArrayList<>();
        hausarbeit.add( cap1 );

        return new Message[] {
                new Message("Liebes Fernuni-Team,\nnach bestandener Hausarbeit bitte ich um Signierung des Kompetenznachweises unten.\n\nVielen Dank", "ssi:fernuni-student:8685053", "ssi:fernuni-mitarbeiter", hausarbeit, null, null, true, false, SSI_Representation.prepopulatedData(context).toJSON())
        };}
}
