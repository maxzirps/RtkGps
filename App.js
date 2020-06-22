import React, {useEffect, useState, useRef} from 'react';
import {
  StyleSheet,
  SafeAreaView,
  Text,
  View,
  Button,
  NativeModules,
  NativeEventEmitter,
} from 'react-native';
import NavigationMap from './NavigationMap';

function Separator() {
  return <View style={styles.separator} />;
}

const App: () => React$Node = () => {
  const [status, setStatus] = useState('Stopped');
  const [solution, setSolution] = useState('');
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
  return (
    <>
      <SafeAreaView style={styles.container}>
        <View style={styles.mapContainer}>
          <NavigationMap curPos={curPos} prevPos={prevPos.current} />
        </View>
        <View>
          <Button
            onPress={() => {
              NativeModules.ControlBridge.start();
              setStatus('Started');
            }}
            title="Start"
            accessibilityLabel="Start RTK Service"
          />
        </View>
        <Separator />
        <View>
          <Button
            onPress={() => {
              NativeModules.ControlBridge.stop();
              setStatus('Stopped');
              setSolution('');
            }}
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
        <Separator />
        <View>
          <Text>
            {status} {solution} {JSON.stringify(curPos)}
          </Text>
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
