import React, {useEffect, useState, useRef} from 'react';
import {
  StyleSheet,
  SafeAreaView,
  Text,
  View,
  Button,
  NativeModules,
  NativeEventEmitter,
  Switch,
} from 'react-native';
import NavigationMap from './NavigationMap';

const App: () => React$Node = () => {
  const [isRunning, setIsRunning] = useState(false);
  const [showPaths, setShowPaths] = useState(false);
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

  const toggleRunning = () => setIsRunning(previousState => !previousState);
  const togglePaths = () => setShowPaths(previousState => !previousState);
  return (
    <>
      <SafeAreaView style={styles.container}>
        <View style={styles.mapContainer}>
          <NavigationMap curPos={curPos} prevPos={prevPos.current} />
        </View>
        <View style={styles.lowerBox}>
          <View style={styles.subBox}>
            <View style={styles.subBoxElement}>
              <View style={{flexDirection: 'row'}}>
                <Text>Running</Text>
                <Switch onValueChange={toggleRunning} value={isRunning} />
              </View>
            </View>
            <View style={styles.subBoxElement}>
              <View style={{flexDirection: 'row'}}>
                <Text>Display paths</Text>
                <Switch onValueChange={togglePaths} value={showPaths} />
              </View>
            </View>
          </View>
          <View style={styles.separator} />
          <View style={styles.subBox}>
            <Button
              style={styles.subBoxElement}
              onPress={() => NativeModules.ActivityStarter.navigateToExample()}
              title="Load path"
              accessibilityLabel="Load path to display"
            />
            <Button
              style={styles.subBoxElement}
              onPress={() => NativeModules.ActivityStarter.navigateToExample()}
              title="Export path"
              accessibilityLabel="Open settings"
            />
            <Button
              style={styles.subBoxElement}
              onPress={() => NativeModules.ActivityStarter.navigateToExample()}
              title="Settings"
              accessibilityLabel="Open settings"
            />
          </View>
        </View>
      </SafeAreaView>
    </>
  );
};

const styles = StyleSheet.create({
  separator: {
    marginHorizontal: 8,
    borderLeftColor: '#737373',
    borderLeftWidth: StyleSheet.hairlineWidth,
  },
  subBoxElement: {
    flex: 0.5,
  },
  subBox: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'space-between',
  },
  lowerBox: {
    flex: 0.25,
    flexDirection: 'row',
    justifyContent: 'space-between',
    flexWrap: 'wrap',
  },
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
