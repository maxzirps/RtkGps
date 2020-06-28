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
  const [drivenPath, setDrivenPath] = useState([]);
  const [loadedPath, setLoadedPath] = useState([]);
  const [isPathsVisible, setIsPathsVisible] = useState(true);
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

  useEffect(() => {
    setTimeout(() => {
      setCurPos({...curPos, longitude: curPos.longitude + 0.01});
    }, 3000);
  }, [curPos]);

  useEffect(() => setDrivenPath([...drivenPath, curPos]), [curPos]);

  return (
    <>
      <SafeAreaView style={styles.container}>
        <View style={styles.mapContainer}>
          <NavigationMap
            curPos={curPos}
            prevPos={prevPos.current}
            loadedPath={[]}
            drivenPath={drivenPath}
            isPathsVisible={isPathsVisible}
          />
        </View>
        <Controls
          isPathsVisible={isPathsVisible}
          setIsPathsVisible={setIsPathsVisible}
        />
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
