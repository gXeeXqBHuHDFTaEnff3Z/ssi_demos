package de.vertedge.ssiwallet.ui.VCs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewModelVCs extends ViewModel {

    private MutableLiveData<String> mText;

    public ViewModelVCs() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");

    }

    public LiveData<String> getText() {
        return mText;
    }

}