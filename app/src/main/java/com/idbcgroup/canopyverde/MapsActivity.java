package com.idbcgroup.canopyverde;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
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
    private FloatingActionButton camera;
    private LatLng marker;
    private static final LatLng CARACAS = new LatLng(10.4806, -66.9036);

    /** Demonstrates customizing the info window and/or its contents. */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.red_points_dialog, null);
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
            int badge;

            badge = android.R.drawable.star_big_on;

            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);
            Button seed = ((Button) view.findViewById(R.id.request));
            seed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MapsActivity.this,"New Tree request",Toast.LENGTH_SHORT).show();
                }
            });
            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        camera = (FloatingActionButton) findViewById(R.id.camera);
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
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
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
        MapStyleOptions style;
        style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_treepedia);
        mMap.setMapStyle(style);
        mMap.setMinZoomPreference(12);
        mMap.setMaxZoomPreference(22);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
/*
        mMap.addMarker(new MarkerOptions()
                .position(caracas)
                .title("Marker in Caracas")
                .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on)));

        MarkerOptions m = new MarkerOptions();
        m.anchor(0.5f, 0.5f);
        m.title("Marker in Caracas");
        m.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on));
*/
        mMap.moveCamera(CameraUpdateFactory.newLatLng(CARACAS));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                //String latitude = new String(cameraPosition.target.latitude).toString();
                //ouble longitude = cameraPosition.target.longitude;

                marker = new LatLng(cameraPosition.target.latitude,cameraPosition.target.longitude);

                pref_marker = getSharedPreferences("Marker", 0);
                approved  = pref_marker.getBoolean("approved",false);

                if (approved) {
                    mMap.addMarker(new MarkerOptions()
                            .position(marker)
                            .snippet("new marker")
                            .title("TREE")
                            .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on)));

                    SharedPreferences.Editor editor1 = getSharedPreferences("Marker", 0).edit();
                    editor1.putBoolean("approved", false);
                    editor1.apply();
                }
                //Toast.makeText(MapsActivity.this,marker.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void currentLocation(View view){
        LatLng latLng = new LatLng(mMap.getMyLocation().getLatitude(),mMap.getMyLocation().getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mMap.animateCamera(cameraUpdate);
    }

    public void cameraView(View view){
        startActivity(new Intent(MapsActivity.this, CameraActivity.class));
    }

    public void profileView(View view){
        startActivity(new Intent(MapsActivity.this, UserProfileActivity.class));
    }
}
