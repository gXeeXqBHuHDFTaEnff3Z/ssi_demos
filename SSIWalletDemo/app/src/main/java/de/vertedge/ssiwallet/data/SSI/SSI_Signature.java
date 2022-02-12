package de.vertedge.ssiwallet.data.SSI;

import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import java.util.List;

@Entity(tableName = "ssi_signatures")
public class SSI_Signature {

    @Dao
    public interface SSI_Signature_Dao {
        @Query("SELECT * FROM ssi_signatures")
        List<SSI_Signature> getAll();

        @Insert
        void insertAll(SSI_Signature... ssigs);
    }

    @PrimaryKey
    private final int _id;
    private final int _ssi_owner;
    private final int _ssi_signed_credential;
    private final String _signature;

    public SSI_Signature(int _id, int _ssi_owner, int _ssi_signed_credential, String _signature) {
        this._id = _id;
        this._ssi_owner = _ssi_owner;
        this._ssi_signed_credential = _ssi_signed_credential;
        this._signature = _signature;
    }

    public int get_id() {
        return _id;
    }

    public int get_ssi_signed_credential() {
        return _ssi_signed_credential;
    }

    public int get_ssi_owner() {
        return _ssi_owner;
    }

    public String get_signature() {
        return _signature;
    }

    public static SSI_Signature[] prepopulatedData() {
        return new SSI_Signature[] {
                new SSI_Signature(0, 0, 0, "prepossignatur")
        };
    }
}
