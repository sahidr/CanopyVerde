package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SharedPreferences pref_marker;
    private MapStyleOptions style;
    private Context context;
    private Bitmap m_tree;
    private Date m_date;
    private String m_type, m_size, m_location, m_image, m_email, m_profile, m_user;
    private int m_status;
    private TextView greenIndex, populationDensity;
    private RelativeLayout stats;
    private SharedPreferences pref_session;

    private static final LatLng CARACAS = new LatLng(10.4806, -66.9036);
    private static int MAX_FRACTION_DIGITS = 1;
    private static final int UNREQUESTED = -1;
    private static final int REQUESTED = 0;
    private static final int UNVERIFIED = 1;
    private static final int VERIFIED = 2;
    private static int HEIGHT = 32;
    private static int WIDTH = 32;
    private static int REQUEST_CAMERA = 0;
    private static int REQUEST_GREEN_POINT_REGISTER = 1;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context = this;
        stats = (RelativeLayout) findViewById(R.id.stats);
        greenIndex = (TextView) findViewById(R.id.greenViewIndexPercent);

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US); //Italian for Latin
        formatter.setMaximumFractionDigits(MAX_FRACTION_DIGITS);

        float green_index = (float) 100.001;
        float pop_density = (float) 100000;

        String percent = getResources().getString(R.string.percent, formatter.format(green_index));
        greenIndex.setText(percent + "%");

        populationDensity = (TextView) findViewById(R.id.populationDensityUnits);
        String density = getResources().getString(R.string.density, formatter.format(pop_density));
        populationDensity.setText(density);

    }

    /**
     * Manipulates the map_circle once available.
     * This callback is triggered when the map_circle is ready to be used.
     * This is where we can add latlong or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        style = MapStyleOptions.loadRawResourceStyle(this, R.raw.canopy_style_map);
        mMap.setMapStyle(style);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(CARACAS)); //CARACAS
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);

        Get g = new Get();
        g.execute();

      if (ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMinZoomPreference(12);
        mMap.setMaxZoomPreference(22);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        stats.animate().alpha(0.0f);
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        GreenPoint rp = (GreenPoint) marker.getTag();  //RED POINT DATA
        assert rp != null;
        m_status = rp.getStatus();

        if (m_status == -1) {
            startActivity(new Intent(MapsActivity.this, RedPointRegister.class));
        }
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        stats.animate().alpha(1f);
    }

    public void currentLocation(View view) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.d("NETWORK", "Connection Error");
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setMessage(context.getResources().getString(R.string.enable_location));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            //Open OS Location Settings
                            Intent enableLocation = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(enableLocation);
                        }
                    });
            dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int paramInt) {
                    dialogInterface.dismiss();
                }
            });
            dialog.show();
        } else {
            try {
                LatLng latLng = new LatLng(mMap.getMyLocation().getLatitude(),
                        mMap.getMyLocation().getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                mMap.animateCamera(cameraUpdate);
            } catch (Exception e) {
                Log.d("GPS", "Location Error");
            }
        }
    }

    public void cameraView(View view) {
        CameraPosition cameraPosition = mMap.getCameraPosition();
        SharedPreferences.Editor markerEditor = getSharedPreferences("Marker", 0).edit();
        markerEditor.putLong("lat", Double.doubleToRawLongBits(cameraPosition.target.latitude));
        markerEditor.putLong("long", Double.doubleToRawLongBits(cameraPosition.target.longitude));
        markerEditor.apply();
        cameraIntent();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "CanopyVerde");
        imagesFolder.mkdirs(); // <----

        Calendar calendar = Calendar.getInstance();
        java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());
        SimpleDateFormat date_name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        name = date_name.format(date);
        File image = new File(imagesFolder, name);
        Uri uriSavedImage = Uri.fromFile(image);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage); // set the image file name
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // CAMERA RESULT
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                Intent i = new Intent(MapsActivity.this, GreenPointRegisterActivity.class);
                i.putExtras(data);
                i.putExtra("NAME", name);
                startActivityForResult(i, REQUEST_GREEN_POINT_REGISTER);
            }
            // GREEN POINT REGISTER RESULT

        } else if (requestCode == REQUEST_GREEN_POINT_REGISTER) {

            Log.d("RESULT_OK","OK");
        }
    }

    public void profileView(View view) {
        startActivity(new Intent(MapsActivity.this, UserProfileActivity.class));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public class Get extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject apiResponse = new JSONObject();
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://192.168.1.85:8000/greenpoint/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,false);
                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        apiResponse.put("status",0);
                        apiResponse.put("body",response.getBodyArray());

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        apiResponse.put("status",1);
                        apiResponse.put("body",response.getBody());
                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        apiResponse.put("status",-1);
                        apiResponse.put("body",response.getBody());
                    } else {
                        apiResponse.put("status",-2);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return apiResponse;
        }

        // Process doInBackground() results
        @Override
        protected void onPostExecute(JSONObject response) {

            try {
                //Log.d("RESPONSE POST",String.valueOf(response.getInt("status")));

               if (response.getInt("status") == 0) {
                   JSONArray pointsArray = response.getJSONArray("body");
                   JSONObject points;

                   GreenPoint gp, rp;

                   for (int i = 0; i < pointsArray.length(); i++) {
                       points = pointsArray.getJSONObject(i);
                       Float latitud = Float.parseFloat(points.getString("latitud"));
                       Float longitud = Float.parseFloat(points.getString("longitud"));
                       int status = points.getInt("status");
                       String location = points.getString("location");
                       Date date = java.sql.Date.valueOf(points.getString("date"));

                       Log.d("LATITUD", String.valueOf(latitud));
                       Log.d("LONGITUD", String.valueOf(longitud));
                       Log.d("LOCATION", String.valueOf(location));
                       Log.d("DATE", String.valueOf(date));
                       Log.d("STATUS", String.valueOf(status));

                       gp = new GreenPoint();
                       gp.setLocation(location);
                       gp.setDate(date);
                       gp.setImage("");
                       gp.setStatus(status);
                       int drawable = R.drawable.p_amarillo;

                       if (status == 1 || status == 2){
                           int canopy = points.getInt("canopy");
                           int stem = points.getInt("stem");
                           int height = points.getInt("height");
                           String type = points.getString("type");
                           String user = points.getString("user");

                           Log.d("CANOPY", String.valueOf(canopy));
                           Log.d("STEM", String.valueOf(stem));
                           Log.d("HEIGHT", String.valueOf(height));
                           Log.d("TYPE", String.valueOf(type));
                           Log.d("USER", user);
                           gp.setUser(user);
                           gp.setTreeType(type);
                           gp.setHeight(String.valueOf(height));
                       } else if (status == 0){
                           String type = points.getString("type");
                           String user = points.getString("user");
                           Log.d("TYPE", String.valueOf(type));
                           Log.d("USER", user);
                           gp.setUser(user);
                           gp.setTreeType(type);
                       }

                       switch (status){
                           case -1:
                               drawable = R.drawable.p_rojo;
                               break;
                           case 0:
                               drawable = R.drawable.p_rojo;
                               break;
                           case 1:
                               drawable = R.drawable.p_amarillo;
                               break;
                           case 2:
                               drawable = R.drawable.p_verde;
                               break;
                       }

                       BitmapDrawable color = (BitmapDrawable) getResources().getDrawable(drawable);


                       Bitmap color_scaled = Bitmap
                               .createScaledBitmap(color.getBitmap(), WIDTH, HEIGHT, false);

                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitud, longitud))
                                .icon(BitmapDescriptorFactory.fromBitmap(color_scaled))
                                .anchor(0.5f, 0.4f)
                        );
                        m.setTag(gp);
                   }
               } else {
                   Toast.makeText(MapsActivity.this,"FailtoLoad",Toast.LENGTH_SHORT).show();
               }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    /** Demonstrates customizing the info window and/or its contents. */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View greenPointWindow;
        private final View redPointWindow;

        CustomInfoWindowAdapter() {
            greenPointWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            redPointWindow = getLayoutInflater().inflate(R.layout.red_points_dialog, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            View markerView;
            GreenPoint gp = (GreenPoint) marker.getTag();  //GREEN POINT DATA
            assert gp != null;
            m_status = gp.getStatus();

            if (m_status == 0 || m_status == -1) {
                renderRedPoint(marker, redPointWindow);
                markerView = redPointWindow;
            } else {
                renderGreenPoint(marker, greenPointWindow);
                markerView = greenPointWindow;
            }
            return markerView;
        }


        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void renderRedPoint(Marker marker, View view){
            GreenPoint rp = (GreenPoint) marker.getTag();  //RED POINT DATA
            assert rp != null;
            m_status = rp.getStatus();
            m_location = rp.getLocation();

            TextView available = (TextView) view.findViewById(R.id.available);
            TextView plant = (TextView) view.findViewById(R.id.plant);

            if (m_status == -1){
                available.setText(R.string.available);
                plant.setText(R.string.plant);

            } else {
                available.setText(R.string.occupied);
                m_user = rp.getUser();
                plant.setText(m_user);
            }
        }

        private void renderGreenPoint(Marker marker, View view) {

            GreenPoint gp = (GreenPoint) marker.getTag();  //GREEN POINT DATA
            assert gp != null;
            m_status = gp.getStatus();
            m_date = gp.getDate();
            m_type = gp.getTreeType();
            m_email = gp.getUser();
            m_location = gp.getLocation();
            m_image = gp.getImage();
            m_size = gp.getHeight();

            PorterShapeImageView tree = (PorterShapeImageView) view.findViewById(R.id.treePic);
            PorterShapeImageView profile = (PorterShapeImageView) view.findViewById(R.id.profile);
            TextView user = (TextView) view.findViewById(R.id.user);
            TextView date = (TextView) view.findViewById(R.id.p_date);
            TextView type = (TextView) view.findViewById(R.id.p_type);
            TextView size = (TextView) view.findViewById(R.id.p_size);
            TextView status = (TextView) view.findViewById(R.id.p_status);
            TextView location = (TextView) view.findViewById(R.id.location);

            // from email get username and profile pic

            pref_session = getSharedPreferences("Session", 0);
            m_profile = pref_session.getString("photo",null);

            String username = pref_session.getString("username",null);

            if (m_email.equals("idbcgroup@gmail.com")) {
                user.setText("@idbcgroup");
                profile.setImageResource(R.drawable.btn_locate);
            }else {
                user.setText(username);
                if (m_profile != null){
                    Uri photo = Uri.parse(m_profile);
                    Picasso.with(MapsActivity.this).load(photo).into(profile);
                } else {
                    Picasso.with(MapsActivity.this).load(R.drawable.btn_locate).into(profile);
                }
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy",Locale.US);
            String pointDate = dateFormat.format(m_date);
            date.setText(pointDate);

            type.setText(m_type);
            location.setText(m_location);

            if (m_size.equals("Altura Aproximada"))
                size.setText("Undefined");
            else size.setText(m_size+"m");

            if (m_image.equals("none")|| (m_image.equals(""))){
                tree.setImageResource(R.drawable.araguaney);
            } else {
                Uri photo = Uri.parse(m_image);
                Picasso.with(MapsActivity.this).load(photo).into(tree);
            }

            if (m_status == UNVERIFIED) {
                status.setTextColor(getResources().getColor(R.color.yellow));
                status.setText(getString(R.string.not_verified));
            } else if (m_status == VERIFIED){
                status.setTextColor(getResources().getColor(R.color.colorCanopy));
                status.setText(getString(R.string.verified));
            }
        }
    }

}