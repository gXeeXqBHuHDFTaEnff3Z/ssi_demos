package de.vertedge.ssicapabilitymanager.ui.gallery;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.vertedge.ssicapabilitymanager.MainActivity;
import de.vertedge.ssicapabilitymanager.R;
import de.vertedge.ssicapabilitymanager.SSI.SSI_Claim;
import de.vertedge.ssicapabilitymanager.SSI.SSI_Representation;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.CapMgmt_Database;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Capability;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Joblisting;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Message;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Organisation;
import de.vertedge.ssicapabilitymanager.databinding.FragmentJobsBinding;
import de.vertedge.ssicapabilitymanager.ui.home.RecyclerView_Messages_Adapter;
import kotlinx.coroutines.Job;

public class FragmentJobs extends Fragment implements RecyclerView_Jobs_Adapter.ItemClickListener, RecyclerView_Jobs_Adapter.OnDataChangedListener {



    final private String EXTRA_APPNAME = "EXTRA_APPNAME";
    final private String EXTRA_ACTION = "EXTRA_ACTION";
    final private String EXTRA_DID = "EXTRA_DID";
    final private String EXTRA_REP_LIST = "EXTRA_REP_LIST";
    final private String EXTRA_SIGNATURE = "EXTRA_SIGNATURE";
    final private String EXTRA_REP_JSON = "EXTRA_REP_JSON";
    final private int _requestCodeSelectVCs = 128;

    private GalleryViewModel galleryViewModel;
    private FragmentJobsBinding binding;
    private MainActivity main;
    private RecyclerView_Jobs_Adapter adapter;
    private Switch switchFilter;
    private RecyclerView rcView;

    private List<Long> _capabilities;
    private SSI_Representation _rep;
    private String _signature;
    private CapMgmt_Database db = CapMgmt_Database.getInstance(getContext());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentJobsBinding.inflate(inflater, container, false);
        final View root = binding.getRoot();

        main = (MainActivity) getActivity();
        switchFilter = root.findViewById(R.id.tvRecyclerMsg_CapsChecked);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Context context = binding.getRoot().getContext();

        rcView = binding.recVJobs;
        LinearLayoutManager _layout = new LinearLayoutManager( context );
        rcView.setLayoutManager( _layout );

        List<Joblisting> joblistings = db.jobsDao().getAll();

        if (adapter == null){
            adapter = new RecyclerView_Jobs_Adapter(main, joblistings, this );
            adapter.setClickListener( this::onItemClick );
            rcView.setAdapter(adapter);
            _capabilities = null;
        }

        switchFilter = binding.getRoot().findViewById(R.id.jobs_filter_switch);

        switchFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // replace the adapter when the filter gets changed
            if ( isChecked ){
                // send the user to wallet app to select his signed VCs
                startWalletForCapabilities();

            } else {
                // no filter, show all jobs
                ArrayList<Joblisting> jobs = new ArrayList<>(joblistings);
                Log.i(this.toString(), "generating new adapter with " + jobs.size() + " jobs");
                adapter = new RecyclerView_Jobs_Adapter(main, jobs , this);
                rcView.setAdapter(adapter);
                _capabilities = null;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void startWalletForCapabilities(){
        // create intent
        Log.d("YOU ARE", "HERE!");
        Intent ssiintent = new Intent(Intent.ACTION_ASSIST);
        ssiintent.setType("application/ssi");
        String action = "VCSELECT";
        assert main.get_currentUser() != null;
        String ssidid = main.get_currentUser().get_ssidid();

        // add variables to intent
        Log.i(this.toString(), "Starting Wallet app with intent for action " + action + " by user " + ssidid);
        Bundle extras = new Bundle();
        extras.putString(EXTRA_APPNAME, getApplicationName(main));
        extras.putString(EXTRA_ACTION, action);
        extras.putString(EXTRA_DID, ssidid);
        ssiintent.putExtras(extras);

        // Try to invoke the intent.
        try {
            startActivityForResult(ssiintent, _requestCodeSelectVCs);
        } catch (ActivityNotFoundException en) {
            // Define what app should do if no activity can handle the intent.
            Log.e("SSI", "Could not find SSI request intent, SSI Wallet not installed");
            Toast.makeText(getContext(), "Could not find SSI Wallet App, please install Wallet first", Toast.LENGTH_LONG).show();
            main.finish();
        }

    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == _requestCodeSelectVCs) {
            // this is a list of supposedly signed vcs for a job search
            CapMgmt_Database db = CapMgmt_Database.getInstance( getContext() );
            Bundle _data = data.getExtras();

            // get list of vcs and turn them into caps list
            ArrayList<String> vcs = null;
            ArrayList<SSI_Representation> _reps = new ArrayList<>();
            if ( data.hasExtra(EXTRA_REP_LIST) ) vcs = _data.getStringArrayList(EXTRA_REP_LIST);
            _signature = _data.getString(EXTRA_SIGNATURE);

            // go through JSON array and look for capability names
            ArrayList<Capability> caps = new ArrayList<>();
            for (String vcJSON : vcs) {
                SSI_Representation rep;
                Capability cap = null;

                try {
                    rep = new SSI_Representation( vcJSON );
                } catch (JSONException e) {
                    // JSON has wrong format
                    String msg = getString(R.string.exception_unknown_capability);
                    Log.e("Wrong competence format", vcJSON);
                    e.printStackTrace();
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    Log.e(this.toString(), msg);
                    main.getFragmentManager().popBackStack();
                    return;
                }

                // go thorugh all claims and try to find one that is a cap
                for (SSI_Claim claim : rep.get_claims()){
                    cap = db.capDao().getByName( claim.get_value() );
                    _rep = rep;
                    if (cap != null) break;
                }

                if (cap == null) {
                    String msg = getString(R.string.exception_unknown_capability);
                    Log.e("Could not find valid competence", vcJSON);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    Log.e(this.toString(), msg);
                    main.getFragmentManager().popBackStack();
                    return;
                }
                caps.add(cap);
            }

            // get list of uids for capabilities we got from the signed VCs
            ArrayList<Long> signedCapabilities = new ArrayList<>();
            for (Capability cap : caps){
                signedCapabilities.add( cap.get_uid() );
            }

            // go through all jobs and filter out the ones not fitting the list of signed caps
            List<Joblisting> alljobs = db.jobsDao().getAll();
            List<Joblisting> matchingjobs = new ArrayList<>();
            for (Joblisting job : alljobs){
                if (Capability.requirementsFullfilled(job.getMinRequirements(), signedCapabilities) ){
                    matchingjobs.add(job);
                    Log.i(this.toString(), "job " + job.get_uid() + " fulfills requirements, added to list");
                }
            }

            Log.i(this.toString(), "filtering jobs for wallet cap list, size " + matchingjobs.size());
            _capabilities = signedCapabilities;
            adapter = new RecyclerView_Jobs_Adapter(getContext(), matchingjobs, this );

            final RecyclerView recyclerView = binding.recVJobs;
            recyclerView.setAdapter(adapter);
            adapter.setClickListener( this::onItemClick );
        }
    }

    // click on joblisting, probably wants to apply for job
    @Override
    public void onItemClick(View view, int position) {

        // get necessary data
        CapMgmt_Database db = CapMgmt_Database.getInstance( getContext() );
        Joblisting joblisting = adapter.getItem( position );
        Long companyID = joblisting.get_company_id();
        Organisation org = db.orgsDao().get( companyID );

        ArrayList<Long> signedCapabilities = (ArrayList<Long>) _capabilities;

        // TODO are there attached SSI reps? ask user if he wants to apply emptyhanded

        // send message
        String messageToJob = getResources().getString(R.string.message_to_fab, org.get_name(), joblisting.get_uid() + "");

        Message message = new Message(messageToJob,
                main.get_currentUser().get_ssidid(),
                org.get_ssidid(),
                null,
                signedCapabilities,
                _signature,
                true,
                true,
                _rep.toJSON());
        db.messagesDao().insert(message);

        // tell user
        String messageToUser = getResources().getString(R.string.job_applied, org.get_name());
        Toast.makeText(getContext(), messageToUser, Toast.LENGTH_LONG).show();
    }

    @Override
    public void OnDataChanged() {
        Context context = binding.getRoot().getContext();
        List<Joblisting> joblistings = db.jobsDao().getAll();
        adapter = new RecyclerView_Jobs_Adapter(main, joblistings, this );
        adapter.setClickListener( this::onItemClick );
        rcView.setAdapter(adapter);
    }
}