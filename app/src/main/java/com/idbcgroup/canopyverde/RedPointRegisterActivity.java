package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RedPointRegisterActivity extends AppCompatActivity {

    private static final int REQUESTED = 0;
    private int rp_id, user_id;
    private String location;
    private Float lat, lng;
    private Spinner treeType;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_point_register);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        SharedPreferences pref_session = getSharedPreferences("Session", 0);
        user_id = pref_session.getInt("id",0);
        treeType = (Spinner) findViewById(R.id.treeType);
        rp_id = getIntent().getExtras().getInt("id");
        location = getIntent().getExtras().getString("location");
        lat = getIntent().getExtras().getFloat("latitude");
        lng = getIntent().getExtras().getFloat("longitude");

        TextView location_form = (TextView) findViewById(R.id.location);
        location_form.setText(location);
        progressBar = (ProgressBar) findViewById(R.id.load);

    }

    /**
     * Method that takes the user form and send it to the server
     * @param view the register button of the view
     */
    public void redPointRegister(View view){
        String type = (String) treeType.getSelectedItem();
        PutRedPoint p = new PutRedPoint();
        p.execute(String.valueOf(lat), String.valueOf(lng), type, location,
                String.valueOf(REQUESTED), String.valueOf(user_id));
    }

    /**
     * Method of the Calligraphy Library to insert the font family in the context of the Activity
     * @param newBase the new base context of the Activity
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // Request a red point and update its data
    private class PutRedPoint extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection;
            Integer result = 0;
            try {
                url = new URL("https://canopy-verde.herokuapp.com/treepoint/"+rp_id+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("PUT");

                String form = URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");
                form += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");
                form += "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(strings[2], "UTF-8");
                form += "&" + URLEncoder.encode("location", "UTF-8") + "=" + URLEncoder.encode(strings[3], "UTF-8");
                form += "&" + URLEncoder.encode("status", "UTF-8") + "=" + URLEncoder.encode(strings[4], "UTF-8");
                form += "&" + URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(strings[5], "UTF-8");

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(String.valueOf(form));
                writer.flush();
                urlConnection.getInputStream();

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,true);

                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        result = 0;

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {

                        JSONObject jsonResponse = response.getBody();
                        String responseMessage = jsonResponse.getJSONArray("non_field_errors").getString(0);
                        if (responseMessage.equals("Unable to log in with provided form.")) {
                            result = 1;
                        }
                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        JSONObject jsonResponse = response.getBody();
                        String responseMessage = jsonResponse.getString("detail");
                        if (responseMessage.equals("Not found.")) {
                            result = -1;
                        }
                    }
                }
            } catch (Exception e) {
                return -1;
            }
            return result;
        }

        // Process doInBackground() results
        @Override
        protected void onPostExecute(Integer anInt) {
            int message;
            Intent i = getIntent();
            switch (anInt) {
                case (-1):
                    message = R.string.error;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    break;
                case (0):
                    message = R.string.successful_register;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, i);
                    finish();
                    break;
                case (1):
                    message = R.string.invalid_data;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            progressBar.setVisibility(View.GONE);
        }

    }

}
