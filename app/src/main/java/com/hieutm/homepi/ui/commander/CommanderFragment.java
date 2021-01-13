package com.hieutm.homepi.ui.commander;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hieutm.homepi.R;
import com.hieutm.homepi.models.Commander;
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

        commanderViewModel.getErrors().observe(getActivity(), error -> {
            if (error == null) {
                return;
            }
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        ProgressBar progressBar = root.findViewById(R.id.commander_progress_bar);
        commanderViewModel.getIsLoading().observe(getActivity(), isLoading -> progressBar.setVisibility(isLoading? View.VISIBLE : View.INVISIBLE));

        RecyclerView commanderListView = root.findViewById(R.id.commander_list_view);
        CommanderListAdapter adapter = new CommanderListAdapter(new ArrayList<>(), this::showBottomSheet);
        commanderListView.setAdapter(adapter);
        commanderListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        commanderViewModel.getCommanders().observe(getActivity(), adapter::setCommanders);

        FloatingActionButton registerCommander = root.findViewById(R.id.register_commander_fab);
        registerCommander.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RegisterCommanderActivity.class);
            startActivity(intent);
        });

        setHasOptionsMenu(true);

        commanderViewModel.refresh();

        return root;
    }

    private void showBottomSheet(Commander commander) {
        CommanderBottomSheetFragment bottomSheet = new CommanderBottomSheetFragment(commander, c -> commanderViewModel.unregisterCommander(c.getId()));
        bottomSheet.show(getParentFragmentManager(), "Commander Bottom Sheet");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.commander_app_bar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.commander_refresh_option) {
            commanderViewModel.refresh();
        }
        return super.onOptionsItemSelected(item);
    }
}