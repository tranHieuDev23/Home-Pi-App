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
import android.view.Menu;
import android.view.MenuItem;
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

    private static final long DISCOVERY_DELAY = 10000;

    private RegisterDeviceViewModel viewModel;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    viewModel.addDiscoveredDevice(device);
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                    if (currentState == BluetoothAdapter.STATE_ON || currentState == BluetoothAdapter.STATE_OFF) {
                        viewModel.refreshBluetoothStatus();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
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
                startDiscovering();
            } else {
                enableBluetoothLayout.setVisibility(View.VISIBLE);
                registerLayout.setVisibility(View.INVISIBLE);
            }
            invalidateOptionsMenu();
        });
        viewModel.getIsDiscovering().observe(this, isDiscovering -> invalidateOptionsMenu());

        RecyclerView deviceListView = findViewById(R.id.register_device_activity_list_view);
        BluetoothDeviceListAdapter adapter = new BluetoothDeviceListAdapter(new ArrayList<>(), (position, bluetoothDevice) -> registerDevice(bluetoothDevice.getAddress()));
        deviceListView.setAdapter(adapter);
        deviceListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        viewModel.getDevices().observe(this, adapter::setDevices);

        ProgressBar progressBar = findViewById(R.id.register_device_activity_progress_bar);
        viewModel.getIsLoading().observe(this, isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.refreshBluetoothStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDiscovering();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.register_device_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean bluetoothEnabled = viewModel.getIsBluetoothEnabled().getValue();
        menu.getItem(0).setVisible(bluetoothEnabled);
        if (bluetoothEnabled) {
            boolean isDiscovering = viewModel.getIsDiscovering().getValue();
            menu.getItem(0).setIcon(isDiscovering ? R.drawable.ic_baseline_stop_24 : R.drawable.ic_refresh_white_24);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.register_device_refresh_option) {
            if (viewModel.getIsDiscovering().getValue()) {
                stopDiscovering();
            } else {
                startDiscovering();
            }
        }
        return super.onOptionsItemSelected(item);
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
            if (requestCode == WIFI_REQUEST_ID) {
                boolean success = data.getBooleanExtra(ConnectWifiActivity.OUTPUT_SUCCESS_KEY, false);
                if (success) {
                    registerDevice(data.getStringExtra(ConnectWifiActivity.OUTPUT_MAC_KEY));
                }
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
                        Toast.makeText(getBaseContext(), R.string.register_device_an_error_happened, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getBaseContext(), R.string.register_device_register_success, Toast.LENGTH_SHORT).show();
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
        viewModel.startDiscovering();
        new Thread(() -> {
            try {
                Thread.sleep(DISCOVERY_DELAY);
            } catch (InterruptedException e) {
                // Intentionally ignored
            }
            stopDiscovering();
        }).start();
    }

    private void stopDiscovering() {
        viewModel.stopDiscovering();
    }
}