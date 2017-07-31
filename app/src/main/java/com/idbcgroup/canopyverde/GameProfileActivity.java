package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GameProfileActivity extends AppCompatActivity {

    private TextView badge;
    private PorterShapeImageView profilePic;
    private TextView profileFullname, profileEmail, profileUsername;
    private Context context;
    private SharedPreferences pref_session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_profile);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        context = this;

        profilePic= (PorterShapeImageView) findViewById(R.id.profilepic);
        profileFullname = (TextView) findViewById(R.id.fullNameDisplay);
        profileUsername = (TextView) findViewById(R.id.usernameDisplay);
        badge = (TextView) findViewById(R.id.badgeName);

        pref_session = getSharedPreferences("Session", 0);

        String name = pref_session.getString("name",null);
        String email = pref_session.getString("email",null);
        String username = pref_session.getString("username",null);
        String profilepic = pref_session.getString("photo",null);

        if (profilepic!=null) {
            Uri photo = Uri.parse(profilepic);
            Picasso.with(context).load(photo).into(profilePic);
        }

        if (username==null){
            String[] emailParts = email.split("@");
            String user =  emailParts[0];
            profileUsername.setText("@"+user);
        } else {
            profileUsername.setText(username);
        }

        profileFullname.setText(name);

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ITALIAN);
        float gamepoints = 2544;
        String points = getResources().getString(R.string.badge_name_example, formatter.format(gamepoints),
                getString(R.string.u_bagde));
        badge.setText(points);

    }

    public void backToProfile(View view){
        onBackPressed();
    }

    public void goToMap(View view){
        startActivity(new Intent(GameProfileActivity.this,MapsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
