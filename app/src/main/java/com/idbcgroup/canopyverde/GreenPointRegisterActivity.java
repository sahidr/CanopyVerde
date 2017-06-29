package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GreenPointRegisterActivity extends AppCompatActivity {

    private SharedPreferences pref_location;
    private String latlng;
    private TextView current_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_green_point_register);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        pref_location = getSharedPreferences("Location", 0);
        latlng  = pref_location.getString("LatLng",null);


        current_location = (TextView) findViewById(R.id.location);
        current_location.setText(latlng);


    }

    public void yellowPointRegister(View view){
        SharedPreferences.Editor editor = getSharedPreferences("Marker", 0).edit();
        editor.putBoolean("approved", true);
        editor.apply();
        //startActivity(new Intent(GreenPointRegisterActivity.this, MapsActivity.class));
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
