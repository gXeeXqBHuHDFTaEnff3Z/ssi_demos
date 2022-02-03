package de.vertedge.ssiwallet.data.SSI;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import org.apache.commons.codec.digest.DigestUtils;
import java.util.List;

import de.vertedge.ssiwallet.R;

@Entity(tableName = "ssi_authorities")
public class SSI_Authority {

    public static String DEFAULT_AUTHORITY = "Deutschland (DE)";

    @Dao
    public interface SSI_Authority_Dao {
        @Query("SELECT * FROM ssi_authorities")
        List<SSI_Authority> getAll();

        @Query("SELECT * FROM ssi_authorities WHERE _uid LIKE :uid LIMIT 1")
        SSI_Authority findByUID(long uid);

        @Query("SELECT * FROM ssi_authorities WHERE name LIKE :name LIMIT 1")
        SSI_Authority findByName(String name);

        @Insert
        void insertAll(SSI_Authority... authorities);
    }

    @PrimaryKey(autoGenerate = true) private long _uid;
    private final String name;
    private final String uri;
    private final String publicKey;
    private final int picture;
    private boolean enabled;

    /** constructor used by dao
     *
     * @param name
     * @param uri
     * @param publicKey
     * @param picture
     */
    public SSI_Authority(String name, String uri, String publicKey, int picture, boolean enabled) {
        this.name = name;
        this.uri = uri;
        this.publicKey = publicKey;
        this.picture = picture;
        this.enabled = enabled;
    }

    public long get_uid() {
        return _uid;
    }

    public void set_uid(long _uid) {
        this._uid = _uid;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public int getPicture() {
        return picture;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @NonNull
    @Override
    public String toString(){
        return name;
    }

    /** DEMO STUB authorities don't sign anything on local devices (unless when Self-Signing)
     * and signing is not really done with public keys
     *
     * @param _document document that needs to be signed
     * @return signed document
     */
    public String sign(String _document) {
        String _signature = _document + "\n----SHA256 SIGNATURE----\n"+ publicKey;
        _signature = DigestUtils.sha256Hex(_signature);

        return _signature; // Outputs "SGVsbG8="
    }

    public static SSI_Authority[] prepopulatedData() {
        return new SSI_Authority[] {
                new SSI_Authority(DEFAULT_AUTHORITY, "eid://127.0.0.1:24727/eID-Client", "fb36b404a70c9b576d332b3018abb7fa48dc524593aa8aac776427db3636b064", R.drawable.flag_germany, true),
                new SSI_Authority("Netherlands (NL)", "eid://thisdoesnotreallyexist.nl/eid", "c015344ccb2d1eeb7773ab2b649ac7037f94858a97c2506c4daa67dccbe5e135", R.drawable.flag_nl, true)
        };
    }
}
