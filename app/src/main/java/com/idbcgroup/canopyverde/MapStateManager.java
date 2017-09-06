package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * This Class Load or Save the State of the Map after the onStart, onResume, onPause of the
 * app's lifecycle
 *
 * Based of the post
 * @link https://stackoverflow.com/questions/34636722/android-saving-map-state-in-google-map
 */
class MapStateManager {
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "MAPTYPE";
    private static final String PREFS_NAME ="mapCameraState";

    private final SharedPreferences mapStatePrefs;

    /**
     * Gets the preferences saved for a map
     * @param context the Activity context
     */
    MapStateManager(Context context) {
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Sve the last state of the map into a Shared Preferences
     * @param lastMap the last map state
     */
    void saveMapState(GoogleMap lastMap) {
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        CameraPosition position = lastMap.getCameraPosition();
        editor.putFloat(LATITUDE, (float) position.target.latitude);
        editor.putFloat(LONGITUDE, (float) position.target.longitude);
        editor.putFloat(ZOOM, position.zoom);
        editor.putFloat(TILT, position.tilt);
        editor.putFloat(BEARING, position.bearing);
        editor.putInt(MAPTYPE, lastMap.getMapType());
        editor.apply();
    }

    /**
     * Gets the last camera position of the map
     * @return the camera position
     */
    CameraPosition getSavedCameraPosition() {

        double latitude = mapStatePrefs.getFloat(LATITUDE, (float) 10.4806); // Caracas Latitude
        double longitude = mapStatePrefs.getFloat(LONGITUDE, (float) -66.9036); // Caracas Longitude
        LatLng target = new LatLng(latitude, longitude);
        float zoom = mapStatePrefs.getFloat(ZOOM, 0);
        float bearing = mapStatePrefs.getFloat(BEARING, 0);
        float tilt = mapStatePrefs.getFloat(TILT, 0);
        return new CameraPosition(target, zoom, tilt, bearing);
    }

    /**
     * Obtains the last map type in the Shared Preferences
     * @return the type of the map
     */
    int getSavedMapType() {
        return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
    }

    /**
     * Removes all the content of the State of the Map
     */
    void deletePreferences() {
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