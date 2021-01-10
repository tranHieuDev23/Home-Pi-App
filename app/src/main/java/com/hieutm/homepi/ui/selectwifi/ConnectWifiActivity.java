package com.hieutm.homepi.ui.selectwifi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.hieutm.homepi.R;
import com.hieutm.homepi.models.WifiNetwork;
import com.hieutm.homepi.ui.AppViewModelFactory;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ConnectWifiActivity extends AppCompatActivity {
    public static final String INPUT_EXTRA_MAC_KEY = "INPUT_EXTRA_MAC_KEY";
    public static final String OUTPUT_SUCCESS_KEY = "OUTPUT_SUCCESS_KEY";
    public static final String OUTPUT_MAC_KEY = "OUTPUT_MAC_KEY";

    private ConnectWifiViewModel viewModel;
    private String mac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_wifi);
        viewModel = AppViewModelFactory.getInstance(this).create(ConnectWifiViewModel.class);

        mac = getIntent().getStringExtra(INPUT_EXTRA_MAC_KEY);
        if (mac == null) {
            throw new RuntimeException("No target device's MAC address was included in the intent");
        }
        viewModel.setMac(mac);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        RecyclerView listView = findViewById(R.id.connect_wifi_activity_list_view);
        WifiListAdapter adapter = new WifiListAdapter(new ArrayList<>(), viewModel::selectWifiNetWork);
        listView.setAdapter(adapter);
        viewModel.getWifiNetworks().observe(this, adapter::setObjects);

        ProgressBar progressBar = findViewById(R.id.connect_wifi_activity_progress_bar);
        viewModel.getIsLoading().observe(this, isLoading -> progressBar.setVisibility(isLoading? View.VISIBLE : View.GONE));

        viewModel.getSelectedWifiNetwork().observe(this, wifiNetwork -> {
            if (wifiNetwork == null) {
                return ;
            }
            if (wifiNetwork.isOpen()) {
                connectWifi(wifiNetwork, null);
                return;
            }
            ConnectWifiDialogFragment dialog = new ConnectWifiDialogFragment(wifiNetwork.getSsid(), psk -> {
                if (psk == null || psk.length() < 8) {
                    return false;
                }
                connectWifi(wifiNetwork, psk);
                return true;
            });
            dialog.show(getSupportFragmentManager(), "WiFiAuthDialog");
        });

        scanWifi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect_wifi_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.connect_wifi_refresh_option) {
            scanWifi();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanWifi() {
        viewModel
                .scanWifi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @SuppressLint("CheckResult")
    private void connectWifi(WifiNetwork wifiNetwork, String psk) {
        viewModel
                .connectWifi(wifiNetwork, psk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::responseConnectSuccess, throwable -> Toast.makeText(getBaseContext(), R.string.connect_wifi_activity_failure, Toast.LENGTH_LONG).show());
    }

    private void responseConnectSuccess() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(OUTPUT_SUCCESS_KEY, true);
        resultIntent.putExtra(OUTPUT_MAC_KEY, mac);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}