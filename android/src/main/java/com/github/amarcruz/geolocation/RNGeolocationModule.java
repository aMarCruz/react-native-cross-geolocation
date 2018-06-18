package com.github.amarcruz.geolocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.SystemClock;

import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class RNGeolocationModule extends ReactContextBaseJavaModule {
    private static final String TAG = Constants.TAG;

    private FusedLocationProviderClient mFusedProviderClient;
    private boolean mRequestingLocationUpdates = false;

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            synchronized (RNGeolocationModule.this) {
                if (locationResult != null) {
                    emitSuccess(locationResult.getLastLocation());
                }
            }
        }
    };

    RNGeolocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(reactContext);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public Map<String, Object> getConstants () {
        final Map<String, Object> constants = new HashMap<>();

        constants.put("HIGH_ACCURACY", LocationRequest.PRIORITY_HIGH_ACCURACY);
        constants.put("BALANCED", LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        constants.put("LOW_POWER", LocationRequest.PRIORITY_LOW_POWER);
        constants.put("NO_POWER", LocationRequest.PRIORITY_NO_POWER);

        return constants;
    }

    @ReactMethod
    public void setConfiguration (final ReadableMap conf) {
        LocationOptions.setConfiguration(conf);
    }

    @ReactMethod
    public void getCurrentPosition(ReadableMap options, final Callback success, final Callback error) {
        final ReactApplicationContext context = getReactApplicationContext();
        final Activity activity = context.getCurrentActivity();
        final LocationOptions opts = LocationOptions.fromReactMap(options);
        final LocationResolver resolver = new LocationResolver(success, error);

        if (activity == null) {
            resolver.error("Cannot get activity.");
            return;
        }

        if (!isPlayServicesAvailable(context)) {
            resolver.error("Google Play Service not available.");
            return;
        }

        if (!hasPermissions(context)) {
            resolver.error(PositionError.PERMISSION_DENIED, "Location permission not granted.");
            return;
        }

        final SettingsClient settingsClient = LocationServices.getSettingsClient(context);

        final LocationRequest locationRequest = new LocationRequest()
                .setInterval(LocationOptions.getUpdateInterval())
                .setFastestInterval(LocationOptions.getFastestInterval())
                .setPriority(opts.priority)
                .setExpirationDuration(opts.timeout);

        final LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(@NonNull LocationSettingsResponse task) {
                        // All location settings are satisfied.
                        getUserLocation(locationRequest, opts, resolver);
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception ex) {
                        final int code = ((ApiException) ex).getStatusCode();
                        resolver.error("Error " + code + ": " + ex.getMessage());
                    }
                });
    }

    @ReactMethod
    public void stopObserving () {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            try {
                mFusedProviderClient.removeLocationUpdates(mLocationCallback);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @ReactMethod
    public void startObserving (final ReadableMap opts) {
        if (mRequestingLocationUpdates) {
            return;
        }
        mRequestingLocationUpdates = true;
        mRequestingLocationUpdates = startUpdater(LocationOptions.fromReactMap(opts));
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation(
            final LocationRequest locationRequest,
            final LocationOptions options,
            final LocationResolver resolver
    ) {
        if (mFusedProviderClient != null) {
            mFusedProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();

                    if (location != null &&
                            (SystemClock.currentTimeMillis() - location.getTime()) < options.maximumAge) {
                        resolver.success(location);
                    } else {
                        // Last location not available, request new location.
                        new SingleLocationRequest(
                                mFusedProviderClient,
                                locationRequest,
                                resolver).getLocation();
                    }
                }
            });
        }
    }

    private boolean startUpdater (final LocationOptions options) {
        final ReactApplicationContext context = getReactApplicationContext();
        final Activity activity = context.getCurrentActivity();

        if (activity == null) {
            emitError("Cannot get activity.");
            return false;
        }

        if (!isPlayServicesAvailable(context)) {
            emitError("Google Play Service not available.");
            return false;
        }

        if (!hasPermissions(context)) {
            emitError(PositionError.PERMISSION_DENIED, "Location permission not granted.");
            return false;
        }

        final SettingsClient settingsClient = LocationServices.getSettingsClient(context);

        final LocationRequest locationRequest = new LocationRequest()
                .setInterval(LocationOptions.getUpdateInterval())
                .setFastestInterval(LocationOptions.getFastestInterval())
                .setPriority(options.priority)
                .setExpirationDuration(options.timeout)
                .setSmallestDisplacement(options.distanceFilter);

        final LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(@NonNull LocationSettingsResponse task) {
                        // All location settings are satisfied.
                        mFusedProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, null)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        emitError(e.getMessage());
                                    }
                                });

                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception ex) {
                        final int code = ((ApiException) ex).getStatusCode();
                        emitError("Error " + code + ": " + ex.getMessage());
                    }
                });

        return true;
    }

    private void emitSuccess(final Location location) {
        final ReactApplicationContext reactContext = getReactApplicationContext();

        if (reactContext.hasActiveCatalystInstance()) {
            final WritableMap result = LocationResolver.locationToMap(location);
            reactContext
                    .getJSModule(RCTDeviceEventEmitter.class)
                    .emit("geolocationDidChange", result);
        } else {
            Log.i(TAG, "Waiting for Catalyst Instance...");
        }
    }

    private void emitError(int code, String message) {
        final ReactApplicationContext reactContext = getReactApplicationContext();

        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                    .getJSModule(RCTDeviceEventEmitter.class)
                    .emit("geolocationError", PositionError.buildError(code, message));
        } else {
            Log.i(TAG, "Waiting for Catalyst Instance...");
        }
    }

    private void emitError(final String message) {
        emitError(PositionError.POSITION_UNAVAILABLE, message);
    }

    private static boolean hasPermissions(final ReactApplicationContext context) {
        return (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
               (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isPlayServicesAvailable(final ReactApplicationContext context) {
        final GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);

        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        }

        if (googleApiAvailability.isUserResolvableError(resultCode)) {
            googleApiAvailability.getErrorDialog(
                    getCurrentActivity(), resultCode, Constants.PLAY_SERVICES_REQUEST).show();
        }
        return false;
    }
}
