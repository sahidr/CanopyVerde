package com.idbcgroup.canopyverde;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class UserProfile extends AppCompatActivity {

    private ImageButton edit;
    private ImageButton map;
    private Button glogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_profile);

        map = (ImageButton) findViewById(R.id.map);
        edit = (ImageButton) findViewById(R.id.edit);
        glogout = (Button) findViewById(R.id.logout);


        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserProfile.this, EditProfile.class);
                startActivity(i);
            }
        });

        glogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor preferences = getSharedPreferences("Session", 0).edit().clear();
                preferences.apply();
                Intent i = new Intent(UserProfile.this,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

    }
}
