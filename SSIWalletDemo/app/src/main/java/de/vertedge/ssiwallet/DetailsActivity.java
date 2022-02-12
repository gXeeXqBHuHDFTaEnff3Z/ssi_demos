package de.vertedge.ssiwallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.vertedge.ssiwallet.data.SSI.SSI_Authority;
import de.vertedge.ssiwallet.data.SSI.SSI_Claim;
import de.vertedge.ssiwallet.data.SSI.SSI_Database;
import de.vertedge.ssiwallet.data.SSI.SSI_Identity;
import de.vertedge.ssiwallet.data.SSI.SSI_Proof;
import de.vertedge.ssiwallet.data.SSI.SSI_Representation;

public class DetailsActivity extends AppCompatActivity {

    final private String EXTRA_APPNAME = "EXTRA_APPNAME";
    final private String EXTRA_ACTION = "EXTRA_ACTION";
    final private String EXTRA_SIGNATURE = "EXTRA_SIGNATURE";
    final private String EXTRA_DID = "EXTRA_DID";
    final private String EXTRA_REP_JSON = "EXTRA_REP_JSON";

    private String _firstname, _lastname, _receivedDID;
    private String _rep, _signature;
    private String _action, _appname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if ((Intent.ACTION_ASSIST.equals(action)) && type != null) {
            if ("application/ssi".equals(type)) {
                // find out request type
                Bundle extras = intent.getExtras();
                _appname = extras.getString(EXTRA_APPNAME);
                _action = extras.getString(EXTRA_ACTION);
                _rep = extras.getString(EXTRA_REP_JSON);
                _signature = extras.getString(EXTRA_SIGNATURE);
                _receivedDID = extras.getString(EXTRA_DID);

                // Logging
                Log.d(this.toString(), "Received intent of type " + type + ", with action " + _action + ", claim " + _rep + ", DID " + _receivedDID);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("SSIW Details", "onResume with action " + _action + ", and claim " + _rep);

        SSI_Database db = SSI_Database.getInstance(getApplicationContext());

        final TextView tvClaim = findViewById(R.id.det_tv_claim);
        final TextView tvSignature = findViewById(R.id.det_tv_proofcheck);
        final TextView tvCheck = findViewById(R.id.det_tv_verification);
        final TextView tvTermsOfUse = findViewById(R.id.det_tv_termsofUse);

        assert this.getReferrer() != null;
        // get sending app and icon
        String host = this.getReferrer().getHost();
        String authorityName = null;

        // get representation
        SSI_Representation rep = null;
        try {
            rep = new SSI_Representation( _rep );
        } catch (JSONException e) {
            // JSON has wrong format
            String msg = getString(R.string.excpetion_rep_format);
            Log.e(msg, _rep);
            e.printStackTrace();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            Log.e(this.toString(), msg);
            return;
        }

        // get list of authoritites
        ArrayList<SSI_Authority> ssi_authorities = new ArrayList<>(db.ssiAuthorityDao().getAll());

        // try to validate against authorities
        boolean signatureValid = false;
        for (SSI_Authority ssi_authority : ssi_authorities){
            if (rep.isValid()){
                signatureValid = true;
                authorityName = ssi_authority.getName();
                break;
            }
        }

        // if no authority did not sign this, maybe one of our ids?
        if (!signatureValid){
            List<SSI_Identity> identities = db.identityDao().getAll();
            for (SSI_Identity identity : identities){
                if ( rep.isValid() ) {
                    signatureValid = true;
                    authorityName = identity.getFullName();
                }
            }
        }

        // construct text representation for claims
        String claims = "";
        for (SSI_Claim claim : rep.get_claims()){
            claims = "•" + claim.toString() + "\n";
        }

        // construct text representation for signatures
        String signatures = "";
        for (SSI_Proof proof : rep.get_proofs()){
            signatures = "•" + proof.toString() + "\n";
        }

        // construct text representation for terms
        String sourceTerms = rep.get_termsOfUse();

        if (sourceTerms != null) {
            sourceTerms = sourceTerms.replace("\u003d", "=").replace("\u0027", "'");
        }

        String terms = "";

        if (sourceTerms != null){
            if (sourceTerms.contains("noArchive=" + true)){
                terms = terms + "• " + getString(R.string.termsOfUse_noArchive) + "\n";
            }

            if (sourceTerms.contains("target=")){
                terms = terms + "• " +getString(R.string.termsOfUser_target,
                        sourceTerms.substring(sourceTerms.indexOf("target=") + 8, sourceTerms.lastIndexOf("'") ) + "\n");
            }
        }

        tvClaim.setText( claims );
        tvClaim.setOnClickListener(view -> {
            // copy raw rep to clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("application/ssi", _rep);
            clipboard.setPrimaryClip(clip);
            // show toast
            String msg = getString(R.string.vc) + " " + getString(R.string.copied_to_clipbaord);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        });

        tvSignature.setText( signatures );
        tvSignature.setOnClickListener(view -> {
            // copy signature to clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("application/ssi", _signature);
            clipboard.setPrimaryClip(clip);
            // show toast
            String msg = getString(R.string.copied_to_clipbaord) + ": " + _signature;
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        });

        tvCheck.setText( signatureValid ? getText(R.string.valid) + ": " + authorityName : getText(R.string.invalid));

        tvTermsOfUse.setText( terms );
    }
}