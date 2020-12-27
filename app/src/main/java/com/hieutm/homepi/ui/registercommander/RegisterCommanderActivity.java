package com.hieutm.homepi.ui.registercommander;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.hieutm.homepi.R;

public class RegisterCommanderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_commander);
        RegisterCommanderPagerAdapter registerCommanderPagerAdapter = new RegisterCommanderPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(registerCommanderPagerAdapter);
    }
}