package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
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

/**
 * Activity for the management of the user's Game Profile
 */
public class GameProfileActivity extends AppCompatActivity {

    private int user_id;
    private ListView gamerReportList;
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

        Context context = this;

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        PorterShapeImageView profilePic = (PorterShapeImageView) findViewById(R.id.profilepic);
        TextView profileFullName = (TextView) findViewById(R.id.fullNameDisplay);
        TextView profileUsername = (TextView) findViewById(R.id.usernameDisplay);
        TextView badge = (TextView) findViewById(R.id.badgeName);
        gamerReportList = (ListView) findViewById(R.id.reward_list);
        SharedPreferences pref_session = getSharedPreferences("Session", 0);

        /*
         * GetUserGameProfile User data from Shared Preferences
         */
        String full_name = pref_session.getString("fullname",null);
        String username = pref_session.getString("username",null);
        String profile_pic = pref_session.getString("photo",null);
        int game_points = pref_session.getInt("game_points",0);
        String badge_name = pref_session.getString("badge",null);
        user_id = pref_session.getInt("id",0);

        GetUserGameProfile game_profile = new GetUserGameProfile();
        game_profile.execute();

        if (profile_pic!=null) {
            Uri photo = Uri.parse(profile_pic);
            Picasso.with(context).load(photo).into(profilePic);
        }

        profileUsername.setText("@"+username);
        profileFullName.setText(full_name);

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ITALIAN);
        String gameReport = getResources().getString(R.string.badge_name_example, formatter.format(game_points),
                badge_name);
        badge.setText(gameReport);
    }

    /**
     * This Method allows the user to go back to the UserProfileActivity
     * @param view the button of User in the View
     */
    public void backToProfile(View view){
        onBackPressed();
    }

    /**
     * This Method allows the user to go to the UserProfileActivity
     * @param view the button of Map in the View
     */
    public void goToMap(View view){
        startActivity(new Intent(GameProfileActivity.this,MapActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    /**
     * Method of the Calligraphy Library to insert the font family in the context of the Activity
     * @param newBase the new base context of the Activity
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    /**
     * AsyncTask created to get the latest data of the user's Game Profile
     */
    private class GetUserGameProfile extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject apiResponse = new JSONObject();
            URL url;
            HttpURLConnection urlConnection;

            try {
                //  The URL must contain the user id of the profile requested
                url = new URL("https://canopy-verde.herokuapp.com/game/"+user_id+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);

                // The response body would be an array
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


        /**
         * This method process the Array of game reports contained in the APIResponse
         * @param response the body of the request's response that contains a status and the array
         *                 of game reports
         */
        @Override
        protected void onPostExecute(JSONObject response) {

            try {

                ArrayList<GameReport> data = new ArrayList<>();
                if (response.getInt("status") == 0) {
                    JSONArray gameReportArray = response.getJSONArray("body");
                    JSONObject gameReport;

                    for (int i = 0; i < gameReportArray.length(); i++) {
                        gameReport = gameReportArray.getJSONObject(i);

                        int status = gameReport.getInt("point_status");
                        String cause = gameReport.getString("cause");
                        int points = gameReport.getInt("point_value");
                        Date date = java.sql.Date.valueOf(gameReport.getString("point_date"));

                        data.add(0,new GameReport(status, cause, points, date));
                    }

                    gamerReportList.setAdapter(new ListAdapter(GameProfileActivity.this, R.layout.badge_list_row, data) {

                        /* This will implement the abstract method onEntry(Implemented in ListAdapter), with
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

                                int status = ((GameReport) entry).getStatus();
                                String type = ((GameReport) entry).getReportType();
                                int gameReport = ((GameReport) entry).getPoints();
                                Date date = ((GameReport) entry).getDate();

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