package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static java.security.AccessController.getContext;

public class GameProfileActivity extends AppCompatActivity {

    private TextView badge;
    private PorterShapeImageView profilePic;
    private TextView profileFullname, profileUsername;
    private Context context;
    private SharedPreferences pref_session;
    private int user_id;
    private ListView badgeList;
    private ProgressBar progressBar;

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

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        profilePic= (PorterShapeImageView) findViewById(R.id.profilepic);
        profileFullname = (TextView) findViewById(R.id.fullNameDisplay);
        profileUsername = (TextView) findViewById(R.id.usernameDisplay);
        badge = (TextView) findViewById(R.id.badgeName);
        badgeList = (ListView) findViewById(R.id.reward_list);
        pref_session = getSharedPreferences("Session", 0);


        String fullname = pref_session.getString("fullname",null);
        String username = pref_session.getString("username",null);
        String profilepic = pref_session.getString("photo",null);
        int game_points = pref_session.getInt("game_points",0);
        String badge_name = pref_session.getString("badge",null);
        user_id = pref_session.getInt("id",0);


        Get g = new Get();
        g.execute();

        if (profilepic!=null) {
            Uri photo = Uri.parse(profilepic);
            Picasso.with(context).load(photo).into(profilePic);
        }

        profileUsername.setText("@"+username);
        profileFullname.setText(fullname);

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ITALIAN);
        String gameReport = getResources().getString(R.string.badge_name_example, formatter.format(game_points),
                badge_name);
        badge.setText(gameReport);

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


    public class Get extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject apiResponse = new JSONObject();
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://192.168.1.85:8000/game/"+user_id+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,false);

                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        apiResponse.put("status",0);
                        apiResponse.put("body",response.getBodyArray());

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        apiResponse.put("status",1);
                        apiResponse.put("body",response.getBody());
                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        apiResponse.put("status",-1);
                        apiResponse.put("body",response.getBody());
                    } else {
                        apiResponse.put("status",-2);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return apiResponse;
        }

        // Process doInBackground() results
        @Override
        protected void onPostExecute(JSONObject response) {

            try {

                ArrayList<Badge> data = new ArrayList<Badge>();
                if (response.getInt("status") == 0) {
                    JSONArray gameReportArray = response.getJSONArray("body");
                    JSONObject gameReport;

                    for (int i = 0; i < gameReportArray.length(); i++) {
                        gameReport = gameReportArray.getJSONObject(i);

                        int status = gameReport.getInt("point_status");
                        String cause = gameReport.getString("cause");
                        int points = gameReport.getInt("point_value");
                        Date date = java.sql.Date.valueOf(gameReport.getString("point_date"));

                        data.add(0,new Badge(status, cause, points, date));
                    }

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
                                int gameReport = ((Badge) entry).getPoints();
                                Date date = ((Badge) entry).getDate();

                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy",Locale.US);
                                String date_formatted = dateFormat.format(date);

                                String[] dateParts = date_formatted.split("-");
                                String day = dateParts[0];
                                String month =  dateParts[1] + " " + dateParts[2];

                                if (status == 0) {
                                    pointStatus.setImageResource(R.drawable.p_rojo);
                                } else if (status == 1) {
                                    pointStatus.setImageResource(R.drawable.p_amarillo);
                                } else {
                                    pointStatus.setImageResource( R.drawable.p_verde);
                                }

                                reportType.setText(type);
                                reportPoints.setText("+"+gameReport);
                                reportDay.setText(day);
                                reportDate.setText(month);

                            }
                        }
                    });

                } else {
                    Toast.makeText(GameProfileActivity.this,R.string.error,Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

            progressBar.setVisibility(View.GONE);
        }
    }


}