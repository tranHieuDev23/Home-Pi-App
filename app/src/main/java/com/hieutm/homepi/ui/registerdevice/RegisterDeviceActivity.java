package com.hieutm.homepi.ui.registerdevice;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.hieutm.homepi.R;
import com.hieutm.homepi.data.Result;
import com.hieutm.homepi.data.model.Device;
import com.hieutm.homepi.ui.AppViewModelFactory;

import java.util.ArrayList;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class RegisterDeviceActivity extends AppCompatActivity {
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int PERMISSION_REQUEST_ID = 1;

    private RegisterDeviceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device);

        final ViewModelProvider.Factory viewModelFactory = AppViewModelFactory.getInstance(getApplicationContext());
        final ViewModelProvider modelProvider = new ViewModelProvider(this, viewModelFactory);
        viewModel = modelProvider.get(RegisterDeviceViewModel.class);

        final Button enableBluetoothButton = findViewById(R.id.register_device_activity_enable_bluetooth_button);
        enableBluetoothButton.setOnClickListener(v -> viewModel.requestBluetooth(this));

        final View enableBluetoothLayout = findViewById(R.id.register_device_activity_enable_bluetooth_layout);
        final View registerLayout = findViewById(R.id.register_device_activity_register_layout);
        viewModel.getIsBluetoothEnabled().observe(this, bluetoothEnabled -> {
            if (bluetoothEnabled) {
                enableBluetoothLayout.setVisibility(View.INVISIBLE);
                registerLayout.setVisibility(View.VISIBLE);
            } else {
                enableBluetoothLayout.setVisibility(View.VISIBLE);
                registerLayout.setVisibility(View.INVISIBLE);
            }
        });

        RecyclerView deviceListView = findViewById(R.id.register_device_activity_list_view);
        BluetoothDeviceListAdapter adapter = new BluetoothDeviceListAdapter(new ArrayList<>(), (position, device) -> {
            viewModel.registerDevice(position, new Result.ResultHandler<Device>() {
                @Override
                public void onSuccess(Result.Success<Device> result) {

                }

                @Override
                public void onError(Result.Error error) {

                }
            });
        });
        deviceListView.setAdapter(adapter);
        deviceListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        viewModel.getDevices().observe(this, adapter::setDevices);

        startDiscovering();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(PERMISSION_REQUEST_ID)
    public void startDiscovering() {
        if (!EasyPermissions.hasPermissions(this, REQUIRED_PERMISSIONS)) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.register_device_activity_permission_rationale),
                    PERMISSION_REQUEST_ID,
                    REQUIRED_PERMISSIONS);
            return;
        }
        this.viewModel.discoverDevices();
    }
}