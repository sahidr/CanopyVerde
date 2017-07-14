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
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SharedPreferences pref_marker;
    private MapStyleOptions style;
    private Context context;
    private Bitmap m_tree, m_user;
    private String m_date,m_type,m_size, m_username, m_location, m_image;
    private int m_status;
    private TextView greenIndex, populationDensity;
    private RelativeLayout stats;
    private SharedPreferences pref_session;
    private ArrayList<LatLng> markers;
    private int counter = 0;

    private static final LatLng CARACAS = new LatLng(10.4806, -66.9036);
    private static int HEIGHT = 32;
    private static int WIDTH = 32;
    private static int REQUEST_CAMERA = 0;
    private static int REQUEST_GREEN_POINT_REGISTER = 1;

    LatLng l1 = new LatLng(10.492037, -66.827096);
    LatLng l2 = new LatLng(10.492330, -66.827681);
    LatLng l3 = new LatLng(10.488365, -66.825681);
    LatLng l4 = new LatLng(10.488530, -66.825873);
    LatLng l5 = new LatLng(10.490110, -66.827370);
    LatLng l6 = new LatLng(10.491841, -66.826925);
    LatLng l7 = new LatLng(10.484345, -66.826400);
    LatLng l8 = new LatLng(10.475043, -66.954308);
    LatLng[] l = {l1,l2,l3,l4,l5,l6,l7,l8};

    /** Demonstrates customizing the info window and/or its contents. */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, View view) {

            GreenPoint gp = (GreenPoint) marker.getTag();
            assert gp != null;
            m_status = gp.getStatus();
            m_date = gp.getDate();
            m_type = gp.getTreeType();
            m_username = gp.getUser();
            m_location = gp.getLocation();
            m_image = gp.getImage();

            //ImageView tree = (ImageView) view.findViewById(R.id.treePic);
            CircleImageView profile = (CircleImageView) view.findViewById(R.id.profile);
            TextView user = (TextView) view.findViewById(R.id.user);
            TextView date = (TextView) view.findViewById(R.id.p_date);
            TextView type = (TextView) view.findViewById(R.id.p_type);
            TextView size = (TextView) view.findViewById(R.id.p_size);
            TextView status = (TextView) view.findViewById(R.id.p_status);
            TextView location = (TextView) view.findViewById(R.id.location);

            user.setText(m_username);
            date.setText(m_date);
            type.setText(m_type);
            size.setText(m_size);
            location.setText(m_location);

            if (m_image.equals("image")){
                profile.setImageResource(R.drawable.btn_locate);
            } else {
                Uri photo = Uri.parse(m_image);
                Picasso.with(MapsActivity.this).load(photo).into(profile);
            }

            if (m_status == 0){
                status.setTextColor(getResources().getColor(R.color.pink));
                status.setText("RED");
            } else if (m_status == 1) {
                status.setTextColor(getResources().getColor(R.color.yellow));
                status.setText(getString(R.string.not_verified));
            } else if (m_status == 2){
                status.setTextColor(getResources().getColor(R.color.colorCanopy));
                status.setText(getString(R.string.verified));
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        context = this;

        markers = new ArrayList<>();
        Collections.addAll(markers, l);

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US); //Italian
        formatter.setMaximumFractionDigits(1);

        stats = (RelativeLayout) findViewById(R.id.stats);

        float green_index = (float) 100.001;
        float pop_density = (float) 100000;
        greenIndex = (TextView) findViewById(R.id.greenViewIndexPercent);
        String percent = getResources().getString(R.string.percent, formatter.format(green_index));
        greenIndex.setText(percent+"%");

        populationDensity = (TextView) findViewById(R.id.populationDensityUnits);
        String density = getResources().getString(R.string.density, formatter.format(pop_density));
        populationDensity.setText(density);

        setupMapIfNeeded();

    }

    private void setupMapIfNeeded() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map_circle once available.
     * This callback is triggered when the map_circle is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
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
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);

        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
            mMap.setMapType(mgr.getSavedMapType());
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(CARACAS)); //CARACAS
        }


        BitmapDrawable green_point=(BitmapDrawable)getResources().getDrawable(R.drawable.p_verde);
        Bitmap green_point_scaled = Bitmap
                .createScaledBitmap(green_point.getBitmap(),WIDTH, HEIGHT, false);

        for (int i = 0; i < markers.size(); i++) {
            GreenPoint g = new GreenPoint(i,"Av. Los Cortijos","14/07/1993",10,11,12,"Roble","imagen",2);
            g.setUser("@idbcgroup");
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(markers.get(i))
                    .icon(BitmapDescriptorFactory.fromBitmap(green_point_scaled))
                    .anchor(0.5f, 0.4f)
            );
            m.setTag(g);
        }

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

        MarkerOptions m = new MarkerOptions();
        m.anchor(0.5f, 0.5f);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        stats.animate().alpha(0.0f);
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        stats.animate().alpha(1f);
    }

    public void currentLocation(View view){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) { Log.d("NETWORK","Connection Error"); }

        if(!gps_enabled && !network_enabled) {
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
            } catch (Exception e){ Log.d("GPS","Location Error");}
        }
    }

    public void cameraView(View view){
        CameraPosition cameraPosition = mMap.getCameraPosition();
        SharedPreferences.Editor markerEditor = getSharedPreferences("Marker", 0).edit();
        markerEditor.putLong("lat", Double.doubleToRawLongBits(cameraPosition.target.latitude));
        markerEditor.putLong("long",Double.doubleToRawLongBits(cameraPosition.target.longitude));
        markerEditor.apply();
        cameraIntent();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA){
            if (resultCode == Activity.RESULT_OK) {
                Intent i = new Intent(MapsActivity.this,GreenPointRegisterActivity.class);
                i.putExtras(data);
                startActivityForResult(i,REQUEST_GREEN_POINT_REGISTER);
            }
        } else if (requestCode == REQUEST_GREEN_POINT_REGISTER){
            pref_marker = getSharedPreferences("Marker", 0);
            BitmapDrawable yellow_point=(BitmapDrawable)getResources()
                    .getDrawable(R.drawable.p_amarillo);
            Bitmap yellow_point_scaled = Bitmap
                    .createScaledBitmap(yellow_point.getBitmap(),WIDTH, HEIGHT, false);
            if (resultCode == Activity.RESULT_OK) {
                double lat = Double.longBitsToDouble(pref_marker.getLong("lat", -1));
                double lon = Double.longBitsToDouble(pref_marker.getLong("long", -1));
                LatLng latLng = new LatLng(lat, lon);
                markers.add(latLng);

                String location = (String) data.getExtras().get("location");

                pref_session = getSharedPreferences("Session", 0);
                String profilepic = pref_session.getString("photo",null);
                String username = pref_session.getString("username",null);

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy",Locale.US);
                String formattedDate = df.format(calendar.getTime());

                GreenPoint g1 = new GreenPoint(counter,location,formattedDate,10,11,12,"Roble",profilepic,1);
                g1.setUser(username);
                Marker m1 = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(yellow_point_scaled))
                        .anchor(0.5f, 0.4f)
                );
                m1.setTag(g1);
                counter++;
            }
        }
    }

    public void profileView (View view){
        startActivity(new Intent(MapsActivity.this, UserProfileActivity.class));
    }
}