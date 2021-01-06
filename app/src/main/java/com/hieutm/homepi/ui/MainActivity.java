package com.hieutm.homepi.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.hieutm.homepi.R;
import com.hieutm.homepi.auth.AuthenticationService;
import com.hieutm.homepi.ui.home.HomeActivity;
import com.hieutm.homepi.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            getSupportActionBar().hide();
        }

        final AuthenticationService authService = AuthenticationService.getInstance(getApplicationContext());
        authService.getCurrentUser().subscribe(currentUser -> {
            openHomeActivity();
        }, error -> {
            Log.e(AuthenticationService.class.getName(), error.getMessage());
            Toast.makeText(MainActivity.this, R.string.error_cannot_connect, Toast.LENGTH_LONG).show();
            finish();
        }, () -> {
            //noinspection Convert2MethodRef
            openLoginActivity();
        });
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void openHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}