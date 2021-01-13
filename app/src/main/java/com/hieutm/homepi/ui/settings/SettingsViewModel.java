package com.hieutm.homepi.ui.settings;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.auth.AuthenticationService;
import com.hieutm.homepi.models.LoggedInUser;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<LoggedInUser> currentUser;
    private final AuthenticationService authenticationService;

    @SuppressLint("CheckResult")
    public SettingsViewModel(AuthenticationService authenticationService) {
        this.currentUser = new MutableLiveData<>(null);
        this.authenticationService = authenticationService;
        authenticationService.getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currentUser::postValue);
    }

    public LiveData<LoggedInUser> getCurrentUser() {
        return currentUser;
    }

    public Completable logOut() {
        return authenticationService.logOut();
    }
}