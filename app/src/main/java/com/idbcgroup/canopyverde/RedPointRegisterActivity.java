package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
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
    private SharedPreferences pref_session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_point_register);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        pref_session = getSharedPreferences("Session", 0);
        user_id = pref_session.getInt("id",0);
        treeType = (Spinner) findViewById(R.id.treeType);
        rp_id = getIntent().getExtras().getInt("id");
        location = getIntent().getExtras().getString("location");
        lat = getIntent().getExtras().getFloat("latitude");
        lng = getIntent().getExtras().getFloat("longitude");

        Log.w("LATLONG", String.valueOf(lat)+String.valueOf(lng));

    }

    public void redPointRegister(View view){

        String type  = (String) treeType.getSelectedItem();

        PutRedPoint p = new PutRedPoint();
        p.execute(String.valueOf(lat), String.valueOf(lng), location,type, String.valueOf(REQUESTED), String.valueOf(user_id));

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    // AsyncTask. Sends Log In's data to the server's API and process the response.

    public class PutRedPoint extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            JSONObject apiResponse = new JSONObject();
            URL url;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            try {
                url = new URL("http://192.168.1.85:8000/greenpoint/"+rp_id+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("PUT");

                String form = URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");
                form += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");
                form += "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(strings[2], "UTF-8");
                form += "&" + URLEncoder.encode("location", "UTF-8") + "=" + URLEncoder.encode(strings[3], "UTF-8");
                form += "&" + URLEncoder.encode("status", "UTF-8") + "=" + URLEncoder.encode(strings[4], "UTF-8");
                form += "&" + URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(strings[5], "UTF-8");

                Log.d("FORM", String.valueOf(form));

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(String.valueOf(form));
                writer.flush();
                urlConnection.getInputStream();

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,true);


                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {

                        Log.d("OK","ok");

                        result = 0;

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        Log.d("BAD","BAD");

                        JSONObject jsonResponse = response.getBody();

                        String responseMessage = jsonResponse.getJSONArray("non_field_errors").getString(0);
                        if (responseMessage.equals("Unable to log in with provided form.")) {
                            result = 1;
                        }
                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        JSONObject jsonResponse = response.getBody();
                        String responseMessage = jsonResponse.getString("detail");
                        Log.d("NOTFOUND", responseMessage);
                        if (responseMessage.equals("Not found.")) {
                            result = -1;
                        }
                    }
                    Log.w("RESPONSE", String.valueOf(response.getBody()));

                }
            } catch (Exception e) {
                return -1;
            }
            return result;
        }

        // Process doInBackground() results
        @Override
        protected void onPostExecute(Integer anInt) {
            String message;
            switch (anInt) {
                case (-1):
                    message = "Ha habido un problema conectando con el servidor, intente de nuevo más tarde";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    //progressBar.setVisibility(View.GONE);
                    break;
                case (0):
                    //Intent intent = new Intent(getBaseContext(), DashboardActivity.class);
                    message = "¡Red Point Registered!";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    finish();
                    //startActivity(intent);
                    //progressBar.setVisibility(View.GONE);
                    break;
                case (1):
                    message = "Invalid Data";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    //progressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

    }

}
