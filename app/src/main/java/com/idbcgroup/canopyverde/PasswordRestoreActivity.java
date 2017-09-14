package com.idbcgroup.canopyverde;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PasswordRestoreActivity extends AppCompatActivity {

    private EditText email;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_restore);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        email = (EditText) findViewById(R.id.email);
        progressBar = (ProgressBar) findViewById(R.id.load);
    }

    /**
     * This Method takes the email provided by the user and attempt the pasword restore process
     * @param view the restore password button of the view
     */
    public void newPassword (View view){
        String user_email = String.valueOf(email.getText());
        PasswordRestore passwordRestore = new PasswordRestore();
        passwordRestore.execute(user_email);
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
     * Async Task that takes the email and send it to the server
     */
    private class PasswordRestore extends AsyncTask<String, Integer, Integer> {

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
                String email_change = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");
                URL url = new URL("https://canopy-verde.herokuapp.com/reset/password_reset");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(email_change);
                writer.flush();
                APIResponse response = JSONResponseController.getJsonResponse(connection,true);

                if (response != null){

                    if (response.getStatus()==HttpURLConnection.HTTP_OK) {
                        result = 0;
                        Log.d("OK", "OK");
                        return 0;

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
            int message;
            switch (anInt) {
                case (-1):
                    message = R.string.error;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    break;
                case (0):
                    message = R.string.email_confirmation;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    finish();
                    progressBar.setVisibility(View.GONE);
                    break;
                case (1):
                    message = R.string.user_account_doesnt_exists;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }


}
