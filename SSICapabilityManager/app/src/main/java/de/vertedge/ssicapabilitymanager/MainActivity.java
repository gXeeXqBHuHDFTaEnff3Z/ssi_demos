package de.vertedge.ssicapabilitymanager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.digest.DigestUtils;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.vertedge.ssicapabilitymanager.SSI.SSI_Claim;
import de.vertedge.ssicapabilitymanager.SSI.SSI_Representation;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.CapMgmt_Database;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Capability;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Joblisting;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Message;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Organisation;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.User;
import de.vertedge.ssicapabilitymanager.databinding.ActivityMainBinding;
import de.vertedge.ssicapabilitymanager.ui.home.FragmentMessages;

public class MainActivity extends AppCompatActivity {

    public interface OnPermitDenyListener {
        void OnDenyClick(long uid);

        void OnGotoSigningClick(long uid);
    }

    final private String EXTRA_APPNAME = "EXTRA_APPNAME";
    final private String EXTRA_ACTION = "EXTRA_ACTION";
    final private String EXTRA_FIRSTNAME = "EXTRA_FIRSTNAME";
    final private String EXTRA_LASTNAME = "EXTRA_LASTNAME";
    final private String EXTRA_CLAIMS = "EXTRA_CLAIM";
    final private String EXTRA_FROM = "EXTRA_FROM";
    final private String EXTRA_TO = "EXTRA_TO";
    final private String EXTRA_SIGNATURE = "EXTRA_SIGNATURE";
    final private String EXTRA_ID = "EXTRA_ID";
    final private String EXTRA_DID = "EXTRA_DID";
    final private String EXTRA_REP_LIST = "EXTRA_REP_LIST";
    final private String EXTRA_REP_JSON = "EXTRA_REP_JSON";

    final private int _requestCodeLogin = 512;
    final private int _requestCodeSigning = 256;
    final private int _requestCodeSelectVCs = 128;
    private CapMgmt_Database db;

    private User _currentUser;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private final List<OnPermitDenyListener> _listeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        String newmsg = getString(R.string.new_message);
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, newmsg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // get the database instance and daos
        db = CapMgmt_Database.getInstance(getApplicationContext());
        Message.SSIM_Dao msgDao = db.messagesDao();
        Capability.Cap_Dao capDao = db.capDao();
        Joblisting.Jobs_Dao jobsDao = db.jobsDao();
        Organisation.Org_Dao orgsDao = db.orgsDao();

        // first launch, so go get login
        // intent to use ssi
        // Build the intent.
        Intent ssiintent = new Intent(Intent.ACTION_ASSIST);
        ssiintent.setType("application/ssi");

        // add variables to intent
        Bundle extras = new Bundle();
        extras.putString(EXTRA_APPNAME, getApplicationName(this));
        extras.putString(EXTRA_ACTION, "LOGIN");
        extras.putString(EXTRA_SIGNATURE, DigestUtils.sha256Hex(getApplicationName(this)));
        ssiintent.putExtras(extras);

