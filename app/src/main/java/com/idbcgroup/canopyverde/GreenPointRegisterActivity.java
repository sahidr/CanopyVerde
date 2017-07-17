package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GreenPointRegisterActivity extends AppCompatActivity {

    private SharedPreferences pref_marker;
    private TextView current_location;
    private int MAX_LINES = 2;
    private ImageView photoCapture;
    Bitmap thumbnail;
    private String location;
    private Spinner canopySize,stemSize,heightSpinner,treeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_green_point_register);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        pref_marker = getSharedPreferences("Marker", 0);
        double lat = Double.longBitsToDouble( pref_marker.getLong( "lat", -1 ));
        double lon = Double.longBitsToDouble( pref_marker.getLong( "long", -1 ));

        Bundle extras = getIntent().getExtras();

        if(extras == null) {
            thumbnail= null;
        } else {
            thumbnail = (Bitmap) getIntent().getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        }

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;

        try {

            addresses = geocoder.getFromLocation(lat, lon, MAX_LINES);
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
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            assert thumbnail != null;
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            photoCapture.setImageBitmap(thumbnail);
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

        //LOAD DATA INTO DATABASE

        Intent i = getIntent();
        i.putExtra("location",location);
        i.putExtra("canopy",canopy);
        i.putExtra("stem",stem);
        i.putExtra("height", height);
        i.putExtra("type",type);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}