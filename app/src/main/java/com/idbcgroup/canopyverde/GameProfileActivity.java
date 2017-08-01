package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static java.security.AccessController.getContext;

public class GameProfileActivity extends AppCompatActivity {

    private TextView badge;
    private PorterShapeImageView profilePic;
    private TextView profileFullname, profileEmail, profileUsername;
    private Context context;
    private SharedPreferences pref_session;
    private ListView badgeList;

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
        float gamePoints = 2544;
        String points = getResources().getString(R.string.badge_name_example, formatter.format(gamePoints),
                getString(R.string.u_bagde));
        badge.setText(points);

        ArrayList<Badge> data = new ArrayList<Badge>();
        data.add(new Badge(2,"Reporte Verificado",10,"12 MAR 2017"));
        data.add(new Badge(1,"Reporte hecho",2,"12 MAR 2017"));
        data.add(new Badge(1,"Reporte hecho",2,"12 MAR 2017"));
        data.add(new Badge(0,"Solicitud Arbol",5,"12 MAR 2017"));
        data.add(new Badge(1,"Reporte hecho",2,"12 MAR 2017"));

        badgeList = (ListView) findViewById(R.id.reward_list);

        badgeList.setAdapter(new ListAdapter(GameProfileActivity.this, R.layout.badge_list_row, data) {

            /* This will implement the abstract method onEntry(Implemented in AnnounceAdapter), with
            the respective elements and handlers.
             */
            @Override
            public void onEntry(Object entry, View view) {

                if (entry != null){

                    ImageView pointStatus = (ImageView) view.findViewById(R.id.pointStatus);
                    TextView reportType = (TextView) view.findViewById(R.id.BadgeTypeDisplay);
                    TextView reportPoints = (TextView) view.findViewById(R.id.pointsDisplay);
                    TextView reportDay = (TextView) view.findViewById(R.id.dateDisplay);
                    TextView reportDate = (TextView) view.findViewById(R.id.monthDisplay);

                    int status = ((Badge) entry).getStatus();
                    String type = ((Badge) entry).getReportType();
                    int points = ((Badge) entry).getPoints();
                    String date = ((Badge) entry).getDate();

                    String[] dateParts = date.split(" ");
                    String day = dateParts[0];
                    String month =  dateParts[1] + " " + dateParts[2];

                    if (status == 0) {
                        pointStatus.setImageResource( R.drawable.p_rojo);
                    } else if (status == 1) {
                        pointStatus.setImageResource(R.drawable.p_amarillo);
                    } else {
                        pointStatus.setImageResource( R.drawable.p_verde);
                    }

                    reportType.setText(type);
                    reportPoints.setText("+"+points);
                    reportDay.setText(day);
                    reportDate.setText(month);

                    /*
                    SpannableString format =  new SpannableString(date);
                    format.setSpan(new RelativeSizeSpan(2f), 0,2, 0); // set size
                    reportDate.setText(format);*/
                }
            }
        });
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
