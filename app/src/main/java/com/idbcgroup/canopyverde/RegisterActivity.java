package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullname, username, email;
    private Spinner country, city;
    private boolean verified,fullname_field,username_field,email_field,country_field,city_field;

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
        country = (Spinner) findViewById(R.id.country);
        city = (Spinner) findViewById(R.id.city);


/*
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(RegisterActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.country_array));

        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(countryAdapter)
*/

        country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position!=0){
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.fontColor));
                    // Toast.makeText(getBaseContext(),parent.getItemAtPosition(position)+" Selected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position!=0){
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.fontColor));
                    // Toast.makeText(getBaseContext(),parent.getItemAtPosition(position)+" Selected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void toLogin(View view){

        String fullname_text,username_text,email_text;

        fullname_text = fullname.getText().toString();
        username_text = username.getText().toString();
        email_text = email.getText().toString();

        fullname_field = fullname_text.length() != 0;
        username_field = username_text.length() != 0;
        email_field = email_text.length() != 0
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email_text).matches();

        country_field = !country.getSelectedItem().toString().equals("País");
        city_field = !city.getSelectedItem().toString().equals("Ciudad");

        verified = verifyFields(fullname_field,username_field,email_field,country_field,city_field);

        if (verified) {
            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(getBaseContext(),"Fields must be filled",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean verifyFields(boolean fullname_field, boolean username_field, boolean email_field, boolean country_field, boolean city_field){
        if(!fullname_field)
            fullname.setError( "Full name is required!" );
        if(!username_field)
            username.setError( "Username is required!" );
        if(!email_field)
            email.setError( "Valid Email is required!" );
        //if(!country_field)
            // Toast.makeText(getBaseContext(),"Choose a Country",Toast.LENGTH_SHORT).show();
        //if(!city_field)
            //Toast.makeText(getBaseContext(),"Choose a City",Toast.LENGTH_SHORT).show();
        return (fullname_field && username_field && email_field && country_field && city_field);
    }


}
