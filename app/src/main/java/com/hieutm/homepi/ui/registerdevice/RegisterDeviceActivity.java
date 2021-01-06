package com.hieutm.homepi.ui.registerdevice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.hieutm.homepi.R;
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
    private static final int BLUETOOTH_REQUEST_ID = 2;

    private RegisterDeviceViewModel viewModel;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                viewModel.addDiscoveredDevice(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        final ViewModelProvider.Factory viewModelFactory = AppViewModelFactory.getInstance(getApplicationContext());
        final ViewModelProvider modelProvider = new ViewModelProvider(this, viewModelFactory);
        viewModel = modelProvider.get(RegisterDeviceViewModel.class);

        final Button enableBluetoothButton = findViewById(R.id.register_device_activity_enable_bluetooth_button);
        enableBluetoothButton.setOnClickListener(v -> this.requestBluetooth());

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
        @SuppressLint("CheckResult") BluetoothDeviceListAdapter adapter = new BluetoothDeviceListAdapter(new ArrayList<>(), (position, bluetoothDevice) -> {
            viewModel.registerDevice(position).subscribe((device, throwable) -> {
                if (throwable != null) {
                    Toast.makeText(getBaseContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), device.getId(), Toast.LENGTH_LONG).show();
                }
            });
        });
        deviceListView.setAdapter(adapter);
        deviceListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        viewModel.getDevices().observe(this, adapter::setDevices);

        viewModel.refreshBluetoothStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_REQUEST_ID) {
            viewModel.refreshBluetoothStatus();
        }
    }

    private void requestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_ID);
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
        this.viewModel.startDiscovering();
    }
}