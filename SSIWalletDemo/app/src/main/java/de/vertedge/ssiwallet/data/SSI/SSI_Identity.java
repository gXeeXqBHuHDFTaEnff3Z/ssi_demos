package de.vertedge.ssiwallet.data.SSI;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import de.vertedge.ssiwallet.R;

@Entity(tableName = "ssi_identities")
@TypeConverters(SSI_Identity.DateConverter.class)
public class SSI_Identity {

    @Dao
    public interface SSI_Identity_Dao {
        @Query("SELECT * FROM ssi_identities")
        List<SSI_Identity> getAll();

        @Query("SELECT * FROM ssi_identities WHERE _uid LIKE :uid LIMIT 1")
        SSI_Identity findByUID(long uid);

        @Query("SELECT * FROM ssi_identities WHERE _uid IN (:userIds)")
        List<SSI_Identity> loadAllByIds(long[] userIds);

        @Query("SELECT * FROM ssi_identities WHERE _firstname LIKE :firstname AND _lastname LIKE :lastname LIMIT 1")
        SSI_Identity findByName(String firstname, String lastname);

        @Insert
        void insertAll(SSI_Identity... identities);

        @Insert
        long insert(SSI_Identity identity);

        @Delete
        void delete(SSI_Identity identity);
    }

    public static class DateConverter {

        @TypeConverter
        public static Instant toDate(Long dateLong){
            return dateLong == null ? null: Instant.ofEpochSecond(dateLong);
        }

        @TypeConverter
        public static Long fromDate(Instant date){
            return date == null ? null : date.getEpochSecond();
        }
    }

    public static class SSI_DID_Helper{
        public static String didFromName(String firstname, String lastname){
            return "ssi:" + firstname + "-" + lastname;
        }

        public static String FirstNameFromDID(String ssidid){
            int index = ssidid.indexOf("-")-1;
            return ssidid.substring(0, Math.max(index, 0));
        }

        public static String LastNameFromDID(String ssidid){
            int index = ssidid.indexOf("-")+1;
            return ssidid.substring(Math.max(index,0));
        }

    }

    @PrimaryKey(autoGenerate = true) private long _uid;
    private final String _firstname;
    private final String _lastname;
    private final String _signature;
    private final Instant _issued;
    private final Instant _validUntil;
    private final Instant _birthday;
    private final int _picture;
    private final long _authority;
    private final String _privateKey;
    private final String _publicKey;

    public SSI_Identity(String firstname, String lastname, String signature, Instant issued, Instant validUntil, Instant birthday, int picture, long authority, String privateKey, String publicKey) {
        this._firstname = firstname;
        this._lastname = lastname;
        this._signature = signature;
        this._issued = issued;
        this._validUntil = validUntil;
        this._birthday = birthday;
        this._picture = picture;
        this._authority = authority;
        this._privateKey = privateKey;
        this._publicKey = publicKey;
    }

    @Ignore
    public SSI_Identity(String firstname, String lastname, String signature, Instant issued, Instant validUntil, Instant birthday, int picture, long authority) {
        this._firstname = firstname;
        this._lastname = lastname;
        this._signature = signature;
        this._issued = issued;
        this._validUntil = validUntil;
        this._birthday = birthday;
        this._picture = picture;
        this._authority = authority;
        this._privateKey = "privateKey";
        this._publicKey = "publicKey";
    }

    public void set_uid(long _uid) {
        this._uid = _uid;
    }

    public String getDID(){
        return SSI_DID_Helper.didFromName(_firstname, _lastname);
    }

    /* sign a document and return the signature

     */
    public String sign(String _document) {
        String _signature = _document + "\n----SHA256 SIGNATURE----\n"+this.getFullName();
        _signature = DigestUtils.sha256Hex(_signature);
        Log.d("SSI SIGNING", this.getFullName() + " gave signature " + _signature + " to " + _document);
        return _signature; // Outputs "SGVsbG8="
    }

