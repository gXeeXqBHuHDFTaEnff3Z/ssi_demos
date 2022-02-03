package de.vertedge.ssiwallet.ui.Authorities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import de.vertedge.ssiwallet.databinding.FragmentAuthoritiesBinding;

public class FragmentAuthorities extends Fragment {

    private ViewModelAuthorities viewModelAuthorities;
    private FragmentAuthoritiesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModelAuthorities =
                new ViewModelProvider(this).get(ViewModelAuthorities.class);

        binding = FragmentAuthoritiesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
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
}