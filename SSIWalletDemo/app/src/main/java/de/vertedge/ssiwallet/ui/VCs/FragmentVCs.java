package de.vertedge.ssiwallet.ui.VCs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import de.vertedge.ssiwallet.MainActivity;
import de.vertedge.ssiwallet.R;
import de.vertedge.ssiwallet.data.SSI.SSI_Database;
import de.vertedge.ssiwallet.data.SSI.SSI_Identity;
import de.vertedge.ssiwallet.data.SSI.SSI_VerifiableCredential;
import de.vertedge.ssiwallet.databinding.FragmentVcsBinding;

public class FragmentVCs extends Fragment implements AdapterView.OnItemSelectedListener {

    private ViewModelVCs viewModelVCs;
    private FragmentVcsBinding binding;
    private MainActivity main;
    private SSI_Database db;

    private RecyclerView_VCs_Adapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModelVCs =
                new ViewModelProvider(this).get(ViewModelVCs.class);

        binding = FragmentVcsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        main = (MainActivity) getActivity();
        db = SSI_Database.getInstance(getContext());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // update spinner selection
        Spinner idSelection = (Spinner) getView().findViewById(R.id.spinnerIdentities);
        idSelection.setOnItemSelectedListener(this);

        List<SSI_Identity> identities = db.identityDao().getAll();
        String[] spinnerSelection = new String[identities.size()+1];
        spinnerSelection[0] = "(" + getString( R.string.all_identities ) + ")";

        for (int i = 0; i<identities.size(); i++){
            SSI_Identity identity = identities.get(i);
            String itemName = String.format("%s (%s)",
                identity.getFullName(),
                identity.get_birthday());
            spinnerSelection[i+1] = itemName;
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                main,
                android.R.layout.simple_spinner_item,
                spinnerSelection);
        spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        idSelection.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        Context context = binding.getRoot().getContext();

        final RecyclerView recyclerView = binding.rvIdentities;

        // create grid layout with the approprioate number of recycler columns
        final int columns = getResources().getInteger(R.integer.recyclerview_columns);
        GridLayoutManager _layout = new GridLayoutManager( context, columns );
        recyclerView.setLayoutManager( _layout );

        List<SSI_VerifiableCredential> list = db.vcDao().getAll();
        adapter = new RecyclerView_VCs_Adapter(context, list );
        // TODO adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // An id spinner item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        final RecyclerView recyclerView = binding.rvIdentities;
        final List<SSI_VerifiableCredential> list = db.vcDao().getAll();

        if (i == 0){
            adapter = new RecyclerView_VCs_Adapter(getContext(), list );
        } else {
            List<SSI_VerifiableCredential> VCs_where_id_fits = new ArrayList<>();
            for (SSI_VerifiableCredential vc : list){
                if (vc.get_credentialSubject() == i) VCs_where_id_fits.add( vc );
            }
            adapter = new RecyclerView_VCs_Adapter(getContext(), VCs_where_id_fits);
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }
}