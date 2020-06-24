import React, {useEffect, useState, useRef} from 'react';
import {
  StyleSheet,
  SafeAreaView,
  View,
  NativeEventEmitter,
  NativeModules,
} from 'react-native';
import NavigationMap from './components/NavigationMap';
import Controls from './components/Controls';

const App: () => React$Node = () => {
  const [curPos, setCurPos] = useState({
    latitude: 37.420814,
    longitude: -122.081949,
  });
  const prevPos = useRef();
  useEffect(() => {
    prevPos.current = curPos;
  });
  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    eventEmitter.addListener('solution', event => {
      if (event.latitude) {
        setCurPos({...curPos, latitude: event.latitude});
      }
      if (event.longitude) {
        setCurPos({...curPos, longitude: event.longitude});
      }
    });
    return () => {
      eventEmitter.removeAllListeners('solution');
    };
  }, []);
  return (
    <>
      <SafeAreaView style={styles.container}>
        <View style={styles.mapContainer}>
          <NavigationMap curPos={curPos} prevPos={prevPos.current} />
        </View>
        <Controls />
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
    flex: 0.7,
    marginTop: 20,
    marginBottom: 20,
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
});

export default App;
