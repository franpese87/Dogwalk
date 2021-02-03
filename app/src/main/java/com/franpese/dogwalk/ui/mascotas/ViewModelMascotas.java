package com.franpese.dogwalk.ui.mascotas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewModelMascotas extends ViewModel {

    private MutableLiveData<String> label;

    public ViewModelMascotas() {
        label = new MutableLiveData<>();
        label.setValue("Esto es el fragmento mascotas.");
    }

    public LiveData<String> getLabel() {
        return label;
    }
}