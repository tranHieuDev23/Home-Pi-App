package com.hieutm.homepi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hieutm.homepi.R;
import com.hieutm.homepi.auth.AuthenticationService;
import com.hieutm.homepi.data.Result;
import com.hieutm.homepi.data.model.LoggedInUser;
import com.hieutm.homepi.ui.home.HomeActivity;
import com.hieutm.homepi.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        final AuthenticationService authService = AuthenticationService.getInstance(getApplicationContext());
        authService.getCurrentUser(new Result.ResultHandler<LoggedInUser>() {
            @Override
            public void onSuccess(Result.Success<LoggedInUser> result) {
                if (result.getData() == null) {
                    openLoginActivity();
                } else {
                    openHomeActivity();
                }
            }

            @Override
            public void onError(Result.Error error) {
                Log.e(AuthenticationService.class.getName(), error.getError().getMessage());
                Toast.makeText(MainActivity.this, R.string.cannot_connect, Toast.LENGTH_LONG).show();
                finish();
            }
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