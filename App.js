/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, {useEffect} from 'react';
import {
  StyleSheet,
  SafeAreaView,
  Text,
  Alert,
  View,
  Button,
  NativeModules,
  NativeEventEmitter,
} from 'react-native';
import MapView, {PROVIDER_GOOGLE} from 'react-native-maps';

function Separator() {
  return <View style={styles.separator} />;
}

const App: () => React$Node = () => {
  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    this.eventListener = eventEmitter.addListener('TEST', event => {
      console.log(event.eventProperty); // "someValue"
    });
    return () => {
      this.eventListener.remove();
    };
  }, []);
  return (
    <>
      <SafeAreaView style={styles.container}>
        <View style={styles.mapContainer}>
          <MapView
            provider={PROVIDER_GOOGLE} // remove if not using Google Maps
            style={styles.map}
            region={{
              latitude: 37.78825,
              longitude: -122.4324,
              latitudeDelta: 0.015,
              longitudeDelta: 0.0121,
            }}
          />
        </View>
        <View>
          <Button
            onPress={() => NativeModules.ControlBridge.start()}
            title="Start"
            accessibilityLabel="Start RTK Service"
          />
        </View>
        <Separator />
        <View>
          <Button
            onPress={() => NativeModules.ControlBridge.stop()}
            title="Stop"
            accessibilityLabel="Stop RTK Service"
          />
        </View>
        <Separator />
        <View>
          <Button
            onPress={() => NativeModules.ActivityStarter.navigateToExample()}
            title="Open Legacy App"
            accessibilityLabel="Open legacy app interface"
          />
        </View>
      </SafeAreaView>
    </>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginHorizontal: 16,
  },
  mapContainer: {
    height: 500,
    marginTop: 20,
    marginBottom: 20,
  },
  title: {
    textAlign: 'center',
    marginVertical: 8,
  },
  fixToText: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  separator: {
    marginVertical: 8,
    borderBottomColor: '#737373',
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
});

export default App;
