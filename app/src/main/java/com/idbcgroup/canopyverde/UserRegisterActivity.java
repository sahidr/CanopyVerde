package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static java.util.Collections.sort;

public class UserRegisterActivity extends AppCompatActivity {

    private EditText fullname, username, email;
    TextInputEditText password;
    private Spinner country, city;
    private boolean verified,fullname_field,username_field,email_field, password_field,
            country_field,city_field;
    ArrayList<String> countryList = new ArrayList<String>();
    ArrayList<String> citiesList = new ArrayList<String>();
    JSONObject countriesAndCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        fullname = (EditText) findViewById(R.id.fullName);
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        password = (TextInputEditText) findViewById(R.id.passwordedit);
        country = (Spinner) findViewById(R.id.country);
        city = (Spinner) findViewById(R.id.city);

        GetData json = new GetData();
        json.execute();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void userRegister(View view){

        String fullname_text,username_text,email_text,password_text;

        fullname_text = fullname.getText().toString();
        username_text = username.getText().toString();
        email_text = email.getText().toString();
        password_text = password.getText().toString();

        fullname_field = fullname_text.length() != 0;
        username_field = username_text.length() != 0;
        email_field = email_text.length() != 0
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email_text).matches();
        password_field = password_text.length() >= 8;
        country_field = !country.getSelectedItem().toString().equals("País");
        city_field = !city.getSelectedItem().toString().equals("Ciudad");

        verified = verifyFields(fullname_field,username_field,email_field,password_field,country_field,city_field);

        if (verified) {
            Intent i = new Intent(UserRegisterActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }

    public boolean verifyFields(boolean fullname_field, boolean username_field, boolean email_field,
                                boolean password_field, boolean country_field, boolean city_field){
        if(!fullname_field)
            fullname.setError(getString(R.string.fullname_valid));
        if(!username_field)
            username.setError(getString(R.string.username_valid));
        if(!email_field)
            email.setError(getString(R.string.email_valid));
        //if(!password_field)
        //    password.setError(getString(R.string.password_valid));
        return (fullname_field && username_field && email_field && password_field && country_field
                && city_field);
    }


    private class GetData extends AsyncTask <Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            InputStream inputStream = getResources().openRawResource(R.raw.countries_to_cities);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int ctr;
            try {
                ctr = inputStream.read();
                while (ctr != -1) {
                    byteArrayOutputStream.write(ctr);
                    ctr = inputStream.read();
                }
                inputStream.close();
                countriesAndCities = new JSONObject(byteArrayOutputStream.toString());
                JSONArray countries = countriesAndCities.names();
                for (int i = 0; i < countries.length(); i++) {
                    String country = countries.get(i).toString();
                    countryList.add(country);
                }
                sort(countryList);
                countryList.remove("País");
                countryList.add(0,"País");

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return countryList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {

            final ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(UserRegisterActivity.this,
                    R.layout.spinner_item, result);
            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            country.setAdapter(countryAdapter);
            country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        if (position!=0){
                            ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.fontColor));
                        }

                        JSONArray cities = countriesAndCities.getJSONArray(countryList.get(position));

                        if (!citiesList.isEmpty()) citiesList.clear();

                        for (int i = 0; i < cities.length(); i++) {
                            String city = cities.get(i).toString();
                            citiesList.add(city);
                        }
                        sort(citiesList);

                        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(UserRegisterActivity.this,
                                R.layout.spinner_item, citiesList);

                        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        city.setAdapter(cityAdapter);
                        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (!parent.getItemAtPosition(position).toString().equals("Ciudad")){
                                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.fontColor));
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }
    }
}
