package com.idbcgroup.canopyverde;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SharedPreferences pref_marker;
    private Boolean approved;
    private MapStyleOptions style;
    private LatLng marker;
    private Context context;
    private Bitmap m_tree, m_user;
    private String m_date,m_type,m_size, m_status, m_username, m_location;
    private int HEIGHT = 30;
    private int WIDTH = 30;
    private TextView greenIndex;
    private TextView populationDensity;
    private RelativeLayout stats;
    private SharedPreferences pref_session;

    private static final LatLng CARACAS = new LatLng(10.4806, -66.9036);

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
            m_status = marker.getSnippet(); //"Verificado";

            // ImageView tree = (ImageView) view.findViewById(R.id.treePic);
            ImageView profile = (ImageView) view.findViewById(R.id.profile);
            TextView user = (TextView) view.findViewById(R.id.user);
            TextView date = (TextView) view.findViewById(R.id.p_date);
            TextView type = (TextView) view.findViewById(R.id.p_type);
            TextView size = (TextView) view.findViewById(R.id.p_size);
            TextView status = (TextView) view.findViewById(R.id.p_status);
            // TextView location = (TextView) view.findViewById(R.id.location);


            pref_session = getSharedPreferences("Session", 0);
            String profilepic = pref_session.getString("photo",null);
            String username = pref_session.getString("username",null);

            date.setText(m_date);
            type.setText(m_type);
            size.setText(m_size);

            if (m_status.equals(getString(R.string.verified))){
                status.setTextColor(getResources().getColor(R.color.colorCanopy));
                user.setText("@idbcuser");
                profile.setImageResource(R.drawable.btn_locate);
            } else {
                status.setTextColor(getResources().getColor(R.color.yellow));
                user.setText(username);
                if (profilepic!=null) {
                    Uri photo = Uri.parse(profilepic);
                    Picasso.with(MapsActivity.this).load(photo).into(profile);
                }
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

        BitmapDrawable green_point=(BitmapDrawable)getResources().getDrawable(R.drawable.p_verde);
        Bitmap green_point_scaled = Bitmap.createScaledBitmap(green_point.getBitmap(),WIDTH, HEIGHT, false);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
        Canvas canvas1 = new Canvas(bmp);

// paint defines the text color, stroke width and size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);

// modify canvas
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.map_circle), 0,0, color);


        for (int i = 0; i < l.length; i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(l[i])
                    .title("@username")
                    .snippet(getString(R.string.verified))
                    .icon(BitmapDescriptorFactory.fromBitmap(green_point_scaled))
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.locator))
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.p_verde1))
                    .anchor(0.5f, 0.4f)
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

        MarkerOptions m = new MarkerOptions();
        m.anchor(0.5f, 0.5f);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                SharedPreferences.Editor editor1 = getSharedPreferences("Marker", 0).edit();
                BitmapDrawable yellow_point=(BitmapDrawable)getResources().getDrawable(R.drawable.p_amarillo);
                Bitmap yellow_point_scaled = Bitmap.createScaledBitmap(yellow_point.getBitmap(),WIDTH, HEIGHT, false);
                //editor1.putBoolean("approved", false);
                editor1.putLong( "lat", Double.doubleToRawLongBits( cameraPosition.target.latitude));
                editor1.putLong( "long", Double.doubleToRawLongBits(cameraPosition.target.longitude ));
                editor1.apply();

                //marker = new LatLng(cameraPosition.target.latitude,cameraPosition.target.longitude);

                pref_marker = getSharedPreferences("Marker", 0);

                approved  = pref_marker.getBoolean("approved",false);

                if (approved) {
                    double lat = Double.longBitsToDouble( pref_marker.getLong( "lat", -1 ));
                    double lon = Double.longBitsToDouble( pref_marker.getLong( "long", -1 ));
                    LatLng latLng = new LatLng( lat,lon);

                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .snippet(getString(R.string.not_verified))
                            .title("TREE")
                            .icon(BitmapDescriptorFactory.fromBitmap(yellow_point_scaled))
                            .anchor(0.5f, 0.4f)
                    );

                    editor1.putBoolean("approved", false);
                    editor1.apply();
                }

            }
        });

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
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setMessage(context.getResources().getString(R.string.enable_location));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings),
                    new DialogInterface.OnClickListener() {
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
            } catch (Exception e){ Log.d("GPS","Location Error");}
        }
    }

    public void cameraView(View view){
        //startActivity(new Intent (MapsActivity.this, CameraActivity.class));
       cameraIntent();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Intent i = new Intent(MapsActivity.this,GreenPointRegisterActivity.class);
        i.putExtras(data);
        startActivity(i);
    }

    public void profileView (View view){
        startActivity(new Intent(MapsActivity.this, UserProfileActivity.class));
    }
}