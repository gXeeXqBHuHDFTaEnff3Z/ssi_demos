package de.vertedge.ssiwallet.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import de.vertedge.ssiwallet.R;
import de.vertedge.ssiwallet.data.SSI.SSI_Database;
import de.vertedge.ssiwallet.data.SSI.SSI_Identity;
import de.vertedge.ssiwallet.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private SSI_Database db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        try {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        } catch (Exception e) {
            Log.e("HomeFragmentBinding", "onCreateView", e);
            throw e;
        }
        View root = binding.getRoot();

        Context context = binding.getRoot().getContext();

        db = SSI_Database.getInstance( context );

        recyclerView = binding.rvIdentities;

        // create grid layout with the approprioate number of recycler columns
        final int columns = getResources().getInteger(R.integer.recyclerview_columns);
        GridLayoutManager _layout = new GridLayoutManager( context, columns );
        recyclerView.setLayoutManager( _layout );

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Context context = binding.getRoot().getContext();

        List<SSI_Identity> list = db.identityDao().getAll();

        // DEMO if we are empty we need to wait a sec for the db
        if (list.size() == 0){
            try {
                Thread.sleep(200);
                list = db.identityDao().getAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assert list != null;
        RecyclerView_IDs_Adapter adapter = new RecyclerView_IDs_Adapter(context, list);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}