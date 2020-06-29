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
import loadPath from './util/loadPath';
import requestAllPermission from './util/requestAllPermissions';

const initialPosition = {latitude: 37.420814, longitude: -122.081949};

const App: () => React$Node = () => {
  const [drivenPath, setDrivenPath] = useState([]);
  const [loadedPath, setLoadedPath] = useState([]);
  const [isPathsVisible, setIsPathsVisible] = useState(true);
  const [curPos, setCurPos] = useState(initialPosition);
  // Store previous position; this is needed for NavigationMap
  const prevPos = useRef();
  useEffect(() => {
    prevPos.current = curPos;
  });

  useEffect(() => {
    requestAllPermission();
  }, []);

  useEffect(() => {
    // Add an event listener to receive the position calculated by RtkGps
    const eventEmitter = new NativeEventEmitter(NativeModules.ControlBridge);
    eventEmitter.addListener('solution', event => {
      try {
        const pos = JSON.parse(event.solution);
        setCurPos(pos);
      } catch (e) {
        console.error(e);
      }
    });
    return () => {
      // Remove the event listener on close
      eventEmitter.removeAllListeners('solution');
    };
  }, []);

  useEffect(() => {
    // Add the new position to our drivenPath array
    if (drivenPath[0] === initialPosition) {
      // remove initialPosition from driven path
      setDrivenPath([...drivenPath.slice(1), curPos]);
    } else {
      setDrivenPath([...drivenPath, curPos]);
    }
  }, [curPos]);

  const onLoadPathClicked = () => {
    // Load path button was clicked, load path from file
    loadPath().then(path => setLoadedPath(path));
  };

  return (
    <>
      <SafeAreaView style={styles.container}>
        <View style={styles.mapContainer}>
          <NavigationMap
            curPos={curPos}
            prevPos={prevPos.current}
            loadedPath={loadedPath}
            drivenPath={drivenPath}
            isPathsVisible={isPathsVisible}
          />
        </View>
        <Controls
          isPathsVisible={isPathsVisible}
          clearPaths={() => {
            setDrivenPath([]);
            setLoadedPath([]);
          }}
          loadPath={onLoadPathClicked}
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
