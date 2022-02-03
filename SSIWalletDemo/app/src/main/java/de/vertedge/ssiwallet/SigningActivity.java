package de.vertedge.ssiwallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import de.vertedge.ssiwallet.data.SSI.SSI_Authority;
import de.vertedge.ssiwallet.data.SSI.SSI_Claim;
import de.vertedge.ssiwallet.data.SSI.SSI_Database;
import de.vertedge.ssiwallet.data.SSI.SSI_Identity;
import de.vertedge.ssiwallet.data.SSI.SSI_Proof;
import de.vertedge.ssiwallet.data.SSI.SSI_Representation;
import de.vertedge.ssiwallet.data.SSI.SSI_Signature;
import de.vertedge.ssiwallet.data.SSI.SSI_VerifiableCredential;
import de.vertedge.ssiwallet.ui.VCs.RecyclerView_VCs_Adapter;

public class SigningActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // ui
    private TextView tVexplanation;
    private TextView tVclaim;
    private Spinner idSelection;
    private Button bOK;
    private ImageView actionIcon;
    private ImageView appIcon;
    private LinearLayout layoutFromApp;
    private RecyclerView rvMultiSelect;
    private RecyclerView_VCs_Adapter viewVCsAdapter;

    // data model
    SSI_Database db;
    SSI_Identity.SSI_Identity_Dao identDao;
    SSI_VerifiableCredential.SSI_VC_Dao vcDao;
    SSI_Signature.SSI_Signature_Dao ssigDao;
    private List<SSI_Identity> _identities;
    private List<SSI_VerifiableCredential> _credentials;
    private List<SSI_Signature> _signatures;
    private SSI_Representation _rep;

    // received data
    String _appname;
    String _action;
    //String _claim;
    String _from;
    String _signature;
    long _receivedID;
    String _receivedDID;

    // selection
    int selectedIdentity = 0;

    // reply intent
    final private String EXTRA_APPNAME = "EXTRA_APPNAME";
    final private String EXTRA_ACTION = "EXTRA_ACTION";
    final private String EXTRA_FIRSTNAME = "EXTRA_FIRSTNAME";
    final private String EXTRA_LASTNAME = "EXTRA_LASTNAME";
    final private String EXTRA_FROM = "EXTRA_FROM";
    final private String EXTRA_TO = "EXTRA_TO";
    final private String EXTRA_SIGNATURE = "EXTRA_SIGNATURE";
    final private String EXTRA_ID = "EXTRA_ID";
    final private String EXTRA_DID = "EXTRA_DID";
    final private String EXTRA_REP_LIST = "EXTRA_REP_LIST";
    final private String EXTRA_REP_JSON = "EXTRA_REP_JSON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);

        tVexplanation = findViewById(R.id.tVsigning_explanation);
        tVclaim = findViewById(R.id.tVsigning_claim);
        bOK = findViewById(R.id.bSigning_Permit);
        actionIcon = findViewById(R.id.imgVsigning_action);
        appIcon = findViewById(R.id.imgVsigningappicon);
        layoutFromApp = findViewById(R.id.layout_login_from_app);
        rvMultiSelect = findViewById(R.id.rvSigning);
        tVexplanation.setText(R.string.signing_explanation_default);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if ((Intent.ACTION_ASSIST.equals(action)) && type != null) {
            if ("application/ssi".equals(type)) {
                // find out request type
                Bundle extras = intent.getExtras();
                _appname = extras.getString(EXTRA_APPNAME);
                _action = extras.getString("EXTRA_ACTION");
                _from = extras.getString(EXTRA_FROM);
                //_claim = extras.getString("EXTRA_CLAIM");
                _signature = extras.getString("EXTRA_SIGNATURE");
                _receivedID = extras.getLong(EXTRA_ID);
                _receivedDID = extras.getString(EXTRA_DID);
                if ( intent.hasExtra(EXTRA_REP_JSON) )
                try {
                    _rep = new SSI_Representation( extras.getString(EXTRA_REP_JSON) );
                } catch (JSONException e) {
                    Log.e("EXCEPTION", "Representation not in expected JSON format");
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Representation not in expected JSON format", Toast.LENGTH_SHORT).show();
                    finish();
                }

                // Logging
                Log.i(this.toString(), "Received intent of type " + type + ", with action " + _action + ", from " + _from + ", DID " + _receivedDID + ", signature " + _signature);

                // ACTION AUSWERTEN erfolgt in onResume
            } else {
                Log.e("SSI", "unintended intent type");
                finish();
            }
        } else {
            Log.e("SSI", "unintended intent action");
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("SSI Wallet", "onResume with action " + _action);

        assert this.getReferrer() != null;
        // get sending app and icon
        String host = this.getReferrer().getHost();
        String _explanation;
        tVexplanation.setOnClickListener(v -> Toast.makeText(getApplicationContext(), host, Toast.LENGTH_LONG).show());

        try {
            Drawable icon = getPackageManager().getApplicationIcon(host);
            appIcon.setImageDrawable(icon);
            layoutFromApp.setVisibility(View.VISIBLE);
        } catch (PackageManager.NameNotFoundException e) {
            layoutFromApp.setVisibility(View.GONE);
            e.printStackTrace();
        }

        if (_action.contains("LOGIN")) {
            // login view
            actionIcon.setImageResource(R.drawable.ic_identity);
            _explanation = getString(R.string.signing_explanation_login, _appname);
            tVexplanation.setText( _explanation );
            tVclaim.setVisibility(View.GONE);
            bOK.setText(getResources().getString(R.string.login, _appname));
            bOK.setTag(null);
        } else if (_action.contains("SIGNING")) {
            // signing view
            actionIcon.setImageResource(R.drawable.ic_sign);
            tVexplanation.setText(getResources().getString(R.string.signing_explanation_default, _from));
            tVclaim.setText(_rep.get_claim());
            tVclaim.setVisibility(View.VISIBLE);
            bOK.setText(getResources().getString(R.string.signing_allow));
            bOK.setTag(_rep);
        } else if (_action.contains("ADDING:VC")) {
            // adding vc from external app

            SSI_Database db = SSI_Database.getInstance(getApplicationContext());
            // first find out for which ssi user
            String fname = SSI_Identity.SSI_DID_Helper.FirstNameFromDID(_receivedDID);
            String lname = SSI_Identity.SSI_DID_Helper.LastNameFromDID(_receivedDID);

            // try to find fitting identity by did
            SSI_Identity foruser = db.identityDao().findByName(fname, lname);

            // try to find fitting vc by did
            String claimed = _receivedDID.replace("ssi:", "");
            long vcid = db.findVCbyClaim(this, claimed);
            SSI_VerifiableCredential forvc = null;
            if (vcid != -1){
                forvc = db.vcDao().get(vcid);
            }

            // if you found no user but a vc, use the user from the vc
            if ((foruser == null) && (forvc != null)) {
                long uid = forvc.get_credentialSubject();
                foruser = db.identityDao().findByUID(uid);
            }

            // if we found a user then connect to him, else create without userconnection
            SSI_VerifiableCredential newvc;
            if (foruser != null) {
                _rep.set_credentialSubject(foruser.get_uid());
            }
            newvc = new SSI_VerifiableCredential( _rep );

            db.vcDao().insert(newvc);
            Toast.makeText(this, getString(R.string.added_vc, newvc.get_claim()), Toast.LENGTH_LONG).show();
            finish();
        } else if (_action.contains("VCSELECT")) {
            // we want to send back multiple vcs. show them all for selection
            SSI_Database db = SSI_Database.getInstance(getApplicationContext());

            // create the recycler adapter we need for all vcs
            LinearLayoutManager _layout = new LinearLayoutManager(this);
            rvMultiSelect.setLayoutManager(_layout);
            viewVCsAdapter = new RecyclerView_VCs_Adapter(this, db.vcDao().getAll());
            rvMultiSelect.setAdapter(viewVCsAdapter);

            // NOW DO ALL THE VIEWS
            tVexplanation.setText(getString(R.string.signing_explanation_multiselect, _appname));
            tVclaim.setVisibility(View.GONE);
            rvMultiSelect.setVisibility(View.VISIBLE);
            bOK.setText(getString(R.string.send));
            return;
        } else if (_action.contains("DETAILS")) {
            Intent details = new Intent(this, DetailsActivity.class);
            details.setAction(Intent.ACTION_ASSIST);
            details.setType("application/ssi");
            details.putExtra(EXTRA_REP_JSON, _rep.toJSON());
            startActivity(details);
            finish();
        } else
            throw new IllegalArgumentException("unsupported SSI action: " + _action + " from " + _appname);

        // update spinner selection
        idSelection = (Spinner) findViewById(R.id.spinnerSigning_selectID);
        idSelection.setOnItemSelectedListener(this);

        // GET ID DATABASE
        db = SSI_Database.getInstance(getApplicationContext());
        identDao = db.identityDao();
        vcDao = db.vcDao();
        ssigDao = db.ssigDao();

        _identities = identDao.getAll();
        _credentials = vcDao.getAll();
        _signatures = ssigDao.getAll();

        // spinner selection is all the identities
        DateTimeFormatter dtf = DateTimeFormatter
                .ofLocalizedDate( FormatStyle.SHORT )
                .withLocale(getResources().getConfiguration().locale)
                .withZone( ZoneId.systemDefault() );

        String[] spinnerSelection = new String[_identities.size()];
        for (int i = 0; i < _identities.size(); i++) {
            SSI_Identity identity = _identities.get(i);
            String born = (identity.get_birthday() != null ? getResources().getString(R.string.born_short) : "");
            if (!born.isEmpty()) born = "(" + born + " " + dtf.format(identity.get_birthday()) + ")";
            String itemName = String.format("%s %s",
                    identity.getFullName(),
                    born);
            spinnerSelection[i] = itemName;
        }

        // update the spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_ids,
                spinnerSelection);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        idSelection.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // An id spinner item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedIdentity = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void onOKClick(View v) {
        Intent reply = new Intent();
        db = SSI_Database.getInstance(this);

        // find out if this is VCSELECT
        if (viewVCsAdapter != null) {
            // this is VCSELECT, collect the selection
            ArrayList<Long> vcs = viewVCsAdapter.getSelectedVCuids();
            Log.i(this.toString(), "VCSELECT VS uids: " + vcs.stream().map(Object::toString)
                    .collect(Collectors.joining(", ")));

            // convert long to JSON string array
            ArrayList<String> _reps = new ArrayList<>();
            for (long vc : vcs) {
                _reps.add(db.vcDao().get(vc).toJSON());
            }

            // put variables in intent
            //reply.putExtra(EXTRA_DID, _receivedDID);
            reply.putStringArrayListExtra(EXTRA_REP_LIST, _reps);
            if (vcs.size() > 0){
                _signature = db.vcDao().get( vcs.get(0) ).get_proof();
            } else _signature = null;
            reply.putExtra(EXTRA_SIGNATURE, _signature);
            Log.i(this.toString(), "Replying with intent for VCSELECT and vc list " + _reps);
        } else {

            // if this is a single select, get it
            // find identity and its first vc
            SSI_Identity ssi_identity = _identities.get(selectedIdentity);
            String ssidid = ssi_identity.getDID();
            SSI_VerifiableCredential ssi_claim = null;
            for (SSI_VerifiableCredential vc : _credentials) {
                if (vc.get_credentialSubject() == ssi_identity.get_uid()) {
                    ssi_claim = vc;
                    break;
                }
            }

            if (v.getTag() == null) {
                // we are login in, just send the vc the user has
                _rep = (ssi_claim != null ? new SSI_Representation (ssi_claim) : null);
                _signature = (ssi_claim != null ? ssi_claim.get_credentialSubject() + "" : null);
            } else {
                // we are signing, sign the claim and send both
                SSI_Database db = SSI_Database.getInstance(getApplicationContext());
                SSI_Authority authority = db.ssiAuthorityDao().findByName(SSI_Authority.DEFAULT_AUTHORITY);

                _rep = (SSI_Representation) v.getTag();
                _rep.addClaim(ssi_identity.getFullName(), getString(R.string.confirms), _rep.get_claim() );
                // sign all the claims
                for (SSI_Claim claim : _rep.get_claims()){
                    _rep.addProof(SSI_Proof.DEFAULT_PROOF_TYPE, ssi_identity.sign(claim.toString()), authority.toString());
                }
                //_signature = ssi_identity.sign(_claim);
                Log.d("Signed", "Signature is " +_signature);
            }

            // put all infos for the other app in the intent and launch it
            reply.putExtra(EXTRA_FIRSTNAME, ssi_identity.get_firstname());
            reply.putExtra(EXTRA_LASTNAME, ssi_identity.get_lastname());
            if (_rep != null)
            reply.putExtra(EXTRA_REP_JSON, _rep.toJSON());
            reply.putExtra(EXTRA_ID, _receivedID);
            reply.putExtra(EXTRA_DID, ssidid);
            reply.putExtra(EXTRA_SIGNATURE, _signature);
            Log.i(this.toString(), "Replying with intent for lastname " + ssi_identity.get_lastname() + ssidid + "\nSignature: " + _signature);
        }
        setResult(RESULT_OK, reply);
        finish();
    }

    public void onDenyClick(View v) {
        // if user clicks deny, send back a cancel and exit
        Intent reply = new Intent();
        setResult(RESULT_CANCELED, reply);
        finish();
    }
}