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
import writePathToFile from './util/writePath';

const initialPosition = {latitude: 37.420814, longitude: -122.081949};

const App: () => React$Node = () => {
  const [drivenPath, setDrivenPath] = useState([]);
  const [loadedPath, setLoadedPath] = useState([]);
  const [isPathsVisible, setIsPathsVisible] = useState(true);
  const [curPos, setCurPos] = useState(initialPosition);
  const prevPos = useRef();
  useEffect(() => {
    prevPos.current = curPos;
  });
  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    eventEmitter.addListener('solution', event => {
      try {
        const pos = JSON.parse(event.solution);
        setCurPos(pos);
      } catch (e) {
        console.error(e);
      }
    });
    return () => {
      eventEmitter.removeAllListeners('solution');
    };
  }, []);

  useEffect(() => {
    if (drivenPath[0] === initialPosition) {
      // remove initialPosition from driven path
      setDrivenPath([...drivenPath.slice(1), curPos]);
    } else {
      setDrivenPath([...drivenPath, curPos]);
    }
  }, [curPos]);

  const onLoadPathClicked = () => {
    loadPath().then(path => setLoadedPath(path));
  };

  const writePath = () => {
    writePathToFile(drivenPath);
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
          writePath={writePath}
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
