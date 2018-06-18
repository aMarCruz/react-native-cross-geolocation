# react-native-cross-geolocation

[![npm][npm-image]](https://www.npmjs.com/package/react-native-cross-geolocation)
[![License][license-image]](LICENSE)

React Native Geolocation complatible module that uses the new Google Location API in Android devices.

This module is for React Native 0.50.x using the Google Gradle plugin 3.1.2 or later.

## Setup

```bash
$ yarn add react-native-cross-geolocation
$ react-native link react-native-cross-geolocation
```

JavaScript import:
```js
import Geolocation from 'react-native-cross-geolocation'
```

### Configuration and Permissions

This section only applies to projects made with `react-native init` or to those made with Create React Native App which have since ejected. For more information about ejecting, please see the [guide](https://github.com/react-community/create-react-native-app/blob/master/EJECTING.md) on the Create React Native App repository.

#### iOS
You need to include the NSLocationWhenInUseUsageDescription key in Info.plist to enable geolocation when using the app. Geolocation is enabled by default when you create a project with react-native init.

In order to enable geolocation in the background, you need to include the 'NSLocationAlwaysUsageDescription' key in Info.plist and add location as a background mode in the 'Capabilities' tab in Xcode.

#### Android
To request access to location, you need to add the following line to your app's AndroidManifest.xml:

```xml
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

Android API >= 18 Positions will also contain a `mocked` boolean to indicate if position was created from a mock provider.
Android API >= 23 Permissions are handled automatically.

#### Methods
- [`setRNConfiguration`](#setrnconfiguration)
- [`requestAuthorization`](#requestauthorization)
- [`getCurrentPosition`](#getcurrentposition)
- [`watchPosition`](#watchposition)
- [`clearWatch`](#clearwatch)
- [`stopObserving`](#stopobserving)

## Reference

### Methods

#### `setRNConfiguration()`
```js
Geolocation.setRNConfiguration(config);
```

Sets configuration options that will be used in all location requests.

Parameters:

NAME | TYPE | REQUIRED | DESCRIPTION
---- | ---- | -------- | -----------
config | object | Yes | See below.

Supported options (optionals):

- `skipPermissionRequests` (boolean, iOS-only) - Defaults to `false`. If `true`, you must request permissions before using Geolocation APIs.
- `lowAccuracyMode` (number, Android-only) - Defaults to LowAccuracyMode.BALANCED.
- `fastestInterval` (number, Android-only) - Defaults to 10000 (10 secs).
- `updateInterval` (number, Android-only) - Defaults to 5000 (5 secs).

#### `requestAuthorization()`
```js
Geolocation.requestAuthorization();
```
Request suitable Location permission based on the key configured on pList. If NSLocationAlwaysUsageDescription is set, it will request Always authorization, although if NSLocationWhenInUseUsageDescription is set, it will request InUse authorization.

#### `getCurrentPosition()`
```js
Geolocation.getCurrentPosition(geo_success, [geo_error], [geo_options]);
```

Invokes the success callback once with the latest location info.

Parameters:

NAME | TYPE | REQUIRED | DESCRIPTION
---- | ---- | -------- | -----------
geo_success | function | Yes | Invoked with latest location info.
geo_error | function | No | Invoked whenever an error is encountered.
geo_options | object | No | See below.

Supported options:

- `timeout` (ms) - Defaults to MAX_VALUE
- `maximumAge` (ms) - Defaults to INFINITY.
- `enableHighAccuracy` (bool) - On Android, if the location is cached this can return almost immediately, or it will request an update which might take a while.

#### `watchPosition()`
```js
Geolocation.watchPosition(success, [error], [options]);
```

Invokes the success callback whenever the location changes. Returns a `watchId` (number).

Parameters:

NAME | TYPE | REQUIRED | DESCRIPTION
---- | ---- | -------- | -----------
success | function | Yes | Invoked whenever the location changes.
error | function | No | Invoked whenever an error is encountered.
options | object | No | See below.

Supported options:

- `timeout` (ms) - Defaults to MAX_VALUE.
- `maximumAge` (ms) - Defaults to INFINITY.
- `enableHighAccuracy` (bool) - Defaults to `false`.
- `distanceFilter` (m)
- `useSignificantChanges` (bool) (unused in Android).

#### `clearWatch()`
```js
Geolocation.clearWatch(watchID);
```

Parameters:

NAME | TYPE | REQUIRED | DESCRIPTION
---- | ---- | -------- | -----------
watchID | number | Yes | Id as returned by `watchPosition()`.

#### `stopObserving()`
```js
Geolocation.stopObserving();
```

Stops observing for device location changes. In addition, it removes all listeners previously registered.

Notice that this method has only effect if the `geolocation.watchPosition(successCallback, errorCallback)` method was previously invoked.

## TODO

- [ ] Enhanced docs
- [ ] Tests

[npm-image]:      https://img.shields.io/npm/v/react-native-cross-geolocation.svg
[license-image]:  https://img.shields.io/npm/l/express.svg
