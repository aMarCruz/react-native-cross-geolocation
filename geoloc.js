import {
  NativeEventEmitter,
  NativeModules,
  PermissionsAndroid,
  Platform,
} from 'react-native'
import invariant from 'invariant'
//const logError = require('logError');
//const warning = require('fbjs/lib/warning');

const RNGeolocation = NativeModules.LocationObserver
const LocationEventEmitter = new NativeEventEmitter(RNGeolocation)
const logError = () => {}

var nextWatchID = 1
var subscriptions = {}
var updatesEnabled = false

/**
 * The Geolocation API extends the web spec:
 * https://developer.mozilla.org/en-US/docs/Web/API/Geolocation
 *
 * See https://facebook.github.io/react-native/docs/geolocation.html
 */
module.exports = {

  LowAccuracyMode: {
    BALANCED: RNGeolocation.BALANCED,
    LOW_POWER: RNGeolocation.LOW_POWER,
    NO_POWER: RNGeolocation.NO_POWER,
  },

  /*
    * Sets configuration options that will be used in all location requests.
    *
    * See https://facebook.github.io/react-native/docs/geolocation.html#setrnconfiguration
    *
    */
  setRNConfiguration: RNGeolocation.setConfiguration,

  /*
   * Request suitable Location permission based on the key configured on pList.
   *
   * See https://facebook.github.io/react-native/docs/geolocation.html#requestauthorization
   */
  requestAuthorization () {},

  /*
   * Invokes the success callback once with the latest location info.
   *
   * See https://facebook.github.io/react-native/docs/geolocation.html#getcurrentposition
   */
  getCurrentPosition(geo_success, geo_error, geo_options) {
    invariant(
      typeof geo_success === 'function',
      'Must provide a valid geo_success callback.'
    )
    let promise

    // Supports Android's new permission model. For Android older devices,
    // it's always on.
    if (Platform.Version >= 23) {
      promise = PermissionsAndroid.check(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
      ).then((hasPermission) => {
        if (hasPermission) {
          return true
        }
        return PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
        )
      })
    } else {
      promise = Promise.resolve()
    }

    promise.then(() => {
      // native module will call geo_error if has no parmissions
      RNGeolocation.getCurrentPosition(
        geo_options || {},
        geo_success,
        geo_error || logError
      )
    })
  },

  /*
   * Invokes the success callback whenever the location changes.
   *
   * See https://facebook.github.io/react-native/docs/geolocation.html#watchposition
   */
  watchPosition (success, error, options) {
    if (!updatesEnabled) {
      RNGeolocation.startObserving(options || {})
      updatesEnabled = true
    }
    const watchID = String(nextWatchID++)
    subscriptions[watchID] = [
      LocationEventEmitter.addListener(
        'geolocationDidChange',
        success
      ),
      error ? LocationEventEmitter.addListener(
        'geolocationError',
        error
      ) : null,
    ]
    return watchID
  },

  clearWatch (watchID) {
    var sub = subscriptions[watchID]
    if (!sub) {
      // Silently exit when the watchID is invalid or already cleared
      // This is consistent with timers
      return
    }

    sub[0].remove()
    // array element refinements not yet enabled in Flow
    if (sub[1]) {
      sub[1].remove()
    }
    delete subscriptions[watchID]
    for (var p in subscriptions) {
      if (subscriptions.hasOwnProperty(p)) {
        return // still valid subscriptions
      }
    }
    RNGeolocation.stopObserving()
  },

  stopObserving () {
    if (updatesEnabled) {
      RNGeolocation.stopObserving()
      updatesEnabled = false
      Object.keys(subscriptions).forEach((id) => {
        const sub = subscriptions[id]
        if (sub) {
          //warning(false, 'Called stopObserving with existing subscriptions.');
          sub[0].remove()
          if (sub[1]) {
            sub[1].remove()
          }
        }
      })
      subscriptions = {}
    }
  }
}
