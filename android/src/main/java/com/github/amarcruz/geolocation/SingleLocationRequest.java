package com.github.amarcruz.geolocation;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnFailureListener;

class SingleLocationRequest {
    private final FusedLocationProviderClient mFusedProviderClient;
    private final LocationRequest mLocationRequest;
    private final LocationResolver mResolver;

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            synchronized (SingleLocationRequest.this) {
                if (locationResult != null) {
                    removeCallback();
                    mResolver.success(locationResult.getLastLocation());
                }
            }
        }
    };

    private void removeCallback () {
        final LocationCallback callback = mLocationCallback;
        mLocationCallback = null;
        if (callback != null) {
            try {
                mFusedProviderClient.removeLocationUpdates(callback);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    SingleLocationRequest(
            final FusedLocationProviderClient fusedProviderClient,
            final LocationRequest locationRequest,
            final LocationResolver resolver) {
        mFusedProviderClient = fusedProviderClient;
        mLocationRequest = locationRequest;
        mResolver = resolver;
    }

    /**
     * Request one time location update
     */
    @SuppressLint("MissingPermission")
    void getLocation () {
        if (mFusedProviderClient == null) {
            mResolver.error(PositionError.POSITION_UNAVAILABLE, "No location provider available.");
        } else {
            mFusedProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception ex) {
                            removeCallback();
                            try {
                                final int code = ((ApiException) ex).getStatusCode();
                                mResolver.error("Error " + code + ": " + ex.getMessage());
                            } catch (Exception ignore) {
                                mResolver.error(ex.getMessage());
                            }
                        }
                    });
        }
    }
}
