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
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.yahooweather.YahooProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


/*

Copyright 2016 Israel Flores

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private double version = 1.0;
    
    /* Database Information */
    private final static String API_KEY = "API_KEY";
    private static final int MY_PERMISSIONS_REQUEST_READ_ACCESS_FINE_LOCATION = 1;
    private static final String OUTLOOK_TEMPERATURE = "temperature";
    private static final String OUTLOOK_HUMIDITY = "humidity";
    private static final String OUTLOOK_CO2 = "CO2";
    private static final String OUTLOOK_WIND = "wind";
    private static final String OUTLOOK_MEANTEMP = "meanTemp";
    private static final String OUTLOOK = "Outlook"; // database name
    private static final ParseObject outlook = new ParseObject(OUTLOOK);

    /* Get the Date from the system */
    private Calendar calendar = new GregorianCalendar();
    private int hour; // (0 - 23)

    private GoogleApiClient mGoogleApiClient;
    private WeatherConfig config;
    private WeatherClient client;
    private boolean isDoneRetrieving = false; // yahoo weather retrieved
    private boolean isDatabaseRetrieved = false; // parse retrieved

    private Location mLastLocation;
    private double latitude;
    private double longitude;
    private String currentCity;
    private String cityName;

    /*  Inside values */
    private double tempIn;
    private double humIn;
    private double windInside;
    private double co2;
    private double meanTemp;

    /* Outside values */
    private double tempOut;
    private double humOut;
    private double windOutside;
    private double rainProbability;

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
    private TextView outsidePMVText;

    private TextView insideTempView;
    private TextView insideHumView;
    private TextView insideWindView;
    private TextView insideCO2View;
    private TextView insidePMVText;

    private TextView suggestionOne;
    private TextView suggestionTwo;
    private TextView energyOne;
    private TextView energyTwo;
    private TextView savingsOne;
    private TextView savingsTwo;
    private Button ignoreOne;
    private Button ignoreTwo;
    private ImageView iconOne;
    private ImageView iconTwo;

    private TextView studySuggestion;
    private TextView diningSuggestion;
    private TextView sleepingSuggestion;
    private TextView relaxingSuggestion;
    private TextView studyEnergy;
    private TextView diningEnergy;
    private TextView sleepingEnergy;
    private TextView relaxingEnergy;
    private TextView studyCost;
    private TextView diningCost;
    private TextView sleepingCost;
    private TextView relaxingCost;
    private ImageView studyIcon;
    private ImageView diningIcon;
    private ImageView sleepingIcon;
    private ImageView relaxingIcon;

    private double insidePMV;
    private double outsidePMV;

    private double pmvLowerBound = -0.5;
    private double pmvUpperBound = 0.5;
    private PMV inside; // inside values
    private PMV outside; // outside values

    private String[] suggestions;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate called");

        /* Initialize Database */
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        ParseACL acl = new ParseACL();
        // Enable public read access.
        acl.setPublicReadAccess(true);
        ParseACL.setDefaultACL(acl, true);
        outlook.setACL(acl);

        // Get latest parse data
        findLatestData();

        // Ask for Permissions and show an explanation
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

        // Outside Data Views
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        weatherIcon = (ImageView) findViewById(R.id.weather_icon);
        temperatureView = (TextView) findViewById(R.id.temperature_value_outdoors);
        humidityView = (TextView) findViewById(R.id.humidity_value_outdoors);
        windView = (TextView) findViewById(R.id.wind_speed_outdoors);
        rainView = (TextView) findViewById(R.id.precipitation_value_outdoors);
        outsidePMVText = (TextView) findViewById(R.id.outside_pmv_view);

        // Inside Data Views
        insideTempView = (TextView) findViewById(R.id.temperature_value);
        insideHumView = (TextView) findViewById(R.id.humidity_value);
        insideWindView = (TextView) findViewById(R.id.inside_wind);
        insideCO2View = (TextView) findViewById(R.id.inside_co2);
        insidePMVText = (TextView) findViewById(R.id.inside_pmv_view);

        /* Make Suggestions */
        suggestionOne = (TextView) findViewById(R.id.suggestion_one);
        suggestionTwo = (TextView) findViewById(R.id.suggestion_two);
        energyOne = (TextView) findViewById(R.id.energy_impact_one);
        energyTwo = (TextView) findViewById(R.id.energy_impact_two);
        savingsOne = (TextView) findViewById(R.id.savings_one);
        savingsTwo = (TextView) findViewById(R.id.savings_two);
        ignoreOne = (Button) findViewById(R.id.ignore_suggestion_one);
        ignoreTwo = (Button) findViewById(R.id.ignore_suggestion_two);
        iconOne = (ImageView) findViewById(R.id.suggestion_icon);
        iconTwo = (ImageView) findViewById(R.id.suggestion_icon_two);

        /* Blinds */

        studySuggestion = (TextView) findViewById(R.id.study_suggestion);
        diningSuggestion = (TextView) findViewById(R.id.dining_suggestion);
        sleepingSuggestion = (TextView) findViewById(R.id.sleeping_suggestion);
        relaxingSuggestion = (TextView) findViewById(R.id.relaxing_suggestion);
        studyEnergy = (TextView) findViewById(R.id.energy_impact_study);
        diningEnergy = (TextView) findViewById(R.id.energy_impact_dining);
        sleepingEnergy = (TextView) findViewById(R.id.energy_impact_sleeping);
        relaxingEnergy = (TextView) findViewById(R.id.energy_impact_relaxing);
        studyCost = (TextView) findViewById(R.id.savings_study);
        diningCost = (TextView) findViewById(R.id.savings_dining);
        sleepingCost = (TextView) findViewById(R.id.savings_sleeping);
        relaxingCost = (TextView) findViewById(R.id.savings_relaxing);
        studyIcon = (ImageView) findViewById(R.id.suggestion_icon_study);
        diningIcon = (ImageView) findViewById(R.id.suggestion_icon_dining);
        sleepingIcon = (ImageView) findViewById(R.id.suggestion_icon_sleeping);
        relaxingIcon = (ImageView) findViewById(R.id.suggestion_icon_relaxing);


        // Swipe to refresh weather implementation
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // make sure that the values are false first
                isDoneRetrieving = false;
                isDatabaseRetrieved = false;

                swipeRefresh.setRefreshing(true);

                // Spawn thread to retrieve data from Yahoo
                getWeatherInfo();

                // Spawn thread to retrieve data from Parse
                findLatestData();

                // Spawn a thread to make pmv calculations
                new RefreshTask().execute();

            }
        });

        /*
        * This is how the code rewrites itself
        */

        ignoreOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Adjust Boundaries
                if (pmvLowerBound > -1.1 && pmvUpperBound < 1.1) {
                    pmvLowerBound -= 0.3;
                    pmvUpperBound += 0.3;

                    // Adjust code to fit new information
                    suggestionOne.setText(getResources().getString(R.string.no_suggestion));
                    energyOne.setText(getResources().getString(R.string.no_energy));
                    savingsOne.setText(getResources().getString(R.string.no_savings));
                    iconOne.setImageResource(android.R.color.transparent);
                    ignoreOne.setTextColor(getResources().getColor(R.color.greyText));
                    ignoreOne.setText(getResources().getString(R.string.ignored));
                    ignoreOne.setEnabled(false); // disable button
                }
                else {
                    Toast.makeText(MainActivity.this, "PMV limits reached", Toast.LENGTH_SHORT).show();
                }


            }
        });

        ignoreTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Adjust Boundaries
                if (pmvLowerBound > -1.1 && pmvUpperBound < 1.1) {
                    pmvLowerBound -= 0.3;
                    pmvUpperBound += 0.3;

                    // Adjust code to fit new information
                    suggestionTwo.setText(getResources().getString(R.string.no_suggestion));
                    energyTwo.setText(getResources().getString(R.string.no_energy));
                    savingsTwo.setText(getResources().getString(R.string.no_savings));
                    iconTwo.setImageResource(android.R.color.transparent);;
                    ignoreTwo.setTextColor(getResources().getColor(R.color.greyText));
                    ignoreTwo.setText(getResources().getString(R.string.ignored));
                    ignoreTwo.setEnabled(false); // disable button
                }
                else {
                    Toast.makeText(MainActivity.this, "PMV limits reached", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // Spawn a thread to make pmv calculations
        new RefreshTask().execute();

    } // end of onCreate

    /* Query Database to find the latest values */
    private void findLatestData() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(OUTLOOK);
        query.orderByDescending("updatedAt"); // This defines newest object
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Log.e(OUTLOOK, "The getFirst request failed.");
                } else {
                    tempIn = object.getDouble(OUTLOOK_TEMPERATURE);
                    humIn = object.getDouble(OUTLOOK_HUMIDITY);
                    co2 = object.getDouble(OUTLOOK_CO2);
                    meanTemp = object.getDouble(OUTLOOK_MEANTEMP);

                    insideTempView.setText(String.format(Locale.ENGLISH, "%.1f °C", tempIn));
                    insideHumView.setText(String.format(Locale.ENGLISH, "%.1f", humIn));
                    insideWindView.setText("0.2 m/s");
                    insideCO2View.setText(String.format(Locale.ENGLISH, "%.1f", co2));

                    Log.e(OUTLOOK, "Temperature: " + tempIn +
                            " HumIn: " + humIn
                            + " CO2: " + co2
                            + " Mean Radiant Temperature: " + meanTemp);

                    isDatabaseRetrieved = true;
                    Log.e(TAG, "isDatabaseRetrieved: " + isDatabaseRetrieved);
                }

                inside = new PMV(tempIn, humIn, meanTemp, 1); // inside values
                insidePMV = inside.getPmv();
                Log.e(TAG, "inside pmv: " + insidePMV);

            }
        });

    } // findLatestData

    /* Called when the user allows permission to get location */
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

    /* When connection is established to Google Location Services */
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
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set title to city name
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

                                    tempOut = currentWeather.weather.temperature.getTemp();
                                    humOut = currentWeather.weather.currentCondition.getHumidity();
                                    windOutside = currentWeather.weather.wind.getSpeed();
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
                                            + "] Current Temp [" + tempOut + "]");

                                    // Parse value and display

                                    tempString = Double.toString(tempOut) + " °C";
                                    humidityString = Double.toString(humOut) + " %";
                                    windString = String.format(Locale.ENGLISH, "%.1f", windOutside) + " m/s";
                                    rainString = String.format(Locale.ENGLISH, "%.1f", rainProbability) + " %";

                                    temperatureView.setText(tempString);
                                    humidityView.setText(humidityString);
                                    windView.setText(windString);
                                    rainView.setText(rainString);

                                    outside = new PMV(tempOut, humOut, tempOut, 1); // outside values, use tempOut for mean
                                    outsidePMV = outside.getPmv();
