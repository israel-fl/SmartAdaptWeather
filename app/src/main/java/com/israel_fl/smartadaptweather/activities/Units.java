package com.israel_fl.smartadaptweather.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.israel_fl.smartadaptweather.R;

/* This class allows the user to chane the units from Imperial to Metric */
public class Units extends AppCompatActivity {

    public static final String UNIT_PREFERENCE = "com.israel_fl.smartadaptweather.UNIT_PREFERENCE";
    private static final String TAG = Units.class.getSimpleName();

    private boolean unitPreference;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.units_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get a handle to Shared Preferences
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Get a handle to buttons
        RadioGroup unitsGroup = (RadioGroup) findViewById(R.id.units_group);
        RadioButton metric = (RadioButton) findViewById(R.id.metric_button);
        RadioButton imperial = (RadioButton) findViewById(R.id.imperial_button);

        unitPreference = sharedPreferences.getBoolean(UNIT_PREFERENCE, true);

        if (unitPreference) {
            metric.setChecked(true);
        }
        else {
            imperial.setChecked(true);
        }

        unitsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.metric_button:
                        unitPreference = true;
                        editor.apply();
                        break;
                    case R.id.imperial_button:
                        unitPreference = false;
                        editor.apply();
                        break;
                }
                editor.putBoolean(UNIT_PREFERENCE, unitPreference);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");

        editor.putBoolean(UNIT_PREFERENCE, unitPreference);
        editor.commit();
    }
}
