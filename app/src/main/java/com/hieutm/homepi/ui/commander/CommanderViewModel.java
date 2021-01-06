package com.hieutm.homepi.ui.commander;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.R;
import com.hieutm.homepi.models.Commander;
import com.hieutm.homepi.homecontrol.HomeControlService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommanderViewModel extends ViewModel {
    private final MutableLiveData<List<Commander>> commanders;
    private final MutableLiveData<Integer> errors;
    private final HomeControlService homeControlService;

    @SuppressLint("CheckResult")
    public CommanderViewModel(HomeControlService homeControlService) {
        this.commanders = new MutableLiveData<>(new ArrayList<>());
        this.errors = new MutableLiveData<>(null);
        this.homeControlService = homeControlService;
        this.homeControlService.getCommandersOfUser().subscribe(commander -> {
            //noinspection Convert2MethodRef
            addCommander(commander);
        }, error -> {
            errors.setValue(R.string.error_cannot_connect);
        }, () -> {

        });
    }

    public LiveData<List<Commander>> getCommanders() {
        return commanders;
    }

    public LiveData<Integer> getErrors() {
        return errors;
    }

    @SuppressLint("CheckResult")
    public void registerCommander(@NotNull String commanderId) {
        homeControlService.registerCommander(commanderId).subscribe(this::addCommander, error -> {
            Log.e(CommanderViewModel.class.getName(), error.getMessage());
            errors.setValue(R.string.error_cannot_connect);
        });
    }

    public void unregisterCommander(@NotNull String commanderId) {
        removeCommander(commanderId);
    }

    private void addCommander(@NotNull Commander commander) {
        List<Commander> newList = commanders.getValue();
        //noinspection ConstantConditions
        newList.add(commander);
        commanders.setValue(newList);
    }

    private void removeCommander(@NotNull String commanderId) {
        List<Commander> newList = commanders.getValue();
        //noinspection ConstantConditions
        newList.removeIf(item -> item.getId().equals(commanderId));
        commanders.setValue(newList);
    }
}