//                                    new RefreshTask().execute();
                                    Log.e(TAG, "Outside pmv: " + outsidePMV);

                                    isDoneRetrieving = true;
                                    Log.e(TAG, "isDoneRetrieving: " + isDoneRetrieving);

                                }

                                @Override
                                public void onWeatherError(WeatherLibException wle) {
                                    Log.d(TAG, "Weather Error - parsing data");
                                    wle.printStackTrace();
                                    Toast.makeText(MainActivity.this
                                            , "Error retrieving weather"
                                            , Toast.LENGTH_LONG).show();

                                    isDoneRetrieving = true;
                                }

                                @Override
                                public void onConnectionError(Throwable t) {
                                    Log.d(TAG, "Connection error");
                                    t.printStackTrace();
                                    Toast.makeText(MainActivity.this
                                            , "Error Connecting to Network, check your internet connection"
                                            , Toast.LENGTH_LONG).show();

                                    isDoneRetrieving = true;
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
        boolean unitPreference = sharedPreferences.getBoolean(Units.UNIT_PREFERENCE, true);

        if (unitPreference) {
            config.unitSystem = WeatherConfig.UNIT_SYSTEM.M; // metric
        } else {
            config.unitSystem = WeatherConfig.UNIT_SYSTEM.I; // imperial
        }
        config.lang = "en"; // english
        config.maxResult = 1; // Max number of cities retrieved
        config.numDays = 1; // retrieve only one day
    }


    /*
    *  Swipe Refresh Task
     */
    private class RefreshTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            Log.d(TAG, "doInBackground called");

            // Pause thread while other threads finish
            do {} while(!isDoneRetrieving && !isDatabaseRetrieved); // do nothing

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d(TAG, "onPostExecute called");

            makeSuggestions();

            insidePMVText.setText(String.format(Locale.ENGLISH, "%.1f", insidePMV));
            outsidePMVText.setText(String.format(Locale.ENGLISH, "%.1f", outsidePMV));

            // Re enable buttons since new data was retrieved
            ignoreOne.setEnabled(true);
            ignoreTwo.setEnabled(true);
            ignoreOne.setTextColor(getResources().getColor(R.color.colorPrimary));
            ignoreOne.setText(getResources().getString(R.string.ignore_suggestion));
            ignoreTwo.setTextColor(getResources().getColor(R.color.colorPrimary));
            ignoreTwo.setText(getResources().getString(R.string.ignore_suggestion));

            swipeRefresh.setRefreshing(false); // stop refreshing
        }
    }

    // Make Suggestions based on PMV values
    private void makeSuggestions() {
        Log.d(TAG, "Making Suggestions!");

        String cost = "$66";
        String energy = "330 kw";

        suggestions = getResources().getStringArray(R.array.suggestions);
        hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Comfort Zone
        if (insidePMV >= pmvLowerBound && insidePMV <= pmvUpperBound) {
            // do nothing, show no suggestions

            makeNoClothingSuggestion();
            makeNoWindowSuggestion();

        }
        else {

//            /* Clothing Suggestions */
//            if (insidePMV < pmvLowerBound) { // too cold
//
//                // Calculate new PMV and see if it falls in the comfort range
//                PMV newInside = new PMV(tempIn, humIn, meanTemp, 1.5);
//                double newInsidePMV = newInside.getPmv();
//
//                if (newInsidePMV >= pmvLowerBound && newInsidePMV <= pmvUpperBound) {
//                    suggestionOne.setText(suggestions[3]); // wear light clothing
//                    iconOne.setBackgroundResource(R.drawable.shirt);
//                    energyOne.setText(cost);
//                    savingsOne.setText(energy);
//                } else {
//                    makeNoClothingSuggestion();
//                }
//
//            } else {
//                makeNoClothingSuggestion();
//            }

            if (insidePMV > pmvUpperBound) // too hot
            {

                // Calculate new PMV and see if it falls in the comfort range
                PMV newInside = new PMV(tempIn, humIn, meanTemp, 0.5);
                double newInsidePMV = newInside.getPmv();

                if (newInsidePMV >= pmvLowerBound && newInsidePMV <= pmvUpperBound) {
                    suggestionOne.setText(suggestions[2]); // wear light clothing
                    iconOne.setBackgroundResource(R.drawable.shirt);
                    energyOne.setText(cost);
                    savingsOne.setText(energy);
                } else {
                    makeNoClothingSuggestion();
                }

            } else {
                makeNoClothingSuggestion();
            }

            /* Window Suggestions */

            if (insidePMV < pmvLowerBound && co2 < 500) { // Close the window

                if (outsidePMV < pmvLowerBound && outsidePMV > pmvUpperBound) { // unfavorable outside

                    suggestionTwo.setText(suggestions[0]); // close the windows
                    iconTwo.setBackgroundResource(R.drawable.closed_window);
                    energyTwo.setText(cost);
                    savingsTwo.setText(energy);

                } else {

                    makeNoWindowSuggestion(); // outside is pleasant, do nothing

                }

            } else {
                makeNoWindowSuggestion();
            }

            if (insidePMV > pmvUpperBound && co2 > 500) { // open the window

                if (outsidePMV > pmvLowerBound && outsidePMV < pmvUpperBound) { // favorable outside

                    suggestionTwo.setText(suggestions[1]); // open the window
                    iconTwo.setBackgroundResource(R.drawable.open_window);
                    energyTwo.setText(cost);
                    savingsTwo.setText(energy);

                } else {

                    makeNoWindowSuggestion();

                }
            } else {
                makeNoWindowSuggestion();
            }
        } // end of pmv suggestions

        /* Blinds suggestions */

        // Studying between 7am and 6pm
        if (hour >= 7 && hour <= 18) {
            // open
            studySuggestion.setText(suggestions[4]);
            studyIcon.setBackgroundResource(R.drawable.blinds_open);
            studyCost.setText(getString(R.string.blinds_savings));
            studyEnergy.setText(getString(R.string.blinds_energy));

        }
        else {
            // close
            studySuggestion.setText(suggestions[5]);
            studyIcon.setBackgroundResource(R.drawable.blinds_closed);

        }

        // Dining between 7am and 1pm
        if (hour >= 7 && hour < 13) {
            // open
            diningSuggestion.setText(suggestions[4]);
            diningIcon.setBackgroundResource(R.drawable.blinds_open);
            diningCost.setText(getString(R.string.blinds_savings));
            diningEnergy.setText(getString(R.string.blinds_energy));

        }
        else if (hour >= 13 && hour < 16) {
            // close
            diningSuggestion.setText(suggestions[5]);
            diningIcon.setBackgroundResource(R.drawable.blinds_closed);

        }
        else {
            // open
            diningSuggestion.setText(suggestions[4]);
            diningIcon.setBackgroundResource(R.drawable.blinds_open);
            diningCost.setText(getString(R.string.blinds_savings));
            diningEnergy.setText(getString(R.string.blinds_energy));

        }

        // Sleeping between 6am to 4pm
        if (hour >= 6 && hour < 16) {
            // close
            sleepingSuggestion.setText(suggestions[5]);
            sleepingIcon.setBackgroundResource(R.drawable.blinds_closed);

        }
        else {
            // open
            sleepingSuggestion.setText(suggestions[4]);
            sleepingIcon.setBackgroundResource(R.drawable.blinds_open);
            sleepingCost.setText(getString(R.string.blinds_savings));
            sleepingEnergy.setText(getString(R.string.blinds_energy));

        }

        // Relaxing
        if (hour >= 7 && hour < 14) {
            // open
            relaxingSuggestion.setText(suggestions[4]);
            relaxingIcon.setBackgroundResource(R.drawable.blinds_open);
            relaxingCost.setText(getString(R.string.blinds_savings));
            relaxingEnergy.setText(getString(R.string.blinds_energy));

        }
        else if (hour >= 14 && hour < 18) {
            // close
            relaxingSuggestion.setText(suggestions[5]);
            relaxingIcon.setBackgroundResource(R.drawable.blinds_closed);

        }
        else {
            // open
            relaxingSuggestion.setText(suggestions[4]);
            relaxingIcon.setBackgroundResource(R.drawable.blinds_open);
            relaxingCost.setText(getString(R.string.blinds_savings));
            relaxingEnergy.setText(getString(R.string.blinds_energy));

        }

    }


    /* Set Clothing Suggestions to None */
    private void makeNoClothingSuggestion() {
        suggestionOne.setText(getResources().getString(R.string.no_suggestion));
        energyOne.setText(getResources().getString(R.string.no_energy));
        savingsOne.setText(getResources().getString(R.string.no_savings));
        iconOne.setEnabled(false);
    }

    /* Set Windows Suggestions to None*/
    private void makeNoWindowSuggestion() {
        suggestionTwo.setText(getResources().getString(R.string.no_suggestion));
        energyTwo.setText(getResources().getString(R.string.no_energy));
        savingsTwo.setText(getResources().getString(R.string.no_savings));
        iconTwo.setEnabled(false);
    }

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

} // end of main activity
