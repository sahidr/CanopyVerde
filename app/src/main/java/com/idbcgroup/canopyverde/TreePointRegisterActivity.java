package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TreePointRegisterActivity extends AppCompatActivity {

    private static final int UNVERIFIED = 1;
    private static final int MAX_LINES = 2;

    private ImageView photoCapture;
    private Bitmap thumbnail;
    private ProgressBar progressBar;
    private Spinner canopySize, heightSpinner, stemSize, treeType;
    private String location, city;
    private int id;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_green_point_register);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        progressBar = (ProgressBar) findViewById(R.id.load);

        canopySize = (Spinner) findViewById(R.id.canopySize);
        heightSpinner = (Spinner) findViewById(R.id.height);
        stemSize = (Spinner) findViewById(R.id.stemSize);
        treeType = (Spinner) findViewById(R.id.treeType);

        SharedPreferences pref_session = getSharedPreferences("Session", 0);
        id = pref_session.getInt("id",0);

        SharedPreferences pref_marker = getSharedPreferences("Marker", 0);
        latitude = Double.longBitsToDouble( pref_marker.getLong( "lat", -1 ));
        longitude = Double.longBitsToDouble( pref_marker.getLong( "long", -1 ));

        Bundle extras = getIntent().getExtras();

        if(extras == null) {
            thumbnail= null;
        } else {
            thumbnail = (Bitmap) getIntent().getExtras().get("data");
        }
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;

        try {

            // Get the address of the marker
            addresses = geocoder.getFromLocation(latitude, longitude, MAX_LINES);
            String address = addresses.get(0).getAddressLine(0);
            // If any additional address line present than only,
            // check with max available address lines by getMaxAddressLineIndex()
            String address1 = addresses.get(1).getAddressLine(0);
            city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();

            String complete_location =  address+", "+address1+", "+state+city;
            location = address;
            TextView current_location = (TextView) findViewById(R.id.location);
            current_location.setText(complete_location);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Shows a preview of the image took by the camera
     * @param view the eye button of the view
     */
    public void imagePreview (View view){

        final Dialog dialog = new Dialog(TreePointRegisterActivity.this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(getLayoutInflater().inflate(R.layout.image_preview,null));

        photoCapture = (ImageView) dialog.findViewById(R.id.photocaptured);

        Button use = (Button) dialog.findViewById(R.id.use);
        Button retake = (Button) dialog.findViewById(R.id.retake);
        photoCapture.setImageBitmap(thumbnail);

        use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIntent();
            }
        });
        dialog.show();
    }

    /**
     * Method that calls the Camera App of the device
     */
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    /**
     * Result of the camera app
     * @param requestCode identifies who's calling the Intent
     * @param resultCode identifies the result of the called Intent
     * @param data the data retrieved from the called Intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            Bitmap image = (Bitmap) data.getExtras().get("data");
            photoCapture.setImageBitmap(image);
        }
    }

    /**
     * Method that takes the data from the form and compress the image took by the camera
     * @param view the register button of the view
     */
    public void yellowPointRegister(View view){

        String canopy  = (String) canopySize.getSelectedItem();
        String height = (String) heightSpinner.getSelectedItem();
        String stem  = (String) stemSize.getSelectedItem();
        String type  = (String) treeType.getSelectedItem();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Must compress the Image to reduce image size to make upload easy
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byte_arr = stream.toByteArray();
        // Encode Image to String
        String encodedString = Base64.encodeToString(byte_arr, 0);

        boolean canopy_valid = !canopy.equals(getString(R.string.canopy));
        boolean height_valid = !height.equals(getString(R.string.height_aprox));
        boolean stem_valid = !stem.equals(getString(R.string.stem_size));
        boolean type_valid = !type.equals(getString(R.string.tree_type));
        boolean verified  = verifyFields(canopy_valid,height_valid,stem_valid,type_valid);

        if (verified){
            PostTree tree = new PostTree();
            tree.execute(String.valueOf(latitude),String.valueOf(longitude),canopy,stem,height,type,
                    location, String.valueOf(UNVERIFIED), String.valueOf(id), encodedString, city);
        }
    }

    /**
     * Method for the validations of the user data
     * @param canopy_field boolean that represent if the canopy size is valid
     * @param height_field boolean that represent if the height is valid
     * @param stem_field boolean that represent if the stem size is valid
     * @param type_field boolean that represent if the type is valid
     * @return boolean that represent if all of the fields are valid or invalid
     */
    private boolean verifyFields(boolean canopy_field, boolean height_field, boolean stem_field,
                                 boolean type_field) {
        if (!canopy_field) {
            canopySize.setBackgroundResource(R.drawable.first_field_error);
        } else
            canopySize.setBackgroundResource(R.drawable.first_field);
        if(!height_field) {
            heightSpinner.setBackgroundResource(R.drawable.field_error);
        } else
            heightSpinner.setBackgroundResource(R.drawable.field);
        if (!stem_field){
            stemSize.setBackgroundResource(R.drawable.field_error);
        } else
            stemSize.setBackgroundResource(R.drawable.field);
        if(!type_field) {
            treeType.setBackgroundResource(R.drawable.field_error);
        }else
            treeType.setBackgroundResource(R.drawable.field);
        return (canopy_field &&  height_field && stem_field && type_field);
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
     * AsyncTask. Sends the Login's data to the server's API and process the response.
     */
    private class PostTree extends AsyncTask<String, Integer, Integer> {

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
                url = new URL("https://canopy-verde.herokuapp.com/treepoint/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setReadTimeout(10000);

                JSONObject form = new JSONObject();

                form.put("latitude",strings[0]);
                form.put("longitude",strings[1]);
                form.put("canopy",strings[2]);
                form.put("stem",strings[3]);
                form.put("height",strings[4]);
                form.put("type",strings[5]);
                form.put("location",strings[6]);
                form.put("status",strings[7]);
                form.put("user",strings[8]);
                form.put("image",strings[9]);
                form.put("city",strings[10]);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(String.valueOf(form));
                writer.flush();
                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,true);

                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        Log.d("OK","ok");
                        result = 0;

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        Log.d("BAD","BAD");
                        result = 1;

                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        JSONObject jsonResponse = response.getBody();
                        String responseMessage = jsonResponse.getString("detail");
                        Log.d("NOTFOUND", responseMessage);
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

        /**
         * Process the int result of the doInBackground
         * @param result the result of the response
         */
        @Override
        protected void onPostExecute(Integer result) {
            int message;
            Intent i = getIntent();
            switch (result) {
                case (-1):
                    message = R.string.error;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED, i);
                    finish();
                    progressBar.setVisibility(View.GONE);
                    break;
                case (0):
                    message = R.string.tree_added;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, i);
                    finish();
                    progressBar.setVisibility(View.GONE);
                    break;
                case (1):
                    message = R.string.invalid_data;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED, i);
                    finish();
                    progressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

    }
}