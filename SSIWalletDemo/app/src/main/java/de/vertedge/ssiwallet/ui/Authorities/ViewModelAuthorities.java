package de.vertedge.ssiwallet.ui.Authorities;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewModelAuthorities extends ViewModel {

    private MutableLiveData<String> mText;

    public ViewModelAuthorities() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}