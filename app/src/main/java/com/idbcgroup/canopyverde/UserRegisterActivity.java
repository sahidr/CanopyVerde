package com.idbcgroup.canopyverde;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static java.util.Collections.sort;

public class UserRegisterActivity extends AppCompatActivity {

    private EditText full_name, username, email;
    private TextInputEditText password;
    private Spinner country, city;
    private final ArrayList<String> countryList = new ArrayList<>();
    private final ArrayList<String> citiesList = new ArrayList<>();
    private JSONObject countriesAndCities;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        progressBar = (ProgressBar) findViewById(R.id.load);
        full_name = (EditText) findViewById(R.id.fullName);
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        password = (TextInputEditText) findViewById(R.id.passwordedit);
        country = (Spinner) findViewById(R.id.country);
        city = (Spinner) findViewById(R.id.city);

        GetCountriesAndCities countries_and_cities = new GetCountriesAndCities();
        countries_and_cities.execute();
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
     * Method of the button Register, takes the data provided by the user form and send it to
     * the server for the user registration
     * @param view the button of the view
     */
    public void userRegister(View view){

        String full_name_text,username_text,email_text,password_text,country_text, city_text;
        full_name_text = full_name.getText().toString();
        username_text = username.getText().toString();
        email_text = email.getText().toString();
        password_text = password.getText().toString();
        country_text = country.getSelectedItem().toString();
        city_text = city.getSelectedItem().toString();

        boolean full_name_field = full_name_text.length() != 0;
        // Compares if there any white spaces in the username field
        boolean username_field = username_text.length() != 0 && username_text.matches("\\S+");
        boolean email_field = email_text.length() != 0
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email_text).matches();
        boolean password_field = password_text.length() >= 8;

        // Compares if the item selected was the literals País and Ciudad of the countries_to_cities JSON
        boolean country_field = !country_text.equals(getString(R.string.country));
        boolean city_field = !city_text.equals(getString(R.string.city));

        boolean verified = verifyFields(full_name_field, username_field, email_field, password_field,
                country_field, city_field);

