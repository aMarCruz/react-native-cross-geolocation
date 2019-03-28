package com.github.amarcruz.geolocation;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

/**
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/PositionError">PositionError</a>
 */
final class PositionError {
    /**
     * The acquisition of the geolocation information failed because
     * the page didn't have the permission to do it.
     */
    static int PERMISSION_DENIED = 1;

    /**
     * The acquisition of the geolocation failed because at least one
     * internal source of position returned an internal error.
     */
    static int POSITION_UNAVAILABLE = 2;

    static WritableMap buildError(final int code, final String message) {
        WritableMap error = Arguments.createMap();
        error.putInt("code", code);
        if (message != null) {
            error.putString("message", message);
        }
        return error;
    }
}
