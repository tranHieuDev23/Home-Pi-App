package com.hieutm.homepi.ui.commander;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.R;
import com.hieutm.homepi.data.Result;
import com.hieutm.homepi.data.model.Commander;
import com.hieutm.homepi.homecontrol.HomeControlService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommanderViewModel extends ViewModel {
    private final MutableLiveData<List<Commander>> commanders;
    private final MutableLiveData<Integer> errors;
    private final HomeControlService homeControlService;

    public CommanderViewModel(HomeControlService homeControlService) {
        this.commanders = new MutableLiveData<>(new ArrayList<>());
        this.errors = new MutableLiveData<>(null);

        this.homeControlService = homeControlService;
        this.homeControlService.getCommandersOfUser(new Result.ResultHandler<List<Commander>>() {
            @Override
            public void onSuccess(Result.Success<List<Commander>> result) {
                commanders.setValue(result.getData());
            }

            @Override
            public void onError(Result.Error error) {
                errors.setValue(R.string.error_cannot_connect);
            }
        });
    }

    public LiveData<List<Commander>> getCommanders() {
        return commanders;
    }

    public LiveData<Integer> getErrors() {
        return errors;
    }

    public void registerCommander(@NotNull String commanderId) {
        homeControlService.registerCommander(commanderId, new Result.ResultHandler<Commander>() {
            @Override
            public void onSuccess(Result.Success<Commander> result) {
                List<Commander> newList = new ArrayList<>(commanders.getValue());
                newList.add(result.getData());
                commanders.setValue(newList);
            }

            @Override
            public void onError(Result.Error error) {
                Log.e(CommanderViewModel.class.getName(), error.getError().getMessage());
                errors.setValue(R.string.error_cannot_connect);
            }
        });
    }

    public void unregisterCommander(@NotNull String commanderId) {
        List<Commander> newList = new ArrayList<>(commanders.getValue());
        newList.removeIf(item -> item.getId().equals(commanderId));
        commanders.setValue(newList);
    }
}