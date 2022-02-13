package de.vertedge.ssicapabilitymanager.ui.slideshow;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.vertedge.ssicapabilitymanager.MainActivity;
import de.vertedge.ssicapabilitymanager.R;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.CapMgmt_Database;
import de.vertedge.ssicapabilitymanager.capabilitymgmt.Capability;
import de.vertedge.ssicapabilitymanager.databinding.FragmentCapabilitiesBinding;
import de.vertedge.ssicapabilitymanager.ui.home.RecyclerView_Messages_Adapter;

public class FragmentCapabilities extends Fragment implements AdapterView.OnItemSelectedListener {

    private SlideshowViewModel slideshowViewModel;
    private FragmentCapabilitiesBinding binding;
    private MainActivity main;
    private RecyclerView_Capabilities_Adapter adapter;
    private CapMgmt_Database db = CapMgmt_Database.getInstance(getContext());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentCapabilitiesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        main = (MainActivity) getActivity();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Context context = binding.getRoot().getContext();

        final RecyclerView recyclerView = binding.recVCapabilities;
        LinearLayoutManager _layout = new LinearLayoutManager( context );
        recyclerView.setLayoutManager( _layout );

        List<Capability> capabilityList = db.capDao().getAll();

        adapter = new RecyclerView_Capabilities_Adapter(context, capabilityList, main.get_currentUser() );
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        // update spinner selection
        Spinner spinnerStates = (Spinner) getView().findViewById(R.id.spinnerCapStates);
        spinnerStates.setOnItemSelectedListener(this);

        String[] spinnerSelection = new String[Capability.CapState.values().length+1];
        spinnerSelection[0] = "(" + getString( R.string.caps_spinner_show_all ) + ")";

        for (int i = 0; i<Capability.CapState.values().length; i++){
            String state = Capability.CapState.values()[i].toString();
            long count = db.capDao().countStates(Capability.CapState.getById(i));
            String itemName = String.format("%s (%s)",
                    state,
                    count + "");
            spinnerSelection[i+1] = itemName;
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                main,
                android.R.layout.simple_spinner_item,
                spinnerSelection);
        spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spinnerStates.setAdapter(spinnerArrayAdapter);
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

        final RecyclerView recyclerView = binding.recVCapabilities;
        final List<Capability> list = db.capDao().getAll();

        if (i == 0){
            adapter = new RecyclerView_Capabilities_Adapter(getContext(), list, main.get_currentUser() );
        } else {
            List<Capability> capabilityList = new ArrayList<>();
            for (Capability vc : list){
                if (vc.get_state() == Capability.CapState.getById(i-1)) capabilityList.add( vc );
            }
            adapter = new RecyclerView_Capabilities_Adapter(getContext(), capabilityList, main.get_currentUser());
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }
}