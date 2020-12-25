package com.hieutm.homepi.ui.commander;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CommanderViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CommanderViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is commander fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}