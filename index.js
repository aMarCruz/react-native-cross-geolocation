import {
  Platform,
} from 'react-native'

const Geolocation = Platform.OS === 'android'
  ? require('./geoloc')
  : navigator.geolocation

export default Geolocation
