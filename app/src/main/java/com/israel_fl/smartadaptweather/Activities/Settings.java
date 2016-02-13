package com.israel_fl.smartadaptweather.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.israel_fl.smartadaptweather.R;

import java.util.ArrayList;


/* This is the Settings Activity */

public class Settings extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> settingsOptions;
    private ArrayAdapter<String> adapter;
    private String[] settingsArray;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set UP affordance
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listView = (ListView) findViewById(R.id.settings_list);
        settingsArray = getResources().getStringArray(R.array.settings_options);
        settingsOptions = new ArrayList<>();
        for (int i = 0; i < settingsArray.length; i++) {
            settingsOptions.add(settingsArray[i]);
        }
        adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, settingsOptions);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {

                    case 0:
                        intent = new Intent(Settings.this, Units.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(Settings.this, TermsAndConditions.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(Settings.this, About.class);
                        startActivity(intent);
                        break;
                }

            }
        });


    }
}
