package com.israel_fl.smartadaptweather.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.israel_fl.smartadaptweather.R;
import com.israel_fl.smartadaptweather.controllers.PMV;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.yahooweather.YahooProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String API_KEY = "0e012a881e64c68ea176a45f89c23e28ac4a66fb";
    private static final int MY_PERMISSIONS_REQUEST_READ_ACCESS_FINE_LOCATION = 1;

    private GoogleApiClient mGoogleApiClient;
    private WeatherConfig config;
    private WeatherClient client;
    private boolean isDoneRetrieving = false;
    private boolean didConnect = false;

    private Location mLastLocation;
    private double latitude;
    private double longitude;
    private String currentCity;
    private String cityName;

    private float temperature;
    private float humidity;
    private float windVelocity;
    private float rainProbability;

    private String tempString;
    private String humidityString;
    private String windString;
    private String rainString;

    private SwipeRefreshLayout swipeRefresh;
    private ImageView weatherIcon;
    private TextView temperatureView;
    private TextView humidityView;
    private TextView windView;
    private TextView rainView;

    private TextView suggestionOne;
    private TextView suggestionTwo;
    private TextView suggestionThree;
    private TextView energyOne;
    private TextView energyTwo;
    private TextView energyThree;
    private TextView savingsOne;
    private TextView savingsTwo;
    private TextView savingsThree;
    private Button ignoreOne;
    private Button ignoreTwo;
    private Button ignoreThree;

    private double pmv;
    private double ppd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate called");

        // Show an explanation
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            Toast.makeText(MainActivity.this, "We need location to access weather",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_ACCESS_FINE_LOCATION);

        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // The builder creates the client
        WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
        config = new WeatherConfig();
        try {

            // Pass API key before establishing connection
            config.ApiKey = API_KEY;

            client = builder.attach(this)
                    .provider(new YahooProviderType())
                    .httpClient(com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault.class)
                    .config(config)
                    .build();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        weatherIcon = (ImageView) findViewById(R.id.weather_icon);
        temperatureView = (TextView) findViewById(R.id.temperature_value_outdoors);
        humidityView = (TextView) findViewById(R.id.humidity_value_outdoors);
        windView = (TextView) findViewById(R.id.wind_speed_outdoors);
        rainView = (TextView) findViewById(R.id.precipitation_value_outdoors);

        /* Make Suggestions */
        suggestionOne = (TextView) findViewById(R.id.suggestion_one);
        suggestionTwo = (TextView) findViewById(R.id.suggestion_two);
        suggestionThree = (TextView) findViewById(R.id.suggestion_three);
        energyOne = (TextView) findViewById(R.id.energy_impact_one);
        energyTwo = (TextView) findViewById(R.id.energy_impact_two);
        energyThree = (TextView) findViewById(R.id.energy_impact_three);
        savingsOne = (TextView) findViewById(R.id.savings_one);
        savingsTwo = (TextView) findViewById(R.id.savings_two);
        savingsThree = (TextView) findViewById(R.id.savings_three);
        ignoreOne = (Button) findViewById(R.id.ignore_suggestion_one);
        ignoreTwo = (Button) findViewById(R.id.ignore_suggestion_two);
        ignoreThree = (Button) findViewById(R.id.ignore_suggestion_three);

        // Swipe to refresh weather implementation
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getWeatherInfo();

                // if weather information has been retrieved, spawn a thread to make calculations
                if (isDoneRetrieving && didConnect) {
                    new RefreshTask().execute();
                }
                else  if (didConnect == false) {
                    // Display failed meesage
                    Toast.makeText(MainActivity.this, "There was an error connecting," +
                            "check your Network", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /*
        * This is how the code rewrites itself
        */

        ignoreOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Adjust code to fit new information

                ignoreOne.setEnabled(false); // disable button
            }
        });

        ignoreTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Adjust code to fit new information

                ignoreTwo.setEnabled(false); // disable button
            }
        });

        ignoreThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Adjust code to fit new information

                ignoreThree.setEnabled(false); // disable button
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mGoogleApiClient.connect();

                } else {
                    // Permission Denied, close app
                    finish();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected called");

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Last Location: " + mLastLocation);

        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();
        Log.d(TAG, "Latitude: " + latitude + " Longitude: " + longitude);

        // Set title bar name to name of city
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(cityName);
        }

        getWeatherInfo(); // begin weather api
        modifyConfig(); // modify weather settings

        Log.d(TAG, "Last Location: " + mLastLocation);


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection Failed");
        Toast.makeText(MainActivity.this
                , "Error Connecting to Network, check your internet connection"
                , Toast.LENGTH_LONG).show();
    }

    /* Weather API Setup */
    private void getWeatherInfo() {

        Log.d(TAG, "getWeatherInfo called");

        // Search for the city of the user based on GPS coordinates
        client.searchCity(latitude, longitude, new WeatherClient.CityEventListener() {
            @Override
            public void onCityListRetrieved(List<City> cityList) {
                currentCity = cityList.get(0).getId(); // Retrieve the current city of the user
                Log.d(TAG, "Current City: " + currentCity + " City List size: " + cityList.size());

                // The client determines the provider for the forecast
                try {

                    client.getCurrentCondition(new WeatherRequest(currentCity),
                            new WeatherClient.WeatherEventListener() {
                                @Override
                                public void onWeatherRetrieved(CurrentWeather currentWeather) {

                                    modifyConfig(); // modify configuration
                                    client.updateWeatherConfig(config); // tell client config was updated

                                    temperature = currentWeather.weather.temperature.getTemp();
                                    humidity = currentWeather.weather.currentCondition.getHumidity();
                                    windVelocity = currentWeather.weather.wind.getSpeed();
                                    rainProbability = currentWeather.weather.rain[0].getChance();

                                    // Change weather icon based on current conditions
                                    client.getDefaultProviderImage(currentWeather.weather.currentCondition.getIcon(),
                                            new WeatherClient.WeatherImageListener() {
                                                @Override
                                                public void onWeatherError(WeatherLibException wle) {
                                                    Log.e(TAG, "Weather Error when Getting image");
                                                }

                                                @Override
                                                public void onConnectionError(Throwable t) {
                                                    Log.e(TAG, "Connection error when retrieving image");
                                                }

                                                @Override
                                                public void onImageReady(Bitmap image) {
                                                    weatherIcon.setImageBitmap(image);
                                                }
                                            });

                                    Log.d(TAG, "City [" + currentWeather.weather.location.getCity()
                                            + "] Current Temp [" + temperature + "]");

                                    // Parse value and display

                                    tempString = Float.toString(temperature) + " °C";
                                    humidityString = Float.toString(humidity) + " %";
                                    windString = Float.toString(windVelocity) + " m/s";
                                    rainString = Float.toString(rainProbability) + " %";

                                    temperatureView.setText(tempString);
                                    humidityView.setText(humidityString);
                                    windView.setText(windString);
                                    rainView.setText(rainString);

                                    isDoneRetrieving = true;
                                    didConnect = true;
                                    swipeRefresh.setRefreshing(false);

                                }

                                @Override
                                public void onWeatherError(WeatherLibException wle) {
                                    Log.d(TAG, "Weather Error - parsing data");
                                    wle.printStackTrace();
                                    Toast.makeText(MainActivity.this
                                            , "Error retrieving weather"
                                            , Toast.LENGTH_LONG).show();

                                    isDoneRetrieving = true;
                                    didConnect = false;
                                }

                                @Override
                                public void onConnectionError(Throwable t) {
                                    Log.d(TAG, "Connection error");
                                    t.printStackTrace();
                                    Toast.makeText(MainActivity.this
                                            , "Error Connecting to Network, check your internet connection"
                                            , Toast.LENGTH_LONG).show();

                                    isDoneRetrieving = true;
                                    didConnect = false;
                                }
                            });


                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onWeatherError(WeatherLibException wle) {
                Log.e(TAG, "There was an error retrieving the data");
            }

            @Override
            public void onConnectionError(Throwable t) {
                Log.e(TAG, "There was a connection error");
            }
        });

    }

    /*
    *   This method modifies the configuration of the weather provider.
    *   This establishes the parameters for the number of days in the forecast
    *   and Imperial/Metric system.
    */
    private void modifyConfig() {
        Log.d(TAG, "modifyConfig called");

        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        boolean unitPreference = sharedPreferences.getBoolean(Units.UNIT_PREFERENCE,true);

        if (unitPreference) {
            config.unitSystem = WeatherConfig.UNIT_SYSTEM.M; // metric
        }
        else {
            config.unitSystem = WeatherConfig.UNIT_SYSTEM.I; // imperial
        }
        config.lang = "en"; // english
        config.maxResult = 1; // Max number of cities retrieved
        config.numDays = 1; // retrieve only one day
    }

    // TODO: swipe refresh task
    private class RefreshTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            PMV formula = new PMV(); // pass values and calculate

            pmv = formula.getPmv();
            ppd = formula.getPpd();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (pmv > -0.5 && pmv < 0.5) {
                // do stuff
            }

            // Re enable buttons since new data was retrieved
            ignoreOne.setEnabled(true);
            ignoreTwo.setEnabled(true);
            ignoreThree.setEnabled(true);

            isDoneRetrieving = false;
            swipeRefresh.setRefreshing(false); // stop refreshing
        }
    }

//    private void refreshWeather() {
//        swipeRefresh.setRefreshing(false);
//    }

    // Instantiate the menu, and receive input from it
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int settings = item.getItemId();

        // Open the settings menu
        if (settings == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), Settings.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}
