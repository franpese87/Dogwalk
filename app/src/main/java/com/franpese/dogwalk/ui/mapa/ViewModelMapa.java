package com.franpese.dogwalk.ui.mapa;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewModelMapa extends ViewModel {

    private MutableLiveData<String> label;

    public ViewModelMapa() {
        label = new MutableLiveData<>();
        label.setValue("Esto es el fragmento de mapa.");
    }

    public LiveData<String> getLabel() {
        return label;
    }
}