package com.hieutm.homepi.ui.device;

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
import com.hieutm.homepi.models.Device;
import com.hieutm.homepi.ui.AppViewModelFactory;
import com.hieutm.homepi.ui.registerdevice.RegisterDeviceActivity;

import java.util.ArrayList;

public class DeviceFragment extends Fragment {

    private DeviceViewModel deviceViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        Activity activity = getActivity();
        if (activity == null) {
            return root;
        }

        final ViewModelProvider.Factory viewModelFactory = AppViewModelFactory.getInstance(activity.getApplicationContext());
        final ViewModelProvider modelProvider = new ViewModelProvider((ViewModelStoreOwner) activity, viewModelFactory);
        deviceViewModel = modelProvider.get(DeviceViewModel.class);

        deviceViewModel.getErrors().observe(getActivity(), error -> {
            if (error == null) {
                return;
            }
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        ProgressBar progressBar = root.findViewById(R.id.device_progress_bar);
        deviceViewModel.getIsLoading().observe(getActivity(), isLoading -> progressBar.setVisibility(isLoading? View.VISIBLE : View.INVISIBLE));


        RecyclerView deviceListView = root.findViewById(R.id.device_list_view);
        DeviceListAdapter adapter = new DeviceListAdapter(new ArrayList<>(), this::showBottomSheet);
        deviceListView.setAdapter(adapter);
        deviceListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        deviceViewModel.getDevices().observe(getActivity(), adapter::setDevices);

        FloatingActionButton registerCommander = root.findViewById(R.id.register_device_fab);
        registerCommander.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RegisterDeviceActivity.class);
            startActivity(intent);
        });

        setHasOptionsMenu(true);

        deviceViewModel.refresh();

        return root;
    }

    private void showBottomSheet(Device device) {
        DeviceBottomSheetFragment bottom = new DeviceBottomSheetFragment(device, d -> deviceViewModel.unregisterDevice(d.getId()));
        bottom.show(getParentFragmentManager(), "Device Bottom Sheet");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.device_app_bar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.device_refresh_option) {
            deviceViewModel.refresh();
        }
        return super.onOptionsItemSelected(item);
    }
}