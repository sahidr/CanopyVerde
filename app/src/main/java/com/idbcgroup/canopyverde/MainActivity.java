package com.idbcgroup.canopyverde;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref_tour;
    private SharedPreferences pref_session;
    private Boolean visited;
    private Boolean logged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref_tour = getSharedPreferences("Tour", 0);
        visited  = pref_tour.getBoolean("visited",false);

        pref_session = getSharedPreferences("Session", 0);
        logged  = pref_session.getBoolean("logged",false);

        if (visited) {

            if (logged) {

                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                finish();
            } else {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        } else {

            Intent i = new Intent(MainActivity.this, TourActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }


    }
}
