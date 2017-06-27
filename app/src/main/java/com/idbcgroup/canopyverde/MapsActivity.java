package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.media.Image;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GraphView graph;
    private SharedPreferences pref_marker;
    private Boolean approved;
    private MapStyleOptions style;
    private LatLng marker;
    private Context context;
    private Bitmap m_tree, m_user;
    private String m_date,m_type,m_size, m_status, m_username, m_location;

    private static final LatLng CARACAS = new LatLng(10.4806, -66.9036);

    LatLng l1 = new LatLng(10.492037, -66.827096);
    LatLng l2 = new LatLng(10.492330, -66.827681);
    LatLng l3 = new LatLng(10.488365, -66.825681);
    LatLng l4 = new LatLng(10.488530, -66.825873);
    LatLng l5 = new LatLng(10.490110, -66.827370);
    LatLng l6 = new LatLng(10.491841, -66.826925);
    LatLng l7 = new LatLng(10.484345, -66.826400);
    LatLng[] l = {l1,l2,l3,l4,l5,l6,l7};


    /** Demonstrates customizing the info window and/or its contents. */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
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

            m_date = "22/12/17";
            m_type = "Araguaney";
            m_size = "Mediano";
            m_status = "Verificado";


            // ImageView tree = (ImageView) view.findViewById(R.id.treePic);
            // ImageView profile = (ImageView) view.findViewById(R.id.profile);
            // TextView username = (TextView) view.findViewById(R.id.user);
            TextView date = (TextView) view.findViewById(R.id.p_date);
            TextView type = (TextView) view.findViewById(R.id.p_type);
            TextView size = (TextView) view.findViewById(R.id.p_size);
            TextView status = (TextView) view.findViewById(R.id.p_status);
            // TextView location = (TextView) view.findViewById(R.id.location);

            date.setText(m_date);
            type.setText(m_type);
            size.setText(m_size);

            if (m_status.equals(getString(R.string.verified))){
                status.setTextColor(getResources().getColor(R.color.colorCanopy));
            } else {
                status.setTextColor(getResources().getColor(R.color.yellow));
            }
            status.setText(m_status);

            /*
            //int badge;

            //badge = android.R.drawable.star_big_on;

            ((ImageView) view.findViewById(R.id.badge)).setImageResource(R.drawable.araguaney);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                //SpannableString titleText = new SpannableString(title);
                //titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                //titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                //SpannableString snippetText = new SpannableString(snippet);
                //snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                //snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                //snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
            */
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        context = this;

        // Obtain the SupportMapFragment and get notified when the map_circle is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //camera = (ImageButton) findViewById(R.id.locator);
        graph = (GraphView) findViewById(R.id.chart);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
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
        int point_height = 50;
        int point_width = 50;

        mMap = googleMap;
        style = MapStyleOptions.loadRawResourceStyle(this, R.raw.canopy_style_map);
        mMap.setMapStyle(style);

        BitmapDrawable green_point=(BitmapDrawable)getResources().getDrawable(R.drawable.p_verde);
        Bitmap green_point_scaled = Bitmap.createScaledBitmap(green_point.getBitmap(), point_width, point_height, false);

        for (int i = 0; i < l.length; i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(l[i])
                    .title("@username")
                    .snippet(getString(R.string.snippet))
                    .icon(BitmapDescriptorFactory.fromBitmap(green_point_scaled))
                     );
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(CARACAS)); //CARACAS

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                marker = new LatLng(cameraPosition.target.latitude,cameraPosition.target.longitude);

                pref_marker = getSharedPreferences("Marker", 0);
                approved  = pref_marker.getBoolean("approved",false);

                if (approved) {
                mMap.addMarker(new MarkerOptions()
                        .position(marker)
                        .snippet(getString(R.string.not_verified))
                        .title("TREE")
                        .flat(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.p_amarillo)));

                SharedPreferences.Editor editor1 = getSharedPreferences("Marker", 0).edit();
                editor1.putBoolean("approved", false);
                editor1.apply();
                }
            }
        });
    }

    public void currentLocation(View view){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setMessage(context.getResources().getString(R.string.enable_location));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });
            dialog.show();
        } else {
            try {
                LatLng latLng = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                mMap.animateCamera(cameraUpdate);
            } catch (Exception e){}
        }
    }

    public void cameraView(View view){
        startActivity(new Intent(MapsActivity.this, CameraActivity.class));
    }

    public void profileView (View view){
        startActivity(new Intent(MapsActivity.this, UserProfileActivity.class));
    }
}