        // Try to invoke the intent.
        try {
            startActivityForResult(ssiintent, _requestCodeLogin);
        } catch (ActivityNotFoundException en) {
            // Define what your app should do if no activity can handle the intent.
            Log.e("SSI", "Could not find SSI request intent, SSI Wallet not installed");
            String errmsg = getString(R.string.exception_wallet_not_installed);
            Toast.makeText(this, errmsg, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public User get_currentUser() {
        return _currentUser;
    }

    // receive login and handle it
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CapMgmt_Database db = CapMgmt_Database.getInstance(getApplicationContext());
        Log.i(this.toString(), "Received reply with requestCode " + requestCode + " and result code " + resultCode);

        if (requestCode == _requestCodeLogin) {
            if (resultCode == RESULT_OK) {
                Bundle _data = data.getExtras();
                // Handle login from wallet app
                String _firstname = _data.getString(EXTRA_FIRSTNAME);
                String _lastname = _data.getString(EXTRA_LASTNAME);
                String _claim = _data.getString(EXTRA_CLAIMS);
                SSI_Representation vcrep = null;

                if ( data.hasExtra(EXTRA_REP_JSON) )
                try {
                   vcrep = new SSI_Representation( _data.getString(EXTRA_REP_JSON) );
                } catch (JSONException e) {
                    e.printStackTrace();
                    vcrep = null;
                }

                Log.i(this.toString(), "trying to LOGIN " + _firstname + " " + _lastname + " with claim " + _claim);

                // try to find user by claim we have. first the old claim, then by rep
                _currentUser = db.usersDao().findByDID(_claim);
                if ( (_currentUser == null) && (vcrep != null) )
                    for (SSI_Claim claim : vcrep.get_claims()){
                        _currentUser = db.usersDao().findByDID(claim.get_value());
                        if (_currentUser != null) break;
                    }

                int _picture = R.drawable.ic_certificate;
                if (_currentUser != null) {
                    Log.i(this.toString(), "Found user for rep " + vcrep);
                    _picture = _currentUser.get_picture();
                } else {
                    Log.e(this.toString(), "Could not find user for claim " + _claim);
                    String message = getResources().getString(R.string.login_unknown_user, _claim);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                String _signature = _data.getString(EXTRA_SIGNATURE);
                View headerView = binding.navView.getHeaderView(0);
                TextView tVusername = (TextView) headerView.findViewById(R.id.tVnav_header_User);
                TextView tVuser_info = (TextView) headerView.findViewById(R.id.tV_nav_header_info);
                ImageView nav_pic = (ImageView) headerView.findViewById(R.id.imgVnav_header_pic);
                tVusername.setText(_firstname + " " + _lastname);
                tVuser_info.setText(_currentUser.get_ssidid());
                Log.i(this.toString(), "Received intent with requestCode " + requestCode + ", lastname " + _lastname + ", claim " + _currentUser + ", signature: " + _signature);

                // find out if we should display the jobs
                boolean jobsVisibility = true;
                if ((_currentUser != null) && (!_currentUser.is_roleCompOwner()))
                    jobsVisibility = false;
                NavigationView navigationView;
                navigationView = (NavigationView) findViewById(R.id.nav_view);
                Menu nav_Menu = navigationView.getMenu();
                nav_Menu.findItem(R.id.nav_gallery).setVisible(jobsVisibility);
                nav_pic.setImageDrawable(AppCompatResources.getDrawable(this, _picture));
            } else finish();
        } else if (requestCode == _requestCodeSigning) {
            // this is a reply to signing, check if it was signed
            if (resultCode == RESULT_OK) {
                // user signed a capability, send new message to user and delete original request
                Bundle _data = data.getExtras();
                // Handle signing from wallet app
                String _firstname = _data.getString(EXTRA_FIRSTNAME);
                String _lastname = _data.getString(EXTRA_LASTNAME);
                String _signature = _data.getString(EXTRA_SIGNATURE);
                String _claims = _data.getString(EXTRA_CLAIMS);
                SSI_Representation _rep = null;
                try {
                    _rep = new SSI_Representation(_data.getString(EXTRA_REP_JSON));
                } catch (JSONException e) {
                    Log.e("EXCEPTION", "Representation not in expected JSON format");
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Representation not in expected JSON format", Toast.LENGTH_SHORT).show();
                    finish();
                }
                long _receivedID = _data.getLong(EXTRA_ID);

                Log.i(this.toString(), "Received intent with requestCode " + requestCode + ", id " + _receivedID + ", lastname " + _lastname + ", claims " + _claims);

                // create new message for the requesting user with his new signed cap
                ArrayList<Long> emptycaps = new ArrayList<>();
                ArrayList<Long> signedcaps = new ArrayList<>();

                Message currentMsg = db.messagesDao().get(_receivedID);

                // we need to look for the first claim to find it as a capability
                /*String[] claims = _claims.split(",");
                long capWithClaimUID = db.capDao().getByName(claims[0]).get_uid();
                signedcaps.add(capWithClaimUID);
                 */

                // take the representation and send it to the user as a message
                String _text = getResources().getString(R.string.message_to_student);

                Message yourSignedCap = new Message(_text,
                        currentMsg.get_to_user(),
                        currentMsg.get_from_user(),
                        emptycaps,
                        signedcaps,
                        _signature,
                        true,
                        false,
                        _rep.toJSON());
                db.messagesDao().insert(yourSignedCap);

                // inform user
                String msg = getString(R.string.signed_reply, yourSignedCap.get_to_user());
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                // delete original request
                db.messagesDao().delete(currentMsg);
                finish();
            }
        }
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public void registerListener(OnPermitDenyListener listener) {
        _listeners.add(listener);
    }

    // tell listeners about deny click
    public void onDenyClick(View v) {
        long position = (long) v.getTag();
        for (OnPermitDenyListener listener : _listeners) {
            listener.OnDenyClick(position);
        }
    }

    public void onOKClick(View v) {
        // if user clicks goto ssi app, then find out about message intention
        String action;
        String claim = null;
        String signature;
        String ssidid = null;
        String representation;
        long uid = (long) v.getTag();
        Message message = db.messagesDao().get(uid);
        Capability capability;

        // if message is a job application, we want to verify the claims
        if (message.is_isJobApplication()){
            Log.i(this.toString(), "Message " + message.get_uid() + " is  a job application");
            action = "DETAILS";
            claim = db.capDao().get(message.get_signedCapabilities().get(0)).get_name();
            representation = message.get_attachedRep();
            signature = message.get_signature();
        } else
        // if message has a signed attachment but is no jobapplication, then this is for putting it in ssi app
        if (message.get_signature() != null) {
            Log.i(this.toString(), "Message " + message.get_uid() + " has signed attachment");
            action = "ADDING:VC";
            //claim = db.capDao().get(message.get_signedCapabilities().get(0)).get_name();
            representation = message.get_attachedRep();
            signature = message.get_signature();
            ssidid = message.get_to_user();
        } else {
            Log.i(this.toString(), "Message " + message.get_uid() + " has unsigned attachment: " + message.get_text());
            // message has no signed attachment, this is for signing one
            action = "SIGNING";
            // TODO this does only get the first cap, not all of them
            capability = db.capDao().get(message.get_unsignedCapabilities().get(0));
            representation = message.get_attachedRep();
            claim = capability.get_name();

            signature = DigestUtils.sha256Hex(message.toString());
        }

        // create intent
        Intent ssiintent = new Intent(Intent.ACTION_ASSIST);
        ssiintent.setType("application/ssi");

        // add variables to intent
        Log.i(this.toString(), "Starting Wallet app with intent for action " + action + ", claim " + claim + " and signature " + signature);
        Bundle extras = new Bundle();
        extras.putString(EXTRA_APPNAME, getApplicationName(this));
        extras.putString(EXTRA_ACTION, action);
        extras.putString(EXTRA_FROM, message.get_from_user());
        extras.putString(EXTRA_SIGNATURE, signature);
        extras.putString(EXTRA_CLAIMS, claim);
        extras.putString(EXTRA_REP_JSON, representation);
        extras.putLong(EXTRA_ID, message.get_uid());
        extras.putString(EXTRA_DID, ssidid);
        ssiintent.putExtras(extras);

        // Try to invoke the intent.
        try {
            startActivityForResult(ssiintent, _requestCodeSigning);
        } catch (ActivityNotFoundException en) {
            // Define what your app should do if no activity can handle the intent.
            Log.e("SSI", "Could not find SSI request intent, SSI Wallet not installed");

            String errmsg = getString(R.string.exception_wallet_not_installed);
            Toast.makeText(this, errmsg, Toast.LENGTH_LONG);
            finish();
        }
    }
}