        if (verified) {
            PostUser postUser = new PostUser();
            postUser.execute(username_text,email_text,password_text,full_name_text,country_text,city_text);
        }
    }

    /**
     * Method for the validations of the user data
     * @param full_name_field boolean that represent if Fullname is valid
     * @param username_field boolean that represent if Username is valid
     * @param email_field boolean that represent if Email is valid
     * @param password_field boolean that represent if Password is valid
     * @param country_field boolean that represent if Country is valid
     * @param city_field boolean that represent if City is valid
     * @return boolean that represent if all of the fields are valid or invalid
     */
    private boolean verifyFields(boolean full_name_field, boolean username_field, boolean email_field,
                                 boolean password_field, boolean country_field, boolean city_field) {
        if (!full_name_field) {
            full_name.setError(getString(R.string.fullname_valid));
            full_name.setBackgroundResource(R.drawable.first_field_error);
        } else
            full_name.setBackgroundResource(R.drawable.first_field);
        if (!username_field){
            username.setError(getString(R.string.username_valid));
            username.setBackgroundResource(R.drawable.field_error);
        } else
            username.setBackgroundResource(R.drawable.field);
        if(!email_field) {
            email.setError(getString(R.string.email_valid));
            email.setBackgroundResource(R.drawable.field_error);
        } else
            email.setBackgroundResource(R.drawable.field);
        if(!password_field) {
            password.setError(getString(R.string.password_valid));
            password.setBackgroundResource(R.drawable.field_error);
        }else
            password.setBackgroundResource(R.drawable.field);
        if(!country_field) {
            country.setBackgroundResource(R.drawable.field_error);
        }else
            country.setBackgroundResource(R.drawable.field);
        if(!city_field) {
            city.setBackgroundResource(R.drawable.last_field_error);
        }else
            city.setBackgroundResource(R.drawable.last_field);
        return (full_name_field && username_field && email_field && password_field && country_field
                && city_field);
    }

    /**
     * AsyncTask class who reads the Countries and Cities XML in the resourses for the Spinners
     */
    private class GetCountriesAndCities extends AsyncTask <Void, Void, ArrayList<String>> {

        /**
         * Async Task class that takes the JSON countries_to_cities and parser into the spinners
         * of Country and City respectively
         * @param params this class won't take additional parameters
         * @return the list of countries of the JSON in ArrayList format
         */
        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            InputStream inputStream = getResources().openRawResource(R.raw.countries_to_cities);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int control;
            try {
                control = inputStream.read();
                while (control != -1) {
                    byteArrayOutputStream.write(control);
                    control = inputStream.read();
                }
                inputStream.close();
                countriesAndCities = new JSONObject(byteArrayOutputStream.toString());
                JSONArray countries = countriesAndCities.names();
                for (int i = 0; i < countries.length(); i++) {
                    String country = countries.get(i).toString();
                    countryList.add(country);
                }
                sort(countryList);
                countryList.remove(getString(R.string.country));
                // Add first the placeholder País
                countryList.add(0,getString(R.string.country));

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return countryList;
        }

        /**
         * After processing the countries of the JSON this method will assign the respectively cities
         * in the city spinner
         * @param result the country array list from the doInBackground process
         */
        @Override
        protected void onPostExecute(ArrayList<String> result) {

            final ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(UserRegisterActivity.this,
                    R.layout.spinner_item, result);
            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            country.setAdapter(countryAdapter);
            country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                /**
                 * This method implement the onItemSelected of the Interface
                 * OnItemSelectedListener, change the color of the text if the item is
                 * the placeholder and updates the city depending of the country selected
                 * @param parent the adapterView of the spinner
                 * @param view the Spinner who holds the list
                 * @param position the position of the item in the list
                 * @param id of the item selected
                 */
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    final int font_color = ResourcesCompat.getColor(getResources(), R.color.fontColor, null);
                    try {
                        if (position!=0){

                            ((TextView) parent.getChildAt(0)).setTextColor(font_color);
                        }
                        JSONArray cities = countriesAndCities.getJSONArray(countryList.get(position));
                        if (!citiesList.isEmpty()) citiesList.clear();
                        for (int i = 0; i < cities.length(); i++) {
                            String city = cities.get(i).toString();
                            citiesList.add(city);
                        }
                        sort(citiesList);
                        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(UserRegisterActivity.this,
                                R.layout.spinner_item, citiesList);
                        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        city.setAdapter(cityAdapter);
                        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            /**
                             * This method implement the onItemSelected of the Interface
                             * OnItemSelectedListener, change the color of the text if the item is
                             * the placeholder
                             * @param parent the adapterView of the spinner
                             * @param view the Spinner who holds the list
                             * @param position the position of the item in the list
                             * @param id of the item selected
                             */
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (!parent.getItemAtPosition(position).toString().equals("Ciudad")){
                                    ((TextView) parent.getChildAt(0)).setTextColor(font_color);
                                }
                            }

                            /**
                             * The interface must implement the onNothingSelected method but there's
                             * no action associated
                             * @param parent the adapterView of the spinner
                             */
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                /**
                 * The interface must implement the onNothingSelected method but there's
                 * no action associated
                 * @param parent the adapterView of the spinner
                 */
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    /**
     * AsyncTask class for the user register
     */
    private class PostUser extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection;
            Integer result = -1;
            try {
                url = new URL("https://canopy-verde.herokuapp.com/profile/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(10000);

                // The User Serializer in the server is based on a Nested Serializer
                // The user represents the UserSerializer
                JSONObject user = new JSONObject();
                user.put("username",strings[0]);
                user.put("email",strings[1]);
                user.put("password",strings[2]);

                // The profile is nested to the user, it requires the user associated
                JSONObject profile = new JSONObject();
                profile.put("fk_user",user);
                profile.put("fullname",strings[3]);
                profile.put("country",strings[4]);
                profile.put("city",strings[5]);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(profile.toString());
                writer.flush();
                APIResponse response = JSONResponseController.getJsonResponse(urlConnection, true);

                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_CREATED) {
                        result = 0;
                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        result = 1;
                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        result = -1;
                    }
                }
            } catch (Exception e) {
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
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    break;
                case (0):
                    message = R.string.successful_register;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
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
