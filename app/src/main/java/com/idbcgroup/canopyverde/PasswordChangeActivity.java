package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PasswordChangeActivity extends AppCompatActivity {

    private Uri data;
    private TextView password, confirm_password;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        progressBar = (ProgressBar) findViewById(R.id.load);

        data = getIntent().getData();
        password = (TextView) findViewById(R.id.newPassword);
        confirm_password = (TextView) findViewById(R.id.confirmPassword);

    }

    public void passwordChange (View view){

        String user_password = password.getText().toString();
        String user_confirm = confirm_password.getText().toString();

        PasswordChangeTask passwordChange = new PasswordChangeTask();
        passwordChange.execute(String.valueOf(data),user_password,user_confirm);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private class PasswordChangeTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Sends validated Log In's data to the server's API and process the response. Returns an
        // integer value ([-1..1):
        // * -1, if an error occurred during the communication
        // * 0, if everything went OK (redirecting to MainActivity and updating SharedPreferences afterwards)
        // * 1, if the credentials provided aren't valid
        @Override
        protected Integer doInBackground(String... strings) {

            Integer result = -1;
            try {
                // Defining and initializing server's communication's variables
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);

                JSONObject change = new JSONObject();
                change.put("password",strings[1]);
                change.put("confirm_password",strings[2]);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(String.valueOf(change));
                writer.flush();
                APIResponse response = JSONResponseController.getJsonResponse(connection,true);

                if (response != null){

                    if (response.getStatus()==HttpURLConnection.HTTP_OK) {
                        JSONObject response_body = response.getBody();
                        Log.d("OK", response_body.toString());
                        int status = response_body.getInt("status");
                        if (status == 200)
                            result = 0;
                        else
                            result = -1;
                        return result;

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        Log.d("BAD", "BAD");
                        result = 1;
                    } else {
                        Log.d("NOT", "FOUND");
                        result = -1;
                    }
                }

            } catch (MalformedURLException e) {
                return result;
            } catch (IOException e) {
                return result;
            } catch (JSONException e) {
                return result;
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
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    break;
                case (0):
                    message = "¡Password Restored!";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PasswordChangeActivity.this,LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                    break;
                case (1):
                    message = "Contraseñas deben ser iguales y de al menos 8 caracteres";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();

                    break;
                default:
                    break;
            }
            progressBar.setVisibility(View.GONE);
        }
    }

}
