package de.vertedge.ssiwallet.data.SSI;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.vertedge.ssiwallet.R;

@Entity(tableName = "ssi_vcs")
@TypeConverters(SSI_VerifiableCredential.Converters.class)
public class SSI_VerifiableCredential {

    @Dao
    public interface SSI_VC_Dao {
        @Query("SELECT * FROM ssi_vcs")
        List<SSI_VerifiableCredential> getAll();

        @Query("SELECT * FROM ssi_vcs WHERE _uid LIKE :uid LIMIT 1")
        SSI_VerifiableCredential get(long uid);

        @Insert
        void insertAll(SSI_VerifiableCredential... vcs);

        @Insert
        void insert(SSI_VerifiableCredential vc);

        @Query("UPDATE ssi_vcs SET _color=:color WHERE _uid = :uid")
        void updateColor(long uid, int color);
    }

    public static class Converters {

        @TypeConverter
        public static Instant toDate(Long dateLong){
            return dateLong == null ? null: Instant.ofEpochSecond(dateLong);
        }

        @TypeConverter
        public static Long fromDate(Instant date){
            return date == null ? null : date.getEpochSecond();
        }

        @TypeConverter
        public static ArrayList<SSI_Claim> fromClaim(String value) {
            Type listType = new TypeToken<ArrayList<SSI_Claim>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String toClaim(ArrayList<SSI_Claim> list) {
            Gson gson = new Gson();
            return gson.toJson(list);
        }

        @TypeConverter
        public static ArrayList<SSI_Proof> fromProof(String value) {
            Type listType = new TypeToken<ArrayList<SSI_Proof>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String toProof(ArrayList<SSI_Proof> list) {
            Gson gson = new Gson();
            return gson.toJson(list);
        }
    }

    public static final String DEFAULT_CONTEXT = "ssi-vc";

    @PrimaryKey(autoGenerate = true) private long _uid;
    private final ArrayList<SSI_Claim> _claims;
    private final ArrayList<SSI_Proof> _proofs;
    private final int _iconID;
    private int _color;
    private final long _credentialSubject;
    private final String _context;
    private final String _issuer;
    private final Instant _issuance;
    private final Instant _expires;
    private boolean _starred;

    /** constructor for creating VC from database
     * @param _claims   list of claim
     * @param _proofs   list of proofs for the claims
     * @param iconID    drawable id for the GUI
     * @param color     background color for the GUI title
     * @param _credentialSubject    uid of the identity that is the subject of this VC
     * @param _context  context this VC is meant to be interpreted in
     * @param _issuer   VC issuer
     * @param _issuance time of issuance
     * @param _expires  time of expirance
     * @param _starred  TRUE IFF favourite in GUI
     */
    public SSI_VerifiableCredential(ArrayList<SSI_Claim> _claims, ArrayList<SSI_Proof> _proofs, int iconID, int color, long _credentialSubject, String _context, String _issuer, Instant _issuance, Instant _expires, boolean _starred) {
        this._claims = _claims;
        this._proofs = _proofs;
        this._iconID = iconID;
        this._color = color;
        this._credentialSubject = _credentialSubject;
        this._context = _context;
        this._issuer = _issuer;
        this._issuance = _issuance;
        this._expires = _expires;
        this._starred = _starred;
    }

    /** creates a vc from a representation. used to add new vcs received over the network.
     *
     * @param representation received SSI_Rep we want to create a VC from
     */
    public SSI_VerifiableCredential (SSI_Representation representation){
        this._claims = representation.get_claims();
        this._proofs = representation.get_proofs();
        this._color = -1;
        this._iconID = representation.get_iconID();
        this._credentialSubject = representation.get_credentialSubject();
        this._context = representation.get_context();
        this._issuer = representation.get_issuer();
        this._issuance = representation.get_issuance();
        this._expires = representation.get_expiration();
        this._starred = false;
    }

    /** DEMO shortened constructor for creating examples. issuance is now, expires tomorrow.
     *
     * @param claimSubject  subject of the claim
     * @param claimProperty property the subject is said to have
     * @param claimValue    value the property of the subject is said to have
     * @param _iconID   icon for the card title
     * @param proofSignature    SHA256 signature
     * @param _credentialSubject    uid of the SSI_Identity the VC is the subject of
     */
    public SSI_VerifiableCredential(String claimSubject, String claimProperty, String claimValue, int _iconID, String proofSignature, long _credentialSubject){
        this._claims = new ArrayList<>();
        addClaim(claimSubject, claimProperty, claimValue);
        this._proofs = new ArrayList<>();
        addProof(SSI_Proof.DEFAULT_PROOF_TYPE, proofSignature, SSI_Authority.DEFAULT_AUTHORITY);
        this._iconID = _iconID;
        this._credentialSubject = _credentialSubject;
        this._context = DEFAULT_CONTEXT;
        this._issuer = SSI_Authority.DEFAULT_AUTHORITY;
        this._issuance = Instant.now();
        this._expires = Instant.now().plus( 30, ChronoUnit.DAYS);
        this._starred = false;
    }

    public ArrayList<SSI_Proof> get_proofs() {
        return _proofs;
    }

    public String get_proof() {
        return TextUtils.join(", ", _proofs);
    }

    public void set_uid(long _uid) {
        this._uid = _uid;
    }

    public long get_uid() {
        return _uid;
    }

    public ArrayList<SSI_Claim> get_claims() {
        return _claims;
    }

    public String get_claim() {
        return TextUtils.join(", ", _claims);
    }

    public int get_iconID() {
        return _iconID;
    }

    public int get_color() {
        return _color;
    }

    public Instant get_issuance() {
        return _issuance;
    }

    public long get_credentialSubject() {
        return _credentialSubject;
    }

    public String get_context() {
        return _context;
    }

    public String get_issuer() {
        return _issuer;
    }

    public Instant get_expires() {
        return _expires;
    }

    public boolean is_starred() {
        return _starred;
    }

    public void set_starred(boolean _starred) {
        this._starred = _starred;
    }

    public void set_color(int _color) {
        this._color = _color;
    }

    /** make sure we have a color. of not, pick one at random
     *
     * @param context   application contect to get the resource colors
     */
    public void assertColor(Context context){
        if (_color != 0) return;

        int rn = new Random().nextInt(7);
        switch(rn){
            case 1:
                this._color = context.getColor(R.color.pass2);
                break;
            case 2:
                this._color = context.getColor(R.color.pass3);
                break;
            case 3:
                this._color = context.getColor(R.color.pass4);
                break;
            case 4:
                this._color = context.getColor(R.color.pass5);
                break;
            case 5:
                this._color = context.getColor(R.color.pass6);
                break;
            case 6:
                this._color = context.getColor(R.color.pass7);
                break;
            default:
                this._color = context.getColor(R.color.pass1);
                break;
        }
        SSI_Database db = SSI_Database.getInstance(context);
        db.vcDao().updateColor(this.get_uid(), this.get_color());
    }

    /** DEMO we are just checking predetermined SHA256s
     * TRUE IFF all claims can be proven using the proofs, checking them against the authority
     *
     * @return  TRUE IFF all claims can be proven useing all proofs
     */
    public boolean isValid(SSI_Authority authority){
        // if we find a claim that cannot be proven using the proofs, then we log it and return FALSE
        for (SSI_Claim claim : _claims){
            boolean claimOK = false;
            // can you find a proof that verifies this claim?
            for (SSI_Proof proof : _proofs){
                if (proof.validates(claim.toString()))
                    claimOK = true;
            }
            if (!claimOK) return false;
        }
        // if we did not manage to find a wrong one they must all be right
        return true;
    }

    public void addClaim(String subject, String property, String value){
        _claims.add(new SSI_Claim(subject, property, value));
    }

    public void addProof(String type, String signature, String authority){
        _proofs.add(new SSI_Proof(type, signature, authority));
    }

    @NonNull
    @Override
    public String toString(){
        String result;
        result = "SSI_VerifiableCredential [credentialSubject="
                + _credentialSubject
                + ", context="
                + _context
                + ", claims="
                + get_claim()
                + ", proofs="
                + get_proof()
                + ", issuer="
                + _issuer
                + ", issuance="
                + _issuance
                + ", expires="
                + _expires + "]";
        return result;
    }

    public String toJSON(){
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
        return gson.toJson(this);
    }

    public static SSI_VerifiableCredential[] prepopulatedData(long uid1, long uid2, long uid3, long uid4) {
        return new SSI_VerifiableCredential[] {
                new SSI_VerifiableCredential("ssi:valeria-morales", "ist", "fernuni-student:8685053", R.drawable.ic_identity_white, "3c65c24b75a9daf7d0ac4f40d5931f408477fe2ab1c0cc0d33e2002546626509", uid1),
                new SSI_VerifiableCredential("ssi:holger-kaufmann", "ist", "fernuni-mitarbeiter:942", R.drawable.ic_identity_white, "429d637c4349e94bbbea871d2fbc5e7908799a48276aa10720058c42a5e366e8", uid3),
                new SSI_VerifiableCredential("ssi:jamena-untabe",   "ist", "fab-worker:589746",  R.drawable.ic_identity_white,  "b7f324531994b3807d9d4e3cae886c068de1bef226b621516ccadf8febef7fe3", uid2),
                new SSI_VerifiableCredential("ssi:dr.-evil",   "geh??rt", "bs??.bund.de", -1, "960d64de93e5d72d5db5139bf77f0fa71c8c79d574b7540a2bd579e13416f012", uid4)
        };
    }
}