    /* TRUE IFF the identity was signed by the authority

     */
    public boolean isSignedBy(String authority){
        return validateSignature(this._signature, this.getFullName(), authority);
    }

    public static boolean validateSignature(String signature, String document, String authority){
        String verification = document + "\n----SHA256 SIGNATURE----\n" + authority;
        verification = DigestUtils.sha256Hex(verification);

        Log.d("SSI Identity", "Verification for document " + document + ": Signature is '" + signature + "', compared to '" + verification + "'");
        return signature.equals(verification);
    }

    public Instant get_issued() {
        return _issued;
    }

    public Instant get_validUntil() {
        return _validUntil;
    }

    public String getFullName(){

        // if no name given then its anonymous
        if (_firstname.isEmpty() && _lastname.isEmpty() ){
            return "Anonymous";
        }

        if (_firstname.isEmpty()) return _lastname;
        if (_lastname.isEmpty()) return _firstname;

        return _firstname + " " + _lastname;
    }

    public long get_authority() {
        return _authority;
    }

    @NonNull
    @Override
    public String toString(){
        return getAsXML();
    }

    public String getAsXML(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone( ZoneId.systemDefault() );;
        String result = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        result = result + "<ssi-identity>\n";
        result = result + "  <firstname>" + _firstname + "</firstname>";
        result = result + "  <lastname>" + _lastname + "</lastname>";
        if (_birthday != null)
            result = result + "  <birthday>" + dtf.format(_birthday) + "</birthday>";
        result = result + "  <signature-sha256>" + _signature + "</signature-sha256>";
        result = result + "</ssi-identity>\n";
        return result;
    }

    public long get_uid() {
        return _uid;
    }

    public String get_firstname() {
        return _firstname;
    }

    public String get_lastname() {
        return _lastname;
    }

    public String get_signature() {
        return _signature;
    }

    public Instant get_birthday() {
        return _birthday;
    }

    public int get_picture() {
        return _picture;
    }

    public String get_privateKey() {
        return _privateKey;
    }

    public String get_publicKey() {
        return _publicKey;
    }

    public static SSI_Identity[] prepopulatedData(long _authorityID) {
        Instant issuedDate = Instant.now();
        Instant validUntil = Instant.now().plusSeconds(60*60*24*365);
        Instant birthday1 = Instant.parse("1986-07-26T08:25:20.00Z");
        Instant birthday2 = Instant.parse("2002-12-30T03:21:11.00Z");
        Instant birthday3 = Instant.parse("1973-10-03T09:49:15.00Z");

        return new SSI_Identity[] {
                new SSI_Identity("Valeria", "Morales", "ee3d29fa60ca721bde4707be580a3b3021f3825f8a7b00cc2d9ffeaf728afc25", issuedDate, validUntil, birthday2, R.drawable.woman1, _authorityID),
                new SSI_Identity("Jamena","Untabe", "136fab0c90f1cc53c1f4c219515c288fa9e8b57c83457e3ca5f03c67af26c2dd", issuedDate, validUntil, birthday1, R.drawable.woman2, _authorityID),
                new SSI_Identity("Holger","Kaufmann", "ea9679434d63ffcf3af4053a5f369b1105d6858e8fb1b58bc0ed39d461b82d77", issuedDate, validUntil, birthday3, R.drawable.man3, _authorityID),
                new SSI_Identity("Feruniversität", "Großgummersbach", "6784994c820f55c3492c5513aeb85dc28cdb1fe5eea1196127f62d145c3b1ae1", issuedDate, validUntil, null, R.drawable.fernuni, _authorityID),
                new SSI_Identity("Dr.", "Evil", "43999a4b0ff0afbba8e715f67712a0fa5ec8b7acb1f9fbaf91ccafd63931c15b", issuedDate, validUntil, birthday1, R.drawable.man4, _authorityID)
        };
    }
}
