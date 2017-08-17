package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.squareup.picasso.Callback;
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

    private GoogleMap mMap;
    private MapStyleOptions style;
    private Context context;
    private Date m_date;
    private String m_type, m_size, m_location, m_username;
    private int m_status;
    private ImageView m_image, m_profile;
    private TextView greenIndex, populationDensity;
    private RelativeLayout stats;
    private SharedPreferences pref_session;


    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
/*
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
  */
        context = this;
        stats = (RelativeLayout) findViewById(R.id.stats);
        greenIndex = (TextView) findViewById(R.id.greenViewIndexPercent);

        GetStats s = new GetStats();
        s.execute("Caracas");

        setupMapIfNeeded();


/*
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US); //Italian for Latin
        formatter.setMaximumFractionDigits(MAX_FRACTION_DIGITS);

        float green_index = (float) 100.001;
        float pop_density = (float) 100000;

        String percent = getResources().getString(R.string.percent, formatter.format(green_index));
        greenIndex.setText(percent + "%");

        populationDensity = (TextView) findViewById(R.id.populationDensityUnits);
        String density = getResources().getString(R.string.density, formatter.format(pop_density));
        populationDensity.setText(density);
*/
    }

    private void setupMapIfNeeded() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MapStateManager mgr = new MapStateManager(this);
        mgr.saveMapState(mMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMapIfNeeded();
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
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(CARACAS)); //CARACAS
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMinZoomPreference(12);
        mMap.setMaxZoomPreference(20);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
            mMap.setMapType(mgr.getSavedMapType());
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(CARACAS)); //CARACAS
        }


        GetGreenPoints g = new GetGreenPoints();
        g.execute();

        if (ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

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
        int rp_id = rp.getId();
        Float lat = rp.getLatitude();
        Float lng = rp.getLongitude();
        String rp_location = rp.getLocation();

        if (m_status == -1) {
            Intent i =new Intent(MapsActivity.this, RedPointRegisterActivity.class);
            i.putExtra("id",rp_id);
            i.putExtra("latitude",lat);
            i.putExtra("longitude",lng);
            i.putExtra("location",rp_location);
            startActivity(i);
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
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // CAMERA RESULT
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                Intent i = new Intent(MapsActivity.this, GreenPointRegisterActivity.class);
                i.putExtras(data);
                startActivityForResult(i, REQUEST_GREEN_POINT_REGISTER);
            }
            // GREEN POINT REGISTER RESULT

        } else if (requestCode == REQUEST_GREEN_POINT_REGISTER) {
            //GetGreenPoints g = new GetGreenPoints();
            //g.execute();
            finish();
            startActivity(getIntent());

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

    public class GetGreenPoints extends AsyncTask<String, Integer, JSONObject> {
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

                    GreenPoint gp;
                    for (int i = 0; i < pointsArray.length(); i++) {
                        points = pointsArray.getJSONObject(i);
                        Float latitude = Float.parseFloat(points.getString("latitude"));
                        Float longitude = Float.parseFloat(points.getString("longitude"));
                        int status = points.getInt("status");
                        int id = points.getInt("id");
                        String location = points.getString("location");
                        Date date = java.sql.Date.valueOf(points.getString("date"));
                        String image = points.getString("image");

                        ImageView img =  new ImageView(MapsActivity.this);
                        Picasso.with(MapsActivity.this).load(image).into(img);

                        gp = new GreenPoint();
                        gp.setLatitude(latitude);
                        gp.setLongitude(longitude);
                        gp.setId(id);
                        gp.setLocation(location);
                        gp.setDate(date);
                        gp.setImage(img);
                        gp.setStatus(status);

                        int drawable = R.drawable.p_amarillo;

                        if (status == 1 || status == 2){
                            //int canopy = points.getInt("canopy");
                            //int stem = points.getInt("stem");
                            int height = points.getInt("height");
                            String type = points.getString("type");
                            String user = points.getString("username");
                            gp.setUsername(user);
                            gp.setTreeType(type);
                            gp.setHeight(String.valueOf(height));
                        } else if (status == 0){
                            String type = points.getString("type");
                            String user = points.getString("username");
                            gp.setUsername(user);
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
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromBitmap(color_scaled))
                                .anchor(0.5f, 0.4f)
                        );
                        m.setTag(gp);
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "FailtoLoad", Toast.LENGTH_SHORT).show();
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

            if (m_status == UNREQUESTED || m_status == REQUESTED) {
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
                m_username = rp.getUsername();
                plant.setText("@"+m_username);
            }
        }

        private void renderGreenPoint(Marker marker, View view) {

            GreenPoint gp = (GreenPoint) marker.getTag();  //GREEN POINT DATA
            assert gp != null;
            m_status = gp.getStatus();
            m_date = gp.getDate();
            m_type = gp.getTreeType();
            m_username = gp.getUsername();
            m_location = gp.getLocation();
            m_image = gp.getImage();
            m_size = gp.getHeight();
            //m_profile = gp.getProfileImage();

            PorterShapeImageView tree = (PorterShapeImageView) view.findViewById(R.id.treePic);
            PorterShapeImageView profile = (PorterShapeImageView) view.findViewById(R.id.profile);
            TextView user = (TextView) view.findViewById(R.id.user);
            TextView date = (TextView) view.findViewById(R.id.p_date);
            TextView type = (TextView) view.findViewById(R.id.p_type);
            TextView size = (TextView) view.findViewById(R.id.p_size);
            TextView status = (TextView) view.findViewById(R.id.p_status);
            TextView location = (TextView) view.findViewById(R.id.location);

            Drawable image = m_image.getDrawable();
            tree.setImageDrawable(image);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy",Locale.US);
            String pointDate = dateFormat.format(m_date);
            date.setText(pointDate);

            type.setText(m_type);
            location.setText(m_location);
            user.setText("@"+m_username);

            if (m_size.equals("Altura Aproximada"))
                size.setText("Undefined");
            else size.setText(m_size+"m");


            if (m_status == UNVERIFIED) {
                status.setTextColor(getResources().getColor(R.color.yellow));
                status.setText(getString(R.string.not_verified));
            } else if (m_status == VERIFIED){
                status.setTextColor(getResources().getColor(R.color.colorCanopy));
                status.setText(getString(R.string.verified));
            }

        }
    }
    public class GetStats extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject response_body = new JSONObject();
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL("http://192.168.1.85:8000/stats/"+params[0]+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,true);

                if (response != null) {
                    Log.w("RESPONSE", String.valueOf(response.getBody()));
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        response_body = response.getBody();
                        response_body.put("status",0);
                    } else {
                        response_body.put("status",1);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_body;
        }

        // Process doInBackground() results
        @Override
        protected void onPostExecute(JSONObject response) {
            //Log.d("RESPONSE POST",String.valueOf(response.getInt("status")));

            try {
                if (response.getInt("status") == 0) {
                    float population = (float) response.getDouble("population_density");
                    float green_index = (float) response.getDouble("green_index");
                    int reported_trees = response.getInt("reported_trees");

                    NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US); //Italian for Latin
                    formatter.setMaximumFractionDigits(MAX_FRACTION_DIGITS);

                    String percent = getResources().getString(R.string.percent, formatter.format(green_index));
                    greenIndex.setText(percent + "%");

                    populationDensity = (TextView) findViewById(R.id.populationDensityUnits);
                    String density = getResources().getString(R.string.density, formatter.format(population));
                    populationDensity.setText(density);


                }else {
                    Toast.makeText(MapsActivity.this, "FailtoLoad", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}