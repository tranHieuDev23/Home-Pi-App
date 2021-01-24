package com.hieutm.homepi.ui.commander;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.R;
import com.hieutm.homepi.homecontrol.HomeControlService;
import com.hieutm.homepi.models.Commander;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;

public class CommanderViewModel extends ViewModel {
    private final MutableLiveData<List<Commander>> commanders;
    private final MutableLiveData<Integer> errors;
    private final MutableLiveData<Boolean> isLoading;
    private final HomeControlService homeControlService;

    @SuppressLint("CheckResult")
    public CommanderViewModel(HomeControlService homeControlService) {
        this.commanders = new MutableLiveData<>();
        this.errors = new MutableLiveData<>(null);
        this.isLoading = new MutableLiveData<>(false);
        this.homeControlService = homeControlService;
    }

    public LiveData<List<Commander>> getCommanders() {
        return commanders;
    }

    public LiveData<Integer> getErrors() {
        return errors;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public Completable renameCommander(@NotNull String commanderId, @NotNull String newName) {
        return new Completable() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(CompletableObserver s) {
                isLoading.postValue(true);
                homeControlService
                        .renameCommander(commanderId, newName)
                        .doFinally(() -> {
                            isLoading.postValue(false);
                            refresh();
                        })
                        .subscribe(() -> {}, s::onError);
            }
        };
    }

    public Completable unregisterCommander(@NotNull String commanderId) {
        return new Completable() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(CompletableObserver s) {
                isLoading.postValue(true);
                homeControlService
                        .unregisterCommander(commanderId)
                        .doFinally(() -> isLoading.postValue(false))
                        .subscribe(() -> removeCommander(commanderId), throwable -> errors.setValue(R.string.error_cannot_connect));
            }
        };
    }

    @SuppressLint("CheckResult")
    public void refresh() {
        commanders.setValue(new ArrayList<>());
        isLoading.setValue(true);
        this.homeControlService.getCommandersOfUser().subscribe(this::addCommander,
                error -> errors.setValue(R.string.error_cannot_connect),
                () -> isLoading.setValue(false));
    }

    private void addCommander(@NotNull Commander commander) {
        commanders.getValue().add(commander);
        commanders.setValue(commanders.getValue());
    }

    private void removeCommander(@NotNull String commanderId) {
        commanders.getValue().removeIf(item -> item.getId().equals(commanderId));
        commanders.setValue(commanders.getValue());
    }
}