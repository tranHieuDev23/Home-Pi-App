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

    public void unregisterCommander(@NotNull String commanderId) {
        removeCommander(commanderId);
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