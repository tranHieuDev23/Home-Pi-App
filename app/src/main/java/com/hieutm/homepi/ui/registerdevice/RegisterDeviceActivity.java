package com.hieutm.homepi.ui.registerdevice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.hieutm.homepi.R;

public class RegisterDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_commander);
        RegisterDevicePagerAdapter registerDevicePagerAdapter = new RegisterDevicePagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.register_device_view_pager);
        viewPager.setAdapter(registerDevicePagerAdapter);
    }
}