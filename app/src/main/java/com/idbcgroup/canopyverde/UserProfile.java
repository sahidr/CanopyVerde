package com.idbcgroup.canopyverde;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import retrofit2.http.Url;

public class UserProfile extends AppCompatActivity {

    private ImageButton edit;
    private ImageButton map;
    private Button logout;
    private ImageView profile;
    private TextView fullname;
    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_profile);

        map = (ImageButton) findViewById(R.id.map);
        edit = (ImageButton) findViewById(R.id.edit);
        logout = (Button) findViewById(R.id.logout);

        profile = (ImageView) findViewById(R.id.profilepic);
        fullname = (TextView) findViewById(R.id.fullNameDisplay);
        email = (TextView) findViewById(R.id.emailDisplay);
/*
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(i);
        GoogleSignInAccount acct = result.getSignInAccount();
        String personName = acct.getDisplayName();
        String personGivenName = acct.getGivenName();
        String personFamilyName = acct.getFamilyName();
        String personEmail = acct.getEmail();
        String personId = acct.getId();
        Uri personPhoto = acct.getPhotoUrl();

        profile.setImageURI(personPhoto);
        fullname.setText(personName);
        email.setText(personEmail);
*/

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

        logout.setOnClickListener(new View.OnClickListener() {
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
