# react-native-cross-geolocation

[![npm][npm-image]](https://www.npmjs.com/package/react-native-cross-geolocation)
[![License][license-image]](LICENSE)

React Native Geolocation complatible module that uses the new [Google Play services location API](https://developer.android.com/training/location/) on Android devices.

In my country (México), software developers are poorly paid, so I have had to look for another job to earn a living and I cannot dedicate more time to maintaining this and other repositories that over the years have never generated any money for me. If anyone is interested in maintaining this repository, I'd be happy to transfer it to them, along with the associated npm package. |
:---: |
En mi país (México), los desarrolladores de software somos pésimamente pagados, por lo que he tenido que buscar otro trabajo para ganarme la vida y no puedo dedicar más tiempo a mantener éste y otros repositorios que a través de los años nunca me generaron dinero. Si a alguien le interesa dar mantenimiento a este repositorio, con gusto se lo transferiré, así como el paquete de npm asociado. |

If this library has helped you, please support my work with a star or [buy me a coffee][kofi-url].

## IMPORTANT

This module was tested with React Native 0.59.0, but it should work smoothly with apps that use Gradle 4.6 or later.

\* For previous Gradle versions use react-native-cross-geolocation v1.0.6 or bellow.

## Setup

```bash
yarn add react-native-cross-geolocation
react-native link react-native-cross-geolocation
```

### Play Services Location Version

From v1.1.0, react-native-cross-geolocation supports the global variable `playServicesLocationVersion` to specify the version of 'com.google.android.gms:play-services-location' to use. It defaults to 16.0.0

### Configuration and Permissions

This section only applies to projects made with `react-native init` or to those made with Create React Native App which have since ejected. For more information about ejecting, please see the [guide](https://github.com/react-community/create-react-native-app/blob/master/EJECTING.md) on the Create React Native App repository.

#### iOS

You need to include the NSLocationWhenInUseUsageDescription key in Info.plist to enable geolocation when using the app. Geolocation is enabled by default when you create a project with react-native init.

In order to enable geolocation in the background, you need to include the 'NSLocationAlwaysUsageDescription' key in Info.plist and add location as a background mode in the 'Capabilities' tab in Xcode.

#### Android

To request access to location, add the following line to your app's AndroidManifest.xml:

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

Supported options:

- `skipPermissionRequests` (boolean, iOS-only) - Defaults to `false`. If `true`, you must request permissions before using Geolocation APIs.
- `lowAccuracyMode` (number, Android-only) - Defaults to [LowAccuracyMode.BALANCED](#constants).
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
- `distanceFilter` (m) - Defaults to 100.
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

### Constants

rnCrossGeolocation exports a `LowAccuracyMode` object with values to be used in the `lowAccuracyMode` property of the parameter sent to [`setRNConfiguration`](#setrnconfiguration):

NAME | DETAILS
---- | -------
LowAccuracyMode.BALANCED<br>(102)<br>This is the default mode. | Request location precision to within a city block, which is an accuracy of approximately 100 meters.<br>This is considered a coarse level of accuracy, and is likely to consume less power. With this setting, the location services are likely to use WiFi and cell tower positioning. Note, however, that the choice of location provider depends on many other factors, such as which sources are available.
LowAccuracyMode.LOW_POWER<br>(104) | Request city-level precision, which is an accuracy of approximately 10 Km.<br>This is considered a coarse level of accuracy, and is likely to consume less power.
LowAccuracyMode.NO_POWER<br>(105) | Use this if you need negligible impact on power consumption, but want to receive location updates when available.<br>With this setting, your app does not trigger any location updates, but receives locations triggered by other apps.

_**NOTE:** These constants are only for Android, on iOS they are undefined._

## TODO

- [ ] Tests

## Support my Work

I'm a full-stack developer with more than 20 year of experience and I try to share most of my work for free and help others, but this takes a significant amount of time and effort so, if you like my work, please consider...

[<img src="https://amarcruz.github.io/images/kofi_blue.png" height="36" title="Support Me on Ko-fi" />][kofi-url]

Of course, feedback, PRs, and stars are also welcome 🙃

Thanks for your support!

[npm-image]:      https://img.shields.io/npm/v/react-native-cross-geolocation.svg
[license-image]:  https://img.shields.io/npm/l/express.svg
[kofi-url]:       https://ko-fi.com/C0C7LF7I
