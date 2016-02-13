package com.israel_fl.smartadaptweather.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.israel_fl.smartadaptweather.R;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.about_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
