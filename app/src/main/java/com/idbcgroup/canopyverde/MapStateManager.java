package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

class MapStateManager {
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "MAPTYPE";
    private static final String PREFS_NAME ="mapCameraState";

    private final SharedPreferences mapStatePrefs;

    MapStateManager(Context context) {
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    void saveMapState(GoogleMap mapMie) {
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        CameraPosition position = mapMie.getCameraPosition();

        editor.putFloat(LATITUDE, (float) position.target.latitude);
        editor.putFloat(LONGITUDE, (float) position.target.longitude);
        editor.putFloat(ZOOM, position.zoom);
        editor.putFloat(TILT, position.tilt);
        editor.putFloat(BEARING, position.bearing);
        editor.putInt(MAPTYPE, mapMie.getMapType());
        editor.apply();
    }

    CameraPosition getSavedCameraPosition() {

        double latitude = mapStatePrefs.getFloat(LATITUDE, (float) 10.4806); // Caracas Latitude
        double longitude = mapStatePrefs.getFloat(LONGITUDE, (float) -66.9036); // Caracas Longitude
        LatLng target = new LatLng(latitude, longitude);
        float zoom = mapStatePrefs.getFloat(ZOOM, 0);
        float bearing = mapStatePrefs.getFloat(BEARING, 0);
        float tilt = mapStatePrefs.getFloat(TILT, 0);
        return new CameraPosition(target, zoom, tilt, bearing);
    }

    int getSavedMapType() {
        return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
    }

    public void deletePreferences () {
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        editor.remove(LONGITUDE);
        editor.remove(LATITUDE);
        editor.remove(ZOOM);
        editor.remove(TILT);
        editor.remove(BEARING);
        editor.remove(MAPTYPE);
        editor.remove(PREFS_NAME);
        editor.apply();
    }
}