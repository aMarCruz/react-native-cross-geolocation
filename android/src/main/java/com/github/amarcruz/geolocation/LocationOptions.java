package com.github.amarcruz.geolocation;

import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.location.LocationRequest;

final class LocationOptions {
    private static final float DEFAULT_DISTANCE_FILTER = 100f;

    private static long fastestInterval = 5000;     // 5 secs
    private static long updateInterval = 10000;     // 10 secs
    private static int lowPriorityMode = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    final long timeout;
    final double maximumAge;
    final float distanceFilter;
    final int priority;

    private LocationOptions(
            final long timeout,
            final double maximumAge,
            final boolean highAccuracy,
            final float distanceFilter
    ) {
        this.timeout = timeout;
        this.maximumAge = maximumAge;
        this.distanceFilter = distanceFilter;
        this.priority = highAccuracy ? LocationRequest.PRIORITY_HIGH_ACCURACY : lowPriorityMode;
    }

    static LocationOptions fromReactMap(final ReadableMap map) {
        long timeout = Long.MAX_VALUE;
        double maximumAge = Double.POSITIVE_INFINITY;
        boolean highAccuracy = false;
        float distanceFilter = DEFAULT_DISTANCE_FILTER;

        if (map != null) {
            // precision might be dropped on timeout (double -> int conversion), but that's OK
            if (map.hasKey("timeout")) {
                timeout = (long) map.getDouble("timeout");
            }
            if (map.hasKey("maximumAge")) {
                maximumAge = map.getDouble("maximumAge");
            }
            if (map.hasKey("enableHighAccuracy")) {
                highAccuracy = map.getBoolean("enableHighAccuracy");
            }
            if (map.hasKey("distanceFilter")) {
                distanceFilter = (float) map.getDouble("distanceFilter");
            }
        }

        return new LocationOptions(timeout, maximumAge, highAccuracy, distanceFilter);
    }

    static void setConfiguration (final ReadableMap map) {
        if (map != null) {
            if (map.hasKey("fastestInterval")) {
                fastestInterval = (long) map.getDouble("fastestInterval");
            }
            if (map.hasKey("updateInterval")) {
                updateInterval = (long) map.getDouble("updateInterval");
            }
            if (map.hasKey("lowPriorityMode")) {
                int mode = map.getInt("lowPriorityMode");
                if (mode != LocationRequest.PRIORITY_LOW_POWER && mode != LocationRequest.PRIORITY_NO_POWER) {
                    mode = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                }
                lowPriorityMode = mode;
            }
        }
    }

    static long getFastestInterval () {
        return fastestInterval;
    }

    static long getUpdateInterval () {
        return updateInterval;
    }
}
