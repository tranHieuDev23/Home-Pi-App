package com.hieutm.homepi.ui.commander;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hieutm.homepi.R;
import com.hieutm.homepi.data.model.Commander;
import com.hieutm.homepi.ui.AppViewModelFactory;
import com.hieutm.homepi.ui.registercommander.RegisterCommanderActivity;

import java.util.ArrayList;

public class CommanderFragment extends Fragment {

    private CommanderViewModel commanderViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_commander, container, false);
        Activity activity = getActivity();
        if (activity == null) {
            return root;
        }

        final ViewModelProvider.Factory viewModelFactory = AppViewModelFactory.getInstance(activity.getApplicationContext());
        final ViewModelProvider modelProvider = new ViewModelProvider((ViewModelStoreOwner) activity, viewModelFactory);
        commanderViewModel = modelProvider.get(CommanderViewModel.class);

        RecyclerView commanderListView = root.findViewById(R.id.commander_list_view);
        CommanderListAdapter adapter = new CommanderListAdapter(new ArrayList<>(), this::showBottomSheet);
        commanderListView.setAdapter(adapter);
        //noinspection ConstantConditions
        commanderListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        commanderViewModel.getCommanders().observe(getActivity(), adapter::setCommanders);

        FloatingActionButton registerCommander = root.findViewById(R.id.register_commander_fab);
        registerCommander.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RegisterCommanderActivity.class);
            startActivity(intent);
        });

        return root;
    }

    private void showBottomSheet(Commander commander) {
        CommanderBottomSheetFragment bottomSheet = new CommanderBottomSheetFragment(commander, c -> commanderViewModel.unregisterCommander(c.getId()));
        bottomSheet.show(getParentFragmentManager(), "Commander Bottom Sheet");
    }
}