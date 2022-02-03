package de.vertedge.ssicapabilitymanager.SSI;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SSI_Representation {

    private final ArrayList<SSI_Claim> _claims;
    private final ArrayList<SSI_Proof> _proofs;
    private long _credentialSubject;
    private final String _context;
    private final String _issuer;
    private final Instant _issuance;
    private final Instant _expiration;

    public SSI_Representation(ArrayList<SSI_Claim> _claims, ArrayList<SSI_Proof> _proofs, long _credentialSubject, String _context, String _issuer, Instant _issuance, Instant _expiration) {
        this._claims = _claims;
        this._proofs = _proofs;
        this._credentialSubject = _credentialSubject;
        this._context = _context;
        this._issuer = _issuer;
        this._issuance = _issuance;
        this._expiration = _expiration;
    }

    /** creates a representation from a json
     *
     * @param json
     * @throws JSONException
     */
    public SSI_Representation(String json) throws JSONException {
        Log.d("SSI_Representation(String json)", (json == null ? "NULL" : json));
        // Deserialize json into object fields
        JSONObject jo = new JSONObject(json);
        this._credentialSubject = jo.getLong("_credentialSubject");
        this._context = jo.getString("_context");
        this._issuer = jo.getString("_issuer");

        // dates are either empty or in "yyyy-MM-dd'T'HH:mm:ssZ" format
        if ( jo.has("_issuance") ){
            String issuance = jo.getString("_issuance");
            if (issuance.equals("{}")){
                this._issuance = null;
            } else {
                this._issuance = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").parse(issuance, Instant::from);
            }
        } else {
            this._issuance = null;
        }

        if ( jo.has("_expiration") ){
            String expiration = jo.getString("_expiration");
            if (expiration.equals("{}")){
                this._expiration = null;
            } else {
                this._expiration = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").parse( expiration, Instant::from);
            }
        } else {
            this._expiration = null;
        }

        // claims
        JSONArray jsonClaims = jo.getJSONArray("_claims");
        this._claims = new ArrayList<>();
        for(int i=0;i<jsonClaims.length();i++){
            JSONObject jsonClaim = jsonClaims.getJSONObject( i );
            String prop = jsonClaim.getString("_property");
            String subj = jsonClaim.getString("_subject");
            String valu = jsonClaim.getString("_value");
            _claims.add(new SSI_Claim(subj, prop, valu));
        }

        // proofs
        JSONArray jsonProofs = jo.getJSONArray("_proofs");
        this._proofs = new ArrayList<>();
        for(int i=0;i<jsonProofs.length();i++){
            JSONObject jsonProof = jsonProofs.getJSONObject( i );
            String sign = jsonProof.getString("_signature");
            String auth = jsonProof.getString("_authority");
            String type = jsonProof.getString("_type");
            _proofs.add(new SSI_Proof(type, sign, auth));
        }
    }

    public ArrayList<SSI_Claim> get_claims() {
        return _claims;
    }

    public String get_claim() {
        return TextUtils.join(", ", _claims);
    }

    public ArrayList<SSI_Proof> get_proofs() {
        return _proofs;
    }

    public long get_credentialSubject() {
        return _credentialSubject;
    }

    public void set_credentialSubject(long _credentialSubject) {
        this._credentialSubject = _credentialSubject;
    }

    public String get_context() {
        return _context;
    }

    public String get_issuer() {
        return _issuer;
    }

    public Instant get_issuance() {
        return _issuance;
    }

    public Instant get_expiration() {
        return _expiration;
    }

    /**
     * DEMO we are just checking predetermined SHA256s
     * TRUE IFF all claims can be proven using the proofs, checking them against the authority
     *
     * @return
     */
    public boolean isValid(SSI_Authority authority) {
        // if we find a claim that cannot be proven using the proofs, then we log it and return FALSE
        for (SSI_Claim claim : _claims) {
            boolean claimOK = false;
            // can you find a proof that verifies this claim?
            for (SSI_Proof proof : _proofs) {
                if (proof.validates(claim.toString()))
                    claimOK = true;
            }
            if (!claimOK) return false;
        }
        // if we did not manage to find a wrong one they must all be right
        return true;
    }

    /**
     * returns picture ID if VC contains a claim 'picture id ID'
     *
     * @return
     */
    public int get_picture() {
        int result = -1;
        for (SSI_Claim claim : _claims) {
            if (claim.toString().contains("picture id")) {
                return Integer.parseInt(claim.get_value());
            }
        }
        return -1;
    }

    public void addClaim(String subject, String property, String value) {
        _claims.add(new SSI_Claim(subject, property, value));
    }

    public void addProof(String type, String signature, String authority) {
        _proofs.add(new SSI_Proof(type, signature, authority));
    }

    public String toJSON() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
        return gson.toJson(this);
    }

    public static SSI_Representation prepopulatedData(Context context){
        ArrayList<SSI_Claim> claims = new ArrayList<>();
        ArrayList<SSI_Proof> proofs = new ArrayList<>();

        SSI_Claim claim = new SSI_Claim("ssi:fernuni-student:8685053", "beherrscht", "Gestaltung kooperativer Systeme");
        claims.add(claim);

        String vcContext = context.getPackageName();

        Instant issuance = Instant.now();
        Instant expiration = Instant.now().plusSeconds(60*60*24*30);

        return new SSI_Representation(claims, proofs, -1, vcContext, "fernuni", issuance, expiration);
    }
}
