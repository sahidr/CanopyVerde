package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * MapActivity
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener,
        GoogleMap.OnMarkerClickListener {

    private static final LatLng CARACAS = new LatLng(10.4806, -66.9036);
    private static final int MAX_FRACTION_DIGITS = 1;
    private static final int UNREQUESTED = -1;
    private static final int REQUESTED = 0;
    private static final int UNVERIFIED = 1;
    private static final int VERIFIED = 2;
    private static final int HEIGHT = 32;           // Height of the bitmap for the markers
    private static final int WIDTH = 32;            // Width of the bitmap for the markers
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_POINT_REGISTER = 1;

    private GoogleMap mMap;
    private Context context;
    private int m_status;
    private TextView greenIndex, populationDensity,city, reportedTrees;
    private RelativeLayout stats;
    private String lastCity;
    private boolean first_time = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setupMapIfNeeded();

        lastCity = "Caracas";
        context = this;
        stats = (RelativeLayout) findViewById(R.id.stats);
        greenIndex = (TextView) findViewById(R.id.greenViewIndexPercent);
        populationDensity = (TextView) findViewById(R.id.populationDensityUnits);
        reportedTrees = (TextView) findViewById(R.id.reportedTreesUnits);
        city = (TextView) findViewById(R.id.city);
        city.setText("-");
        greenIndex.setText("-- %");
        populationDensity.setText("-- /km^2");
        reportedTrees.setText("--");

        GetStats s = new GetStats();
        s.execute(lastCity);
    }

    /**
     * Obtain the SupportMapFragment and get notified when the map is ready to be used.
     */
    private void setupMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * in case of the application enters in the onPause, the Map state is save
     */
    @Override
    protected void onPause() {
        super.onPause();
        setupMapIfNeeded();
        if (mMap != null) {
            // save last state of the Map
            MapStateManager mgr = new MapStateManager(this);
            mgr.saveMapState(mMap);
        }
    }

    /**
     * In case of empty Map onResume, Map is configured again
     */
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
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.canopy_style_map);
        mMap.setMapStyle(style);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMinZoomPreference(11);
        mMap.setMaxZoomPreference(18);
        mMap.setInfoWindowAdapter(new PointWindow());

        // Load last state of the Map
        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
            mMap.setMapType(mgr.getSavedMapType());
        } else {
            // if Map is new load in Caracas
            mMap.moveCamera(CameraUpdateFactory.newLatLng(CARACAS));
        }

        GetTreePointMarkers g = new GetTreePointMarkers();
        g.execute();

        if (ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        /*
         * OnCameraChange build with AsyncTask for Geocoder
         * Get current city from camera position
         */
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                LatLng location = new LatLng(cameraPosition.target.latitude,
                        cameraPosition.target.longitude);
                new AsyncTask<LatLng, String, String>() {
                    @Override
                    protected String doInBackground(LatLng... params) {
                        Geocoder geocoder = new Geocoder(context);
                        List<Address> addresses;
                        String currentCity = lastCity;
                        try {
                            addresses = geocoder.getFromLocation(params[0].latitude,
                                    params[0].longitude, 1);
                            if (addresses != null)
                                currentCity = addresses.get(0).getLocality();

                            if (currentCity != null && lastCity!= null) {
                                if (!lastCity.equals(currentCity)) {
                                    return currentCity;
                                }
                            }
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                        return currentCity;
                    }

                    @Override
                    protected void onPostExecute(String city) {

                        if (city != null) {
                            if (!lastCity.equals(city)) {
                                lastCity = city;
                                GetStats stats = new GetStats();
                                stats.execute(city);
                            }
                        }
                    }
                }.execute(location);
            }
        });
    }

    /**
     * Fade Animation for the Stats Layout and info window reload when Marker is clicked
     * Implements the GoogleMap.OnMarkerClickListener Interface
     * Reloads the infoWindow for the images
     * @param marker the marker to be clicked
     * @return boolean
     * false for default marker behaviour wich is center camera at marker position and open info window
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.hideInfoWindow();
        marker.showInfoWindow();
        stats.animate().alpha(0.0f);
        return false;
    }

    /**
     * Method that implements the OnInfoWindowClickListener interface
     * Fades in the Stats and close the infoWindow
     * @param marker the marker of the window to be close
     */
    @Override
    public void onInfoWindowClose(Marker marker) {
        stats.animate().alpha(1f);
    }

    /**
     * Implements the interface GoogleMap.OnInfoWindowCloseListener
     * Allows the user to register a Red point if it's available
     * @param marker the red point to register if it's available
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        TreePoint red_point = (TreePoint) marker.getTag();
        assert red_point != null;
        m_status = red_point.getStatus();
        int rp_id = red_point.getId();
        Float lat = red_point.getLatitude();
        Float lng = red_point.getLongitude();
        String rp_location = red_point.getLocation();

        if (m_status == -1) {
            Intent i = new Intent(MapActivity.this, RedPointRegisterActivity.class);
            i.putExtra("id",rp_id);
            i.putExtra("latitude",lat);
            i.putExtra("longitude",lng);
            i.putExtra("location",rp_location);
            startActivityForResult(i,REQUEST_POINT_REGISTER);
        }
    }

    /**
     * Get current location for custom crosseye button
     * @param view Button that sets the camera of the Map and Locate the user in the current
     *             position of the GPS
     */
    public void currentLocation(View view) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {

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

            }
        }
    }

    /**
     * Method to call the Android Camera app for Green Point Register
     * @param view The Button that triggers the TreePointRegister starting with the
     *             cameraView
     */
    public void cameraView(View view) {
        CameraPosition cameraPosition = mMap.getCameraPosition();
        SharedPreferences.Editor markerEditor = getSharedPreferences("Marker", 0).edit();
        markerEditor.putLong("lat", Double.doubleToRawLongBits(cameraPosition.target.latitude));
        markerEditor.putLong("long", Double.doubleToRawLongBits(cameraPosition.target.longitude));
        markerEditor.apply();
        cameraIntent();
    }

    /**
     * Method that calls the Camera App of the device
     */
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * Result of the Camera app or Successful point Register or Update
     * @param requestCode identifies who's calling the Intent
     * @param resultCode identifies the result of the called Intent
     * @param data the data retrieved from the called Intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result from camera app
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                Intent i = new Intent(MapActivity.this, TreePointRegisterActivity.class);
                i.putExtras(data);
                startActivityForResult(i, REQUEST_POINT_REGISTER);
            }
        } else {
            // Result from Yellow or Red Point Register
            GetTreePointMarkers trees = new GetTreePointMarkers();
            trees.execute();
            finish();
            startActivity(getIntent());
        }
    }

    /**
     * Intent to User Profile
     * @param view Button in the view who calls the UserProfileActivity
     */
    public void profileView(View view) {
        startActivity(new Intent(MapActivity.this, UserProfileActivity.class));
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
     * AsyncTasks that gets the registered tree points in the server
     */
    private class GetTreePointMarkers extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject apiResponse = new JSONObject();
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL("https://canopy-verde.herokuapp.com/treepoint/");
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
                if (response.getInt("status") == 0) {
                    JSONArray pointsArray = response.getJSONArray("body");
                    JSONObject points;

                    TreePoint tree_point;
                    for (int i = 0; i < pointsArray.length(); i++) {
                        points = pointsArray.getJSONObject(i);
                        Float latitude = Float.parseFloat(points.getString("latitude"));
                        Float longitude = Float.parseFloat(points.getString("longitude"));
                        int status = points.getInt("status");
                        int id = points.getInt("id");
                        String location = points.getString("location");

                        tree_point = new TreePoint();
                        tree_point.setLatitude(latitude);
                        tree_point.setLongitude(longitude);
                        tree_point.setId(id);
                        tree_point.setLocation(location);
                        tree_point.setStatus(status);

                        int drawable = R.drawable.p_amarillo;

                        if (status == 1 || status == 2){
                            int height = points.getInt("height");
                            String type = points.getString("type");
                            String user = points.getString("username");
                            String image = points.getString("image");
                            String profile = points.getString("profile_pic");
                            Date date = java.sql.Date.valueOf(points.getString("date"));
                            tree_point.setUsername(user);
                            tree_point.setTreeType(type);
                            tree_point.setHeight(String.valueOf(height));
                            tree_point.setDate(date);
                            tree_point.setImage(image);
                            tree_point.setProfileImage(profile);
                        } else if (status == 0){
                            String type = points.getString("type");
                            String user = points.getString("username");
                            tree_point.setUsername(user);
                            tree_point.setTreeType(type);
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

                        // Scales the bitmap that will be used as the Marker icon
                        BitmapDrawable color = (BitmapDrawable) getResources().getDrawable(drawable);
                        Bitmap color_scaled = Bitmap
                                .createScaledBitmap(color.getBitmap(), WIDTH, HEIGHT, false);
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromBitmap(color_scaled))
                                .anchor(0.5f, 0.4f)
                        );
                        m.setTag(tree_point);
                    }
                } else {
                    Toast.makeText(MapActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * AsyncTask Class to get Stats of the current City
     */
    private class GetStats extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject response_body = new JSONObject();
            URL url;
            HttpURLConnection urlConnection;
            try {
                String city = URLEncoder.encode(params[0], "UTF-8");
                url = new URL("https://canopy-verde.herokuapp.com/city/?search="+city);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,false);

                if (response != null) {

                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        response_body.put("status",0);
                        response_body.put("body",response.getBodyArray());

                    } else {
                        response_body.put("status",1);
                        response_body.put("body",response.getBodyArray());
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

            try {
                city.setText(lastCity);
                greenIndex.setText("-- %");
                populationDensity.setText("-- /km^2");
                reportedTrees.setText("--");
                if (response.getInt("status") == 0) {

                    JSONArray cityStatsArray = response.getJSONArray("body");

                    if (cityStatsArray.length()>0) {

                        JSONObject cityStats = cityStatsArray.getJSONObject(0);

                        float population = (float) cityStats.getDouble("population_density");
                        float green_index = (float) cityStats.getDouble("green_index");
                        int reported_trees = cityStats.getInt("reported_trees");

                        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US); //Italian for Latin
                        formatter.setMaximumFractionDigits(MAX_FRACTION_DIGITS);

                        city.setText(lastCity);

                        String percent = getResources().getString(R.string.percent, formatter.format(green_index));
                        greenIndex.setText(percent + "%");

                        String density = getResources().getString(R.string.density, formatter.format(population));
                        populationDensity.setText(density);

                        reportedTrees.setText(String.valueOf(reported_trees));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Custom infoWindow that will be used for the markers
     * The developer can customize the infoWindow content and the Bubble layout of the marker
     * Implements the interface InfoWindowAdapter
     */
    private class PointWindow implements GoogleMap.InfoWindowAdapter{

        /**
         * Implements the getInfoWindow method of the interface
         * Sets the custom infoWindow for the marker provided
         * @param marker the host of the infoWindow who contains the window information to display
         * @return window the view that represents custom infoWindow
         * In case of customizing just the content in the window but no the default bubble layout
         * this method just have to return null and the getInfoContent method have to be override
         */
        @Override
        public View getInfoWindow(Marker marker) {
            View window;

            TreePoint point = (TreePoint) marker.getTag();
            assert point != null;
            String m_location = point.getLocation();
            m_status = point.getStatus();

            String m_username;
            if (m_status == UNREQUESTED || m_status == REQUESTED ) {
                window = getLayoutInflater().inflate(R.layout.red_info_window, null);

                TextView location = (TextView) window.findViewById(R.id.location);
                TextView available = (TextView) window.findViewById(R.id.available);
                TextView plant = (TextView) window.findViewById(R.id.plant);

                location.setText(m_location);

                if (point.getStatus() == UNREQUESTED ){
                    available.setText(R.string.available);
                    plant.setText(R.string.plant);

                } else {
                    available.setText(R.string.occupied);
                    m_username = point.getUsername();
                    plant.setText("@"+ m_username);
                }

            } else {

                Date m_date = point.getDate();
                String m_type = point.getTreeType();
                m_username = point.getUsername();
                String m_image = point.getImage();
                String m_size = point.getHeight();
                String m_profile = point.getProfileImage();

                window = getLayoutInflater().inflate(R.layout.green_info_window, null);
                PorterShapeImageView tree = (PorterShapeImageView) window.findViewById(R.id.treePic);
                PorterShapeImageView profile = (PorterShapeImageView) window.findViewById(R.id.profile);
                TextView user = (TextView) window.findViewById(R.id.user);
                TextView date = (TextView) window.findViewById(R.id.p_date);
                TextView type = (TextView) window.findViewById(R.id.p_type);
                TextView size = (TextView) window.findViewById(R.id.p_size);
                TextView status = (TextView) window.findViewById(R.id.p_status);
                TextView location = (TextView) window.findViewById(R.id.location);

                if (first_time) {
                    first_time = false;
                    Picasso.with(context).load(m_image).into(tree,new MarkerCallback(marker));
                    Picasso.with(context).load(m_profile).into(profile,new MarkerCallback(marker));
                } else {
                    Picasso.with(MapActivity.this).load(m_image).into(tree);
                    Picasso.with(context).load(m_profile).into(profile);
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy",Locale.US);
                String pointDate = dateFormat.format(m_date);
                date.setText(pointDate);

                type.setText(m_type);
                location.setText(m_location);
                user.setText("@"+ m_username);

                if (m_size.equals(getString(R.string.height_aprox)))
                    size.setText("Undefined");
                else size.setText(m_size +"m");

                if (m_status == UNVERIFIED) {
                    int yellow = ResourcesCompat.getColor(getResources(), R.color.yellow, null);
                    //status.setTextColor(getResources().getColor(R.color.yellow));
                    status.setTextColor(yellow);
                    status.setText(getString(R.string.not_verified));
                } else if (m_status == VERIFIED){
                    int green = ResourcesCompat.getColor(getResources(), R.color.colorCanopy, null);
                    status.setTextColor(green);
                    //status.setTextColor(getResources().getColor(R.color.colorCanopy));
                    status.setText(getString(R.string.verified));
                }
            }
            return window;
        }

        /**
         * Implements the getInfoContent method of the interface
         * Sets the custom Content for the marker provided
         * @param marker the host of the infoWindow who contains the window information to display
         * @return window the view that represents custom infoWindow
         * In case of customizing the content and the default bubble layout
         * this method just have to return null and the getInfowWindow method have to be override
         */
        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    /**
     * Callback Interface of the Picasso Library that manage the result after executing the load
     * method
     */
    private static class MarkerCallback implements Callback {
        private final Marker markerToRefresh;

        private MarkerCallback(Marker markerToRefresh) {
            this.markerToRefresh = markerToRefresh;
        }

        /**
         * If the request ends in success reloads the infoWindow to display the lasted content
         */
        @Override
        public void onSuccess() {
            markerToRefresh.hideInfoWindow();
            markerToRefresh.showInfoWindow();
        }

        /**
         * Overrides the method onError in case the request fails
         */
        @Override
        public void onError() {}
    }
}