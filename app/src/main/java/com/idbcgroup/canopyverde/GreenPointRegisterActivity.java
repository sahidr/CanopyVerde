package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GreenPointRegisterActivity extends AppCompatActivity {

    private static final int UNVERIFIED = 1;
    private SharedPreferences pref_marker,pref_session;
    private TextView current_location;
    private int MAX_LINES = 2;
    private ImageView photoCapture;
    Bitmap thumbnail;
    private String location;
    private Spinner canopySize,stemSize,heightSpinner,treeType;
    private String imageName,email;
    private int id;
    double latitude, longitude;
    private String encodedString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_green_point_register);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        pref_session = getSharedPreferences("Session", 0);
        email = pref_session.getString("email",null);
        id = pref_session.getInt("id",0);

        pref_marker = getSharedPreferences("Marker", 0);
        latitude = Double.longBitsToDouble( pref_marker.getLong( "lat", -1 ));
        longitude = Double.longBitsToDouble( pref_marker.getLong( "long", -1 ));

        imageName = (String) getIntent().getExtras().get("NAME");

        if(imageName == null) {
            thumbnail= null;
        } else {
            thumbnail = BitmapFactory.decodeFile(
                    Environment.getExternalStorageDirectory()+ "/CanopyVerde/"+imageName);
        }
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;

        try {

            addresses = geocoder.getFromLocation(latitude, longitude, MAX_LINES);
            String address = addresses.get(0).getAddressLine(0);
            // If any additional address line present than only,
            // check with max available address lines by getMaxAddressLineIndex()
            String address1 = addresses.get(1).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();

            String complete_location =  address+", "+address1+", "+state+city;
            location = address;
            current_location = (TextView) findViewById(R.id.location);
            current_location.setText(complete_location);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void imagePreview (View view){

        final Dialog dialog = new Dialog(GreenPointRegisterActivity.this);
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

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "CanopyVerde");
        imagesFolder.mkdirs(); // <----
        Calendar calendar = Calendar.getInstance();
        java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());
        SimpleDateFormat date_name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        imageName = date_name.format(date);
        File image = new File(imagesFolder, imageName);
        Uri uriSavedImage = Uri.fromFile(image);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage); // set the image file imageName
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            Bitmap image = BitmapFactory.decodeFile(
                    Environment.getExternalStorageDirectory()+ "/CanopyVerde/"+imageName);
            photoCapture.setImageBitmap(image);
        }
    }

    public void yellowPointRegister(View view){

        canopySize = (Spinner) findViewById(R.id.canopySize);
        String canopy  = (String) canopySize.getSelectedItem();

        heightSpinner = (Spinner) findViewById(R.id.height);
        String height = (String) heightSpinner.getSelectedItem();

        stemSize = (Spinner) findViewById(R.id.stemSize);
        String stem  = (String) stemSize.getSelectedItem();

        treeType = (Spinner) findViewById(R.id.treeType);
        String type  = (String) treeType.getSelectedItem();


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Must compress the Image to reduce image size to make upload easy
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byte_arr = stream.toByteArray();
        // Encode Image to String
        encodedString = Base64.encodeToString(byte_arr, 0);

        PostYellowPoint p = new PostYellowPoint();
        p.execute(String.valueOf(latitude),String.valueOf(longitude),canopy,stem,height,type,location,
                String.valueOf(UNVERIFIED), String.valueOf(id),encodedString);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    // AsyncTask. Sends Log In's data to the server's API and process the response.

    public class PostYellowPoint extends AsyncTask<String, Integer, Integer> {

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
                url = new URL("http://192.168.1.85:8000/greenpoint/");
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

                        JSONObject jsonResponse = response.getBody();
                        Log.d("BODY", String.valueOf(response.getBody()));
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

        // Process doInBackground() results
        @Override
        protected void onPostExecute(Integer anInt) {
            String message;
            Intent i = getIntent();
            switch (anInt) {
                case (-1):
                    message = "Ha habido un problema conectando con el servidor, intente de nuevo más tarde";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED, i);
                    finish();
                    //progressBar.setVisibility(View.GONE);
                    break;
                case (0):
                    //Intent intent = new Intent(getBaseContext(), DashboardActivity.class);
                    message = "¡Yellow Point Added!";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, i);
                    finish();
                    //startActivity(intent);
                    //progressBar.setVisibility(View.GONE);
                    break;
                case (1):
                    message = "Invalid data";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED, i);
                    finish();
                    //progressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

    }
}