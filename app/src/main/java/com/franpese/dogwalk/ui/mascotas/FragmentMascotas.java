package com.franpese.dogwalk.ui.mascotas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.franpese.dogwalk.R;

import timber.log.Timber;

public class FragmentMascotas extends Fragment {

    private ViewModelMascotas viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("{onCreateView}");
        viewModel = new ViewModelProvider(this).get(ViewModelMascotas.class);
        View root = inflater.inflate(R.layout.fragment_mapa, container, false);

        viewModel.getLabel().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String label) {
                Timber.d("{getLabel.onChanged("+label+")}");
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        Timber.d("{onStart}");
        super.onStart();
    }

    @Override
    public void onStop() {
        Timber.d("{onStop}");
        super.onStop();
    }

}