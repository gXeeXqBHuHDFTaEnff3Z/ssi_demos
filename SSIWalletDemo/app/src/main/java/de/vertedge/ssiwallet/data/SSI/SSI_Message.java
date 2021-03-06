package de.vertedge.ssiwallet.data.SSI;

import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import java.util.List;

@Entity(tableName = "ssi_messages")
public class SSI_Message {

    @Dao
    public interface SSIM_Dao {
        @Query("SELECT * FROM ssi_messages")
        List<SSI_Message> getAll();

         @Insert
        void insertAll(SSI_Message... messages);
    }

    @PrimaryKey public int uid;

    String _text;
    int _from_ssi_id;
    int _to_ssi_id;
    int _ssi_capability;
    boolean _unread;

    public SSI_Message(String _text, int _from_ssi_id, int _to_ssi_id, int _ssi_capability, boolean _unread) {
        this._text = _text;
        this._from_ssi_id = _from_ssi_id;
        this._to_ssi_id = _to_ssi_id;
        this._ssi_capability = _ssi_capability;
        this._unread = _unread;
    }

    public String get_text() {
        return _text;
    }

    public int getUid() {
        return uid;
    }

    public int get_from_ssi_id() {
        return _from_ssi_id;
    }

    public int get_to_ssi_id() {
        return _to_ssi_id;
    }

    public int get_ssi_capability() {
        return _ssi_capability;
    }

    public boolean is_unread() {
        return _unread;
    }
}
