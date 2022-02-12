package de.vertedge.ssiwallet.data.SSI;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SSI_Representation {

    private final ArrayList<SSI_Claim> _claims;
    private final ArrayList<SSI_Proof> _proofs;
    private final int _iconID;
    private long _credentialSubject;
    private final String _termsOfUse; // how can this rep be used?
    private final String _context; // in what context is this rep valid?
    private final String _issuer;
    private final java.time.Instant _issuance;
    private final java.time.Instant _expires;

    public SSI_Representation(ArrayList<SSI_Claim> _claims, ArrayList<SSI_Proof> _proofs, int iconID, long _credentialSubject, String termsOfUse, String _context, String _issuer, Instant _issuance, Instant _expires) {
        this._claims = _claims;
        this._proofs = _proofs;
        this._iconID = iconID;
        this._credentialSubject = _credentialSubject;
        this._termsOfUse = termsOfUse;
        this._context = _context;
        this._issuer = _issuer;
        this._issuance = _issuance;
        this._expires = _expires;
    }

    /** creates a representation from a json
     *
     * @param json  a SSI_Rep in JSON format
     * @throws JSONException thrown when the string is missing SSI_Rep required field data
     */
    public SSI_Representation(String json) throws JSONException {
        if (json == null) throw new JSONException("SSI_Representation JSON is empty (NULL)");
        // Deserialize json into object fields
        JSONObject jo = new JSONObject(json);
        this._credentialSubject = jo.getLong("_credentialSubject");
        this._context = jo.getString("_context");

        // icons is optional
        if ( jo.has("_iconID") ){
            this._iconID = jo.getInt("_iconID");
        } else this._iconID = -1;

        // terms are optional
        if ( jo.has("_termsOfUse") ){
            this._termsOfUse = jo.getString("_termsOfUse");
        } else this._termsOfUse = null;

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

        if ( jo.has("_expires") ){
            String expiration = jo.getString("_expires");
            if (expiration.equals("{}")){
                this._expires = null;
            } else {
                this._expires = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").parse( expiration, Instant::from);
            }
        } else {
            this._expires = null;
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

    /** create a representation from a vc and include termsofuse
     *
     * @param vc            credential that should be turned into rep 1:1
     * @param termsOfUse    terms of use to restrict using the rep
     */
    public SSI_Representation(SSI_VerifiableCredential vc, String termsOfUse){
        this._claims = vc.get_claims();
        this._proofs = vc.get_proofs();
        this._iconID = vc.get_iconID();
        this._credentialSubject = vc.get_credentialSubject();
        this._context = vc.get_context();
        this._issuer = vc.get_issuer();
        this._issuance = vc.get_issuance();
        this._expires = vc.get_expires();
        this._termsOfUse = termsOfUse;
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

    public int get_iconID() {
        return _iconID;
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
        return _expires;
    }

    public String get_termsOfUse() {
        return _termsOfUse;
    }

    /**
     * DEMO we are just checking predetermined SHA256s
     * TRUE IFF all claims can be proven using the proofs, checking them against the authority
     *
     * @return  TRUE IFF all claims can be proven using all proofs
     */
    public boolean isValid() {
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
     * @return  a picture ID we have a claim 'picture id ID', otherwise -1
     */
    public int get_picture() {
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
}
