package com.hieutm.homepi.ui.commander;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.hieutm.homepi.R;
import com.hieutm.homepi.data.model.Commander;
import com.hieutm.homepi.ui.AppViewModelFactory;

import java.util.ArrayList;

public class CommanderFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_commander, container, false);
        Activity activity = getActivity();
        if (activity == null) {
            return root;
        }

        final ViewModelProvider.Factory viewModelFactory = AppViewModelFactory.getInstance(activity.getApplicationContext());
        final ViewModelProvider modelProvider = new ViewModelProvider((ViewModelStoreOwner) activity, viewModelFactory);
        CommanderViewModel commanderViewModel = modelProvider.get(CommanderViewModel.class);

        ListView commanderListView = root.findViewById(R.id.commander_list_view);
        CommanderListAdapter adapter = new CommanderListAdapter(activity, new ArrayList<>(), commander -> commanderViewModel.unregisterCommander(commander.getId()));
        commanderListView.setAdapter(adapter);
        commanderViewModel.getCommanders().observe(getActivity(), adapter::setCommanders);

        return root;
    }
}