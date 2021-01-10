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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.hieutm.homepi.R;
import com.hieutm.homepi.ui.AppViewModelFactory;
import com.hieutm.homepi.ui.selectwifi.ConnectWifiActivity;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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
    private static final int WIFI_REQUEST_ID = 3;

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
        BluetoothDeviceListAdapter adapter = new BluetoothDeviceListAdapter(new ArrayList<>(), (position, bluetoothDevice) -> registerDevice(bluetoothDevice.getAddress()));
        deviceListView.setAdapter(adapter);
        deviceListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        viewModel.getDevices().observe(this, adapter::setDevices);

        ProgressBar progressBar = findViewById(R.id.register_device_activity_progress_bar);
        viewModel.getIsRegistering().observe(this, isRegistering -> progressBar.setVisibility(isRegistering ? View.VISIBLE : View.GONE));

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
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case BLUETOOTH_REQUEST_ID:
                    viewModel.refreshBluetoothStatus();
                    break;
                case WIFI_REQUEST_ID:
                    boolean success = data.getBooleanExtra(ConnectWifiActivity.OUTPUT_SUCCESS_KEY, false);
                    if (!success) {
                        Toast.makeText(this, R.string.register_device_activity_wifi_not_connected, Toast.LENGTH_LONG).show();
                    }
                    viewModel.registerDevice(data.getStringExtra(ConnectWifiActivity.OUTPUT_MAC_KEY));
                    break;
            }
        }
    }

    private void requestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_ID);
    }

    @SuppressLint("CheckResult")
    private void registerDevice(String mac) {
        viewModel
                .registerDevice(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((device, throwable) -> {
                    if (throwable != null) {
                        if (throwable instanceof DeviceNotConnectedToWifiException) {
                            requestDeviceConnectWifi(mac);
                            return;
                        }
                        Toast.makeText(getBaseContext(), R.string.register_device_an_error_happened, Toast.LENGTH_LONG).show();
                        return;
                    }
                    finish();
                });
    }

    @SuppressLint("CheckResult")
    private void requestDeviceConnectWifi(String mac) {
        Intent wifiActivityIntent = new Intent(getBaseContext(), ConnectWifiActivity.class);
        wifiActivityIntent.putExtra(ConnectWifiActivity.INPUT_EXTRA_MAC_KEY, mac);
        startActivityForResult(wifiActivityIntent, WIFI_REQUEST_ID);
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