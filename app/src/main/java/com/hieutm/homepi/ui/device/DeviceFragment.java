package com.hieutm.homepi.ui.device;

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
import com.hieutm.homepi.data.model.Device;
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

        RecyclerView deviceListView = root.findViewById(R.id.device_list_view);
        DeviceListAdapter adapter = new DeviceListAdapter(new ArrayList<>(), this::showBottomSheet);
        deviceListView.setAdapter(adapter);
        //noinspection ConstantConditions
        deviceListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        deviceViewModel.getDevices().observe(getActivity(), adapter::setDevices);

        FloatingActionButton registerCommander = root.findViewById(R.id.register_device_fab);
        registerCommander.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RegisterDeviceActivity.class);
            startActivity(intent);
        });

        return root;
    }

    private void showBottomSheet(Device device) {
        DeviceBottomSheetFragment bottom = new DeviceBottomSheetFragment(device, d -> deviceViewModel.unregisterDevice(d.getId()));
        bottom.show(getParentFragmentManager(), "Device Bottom Sheet");
    }
}