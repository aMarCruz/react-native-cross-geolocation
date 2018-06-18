package com.github.amarcruz.geolocation;

import android.location.Location;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;

class LocationResolver {
    static private final String TAG = Constants.TAG;

    private Callback mSuccessCallback;
    private Callback mErrorCallback;
    private boolean done = false;

    LocationResolver(final Callback success, final Callback error) {
        mSuccessCallback = success;
        mErrorCallback = error;
    }

    void success(final Location place) {
        if (mSuccessCallback != null && !done) {
            done = true;
            try {
                if (place != null) {
                    mSuccessCallback.invoke(locationToMap(place));
                } else {
                    mErrorCallback.invoke(
                        PositionError.buildError(PositionError.POSITION_UNAVAILABLE, "Canceled."));
                }
            } catch (Exception ex) {
                mErrorCallback.invoke(
                    PositionError.buildError(PositionError.POSITION_UNAVAILABLE, ex.getMessage()));
            } finally {
                mSuccessCallback = null;
                mErrorCallback = null;
            }
        }
    }

    void error(final int code, final String message) {
        if (mErrorCallback != null && !done) {
            done = true;
            Log.e(TAG, "Location error: " + message);
            mErrorCallback.invoke(PositionError.buildError(code, message));
            mErrorCallback = null;
        }
    }

    void error(final String message) {
        error(PositionError.POSITION_UNAVAILABLE, message);
    }

    public static WritableMap locationToMap(Location location) {
        WritableMap map = Arguments.createMap();
        WritableMap coords = Arguments.createMap();

        coords.putDouble("latitude", location.getLatitude());
        coords.putDouble("longitude", location.getLongitude());
        coords.putDouble("altitude", location.getAltitude());
        coords.putDouble("accuracy", location.getAccuracy());
        coords.putDouble("heading", location.getBearing());
        coords.putDouble("speed", location.getSpeed());
        map.putMap("coords", coords);
        map.putDouble("timestamp", location.getTime());
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            map.putBoolean("mocked", location.isFromMockProvider());
        }

        return map;
    }

}
