package com.hieutm.homepi.ui.device;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.hieutm.homepi.R;
import com.hieutm.homepi.ui.AppViewModelFactory;

import java.util.ArrayList;

public class DeviceFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        Activity activity = getActivity();
        if (activity == null) {
            return root;
        }

        final ViewModelProvider.Factory viewModelFactory = AppViewModelFactory.getInstance(activity.getApplicationContext());
        final ViewModelProvider modelProvider = new ViewModelProvider((ViewModelStoreOwner) activity, viewModelFactory);
        DeviceViewModel deviceViewModel = modelProvider.get(DeviceViewModel.class);

        ListView deviceListView = root.findViewById(R.id.device_list_view);
        DeviceListAdapter adapter = new DeviceListAdapter(activity, new ArrayList<>(), device -> deviceViewModel.unregisterDevice(device.getId()));
        deviceListView.setAdapter(adapter);
        deviceViewModel.getDevices().observe(getActivity(), adapter::setDevices);
        return root;
    }
}