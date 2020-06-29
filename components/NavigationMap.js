import React, {useEffect, useRef} from 'react';
import {View, StyleSheet, Text} from 'react-native';
import MapView, {Polyline} from 'react-native-maps';
import carImage from '../assets/car.png';

/**
 * https://github.com/react-native-community/react-native-maps/blob/master/example/examples/AnimatedNavigation.js
 */
const NavigationMap: () => React$Node = ({
  prevPos,
  curPos,
  loadedPath,
  drivenPath,
  isPathsVisible,
}) => {
  const mapEl = useRef(null);
  useEffect(() => {
    updateMap();
  });

  const getRotation = () => {
    if (!prevPos) {
      return 0;
    }
    const xDiff = curPos.latitude - prevPos.latitude;
    const yDiff = curPos.longitude - prevPos.longitude;
    return (Math.atan2(yDiff, xDiff) * 180.0) / Math.PI;
  };

  const updateMap = () => {
    const curAng = 45;
    const curRot = getRotation();
    mapEl.current.animateCamera({
      heading: curRot || 0,
      center: curPos,
      pitch: curAng,
    });
  };
  return (
    <View style={styles.flex}>
      <MapView
        ref={mapEl}
        style={styles.flex}
        initialRegion={{
          latitude: 37.420814,
          longitude: -122.081949,
          latitudeDelta: 0.0922,
          longitudeDelta: 0.0421,
          curAng: 45,
        }}>
        {/* Display car icon */}
        <MapView.Marker
          coordinate={curPos}
          anchor={{x: 0.5, y: 0.5}}
          image={carImage}
        />
        {isPathsVisible && (
          // Display paths
          <>
            <Polyline
              coordinates={loadedPath}
              strokeColor="#001eb2"
              strokeWidth={6}
            />
            <Polyline
              coordinates={drivenPath}
              strokeColor="#cd4746"
              strokeWidth={6}
            />
          </>
        )}
      </MapView>
      <View style={styles.bottomRightOfMap}>
        {/* Display position on right bottom of map */}
        <Text>
          {(curPos.latitude + '').substr(0, 7)} |{' '}
          {(curPos.longitude + '').substr(0, 7)}
        </Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  flex: {
    flex: 1,
    width: '100%',
  },
  bottomRightOfMap: {
    position: 'absolute',
    right: 0,
    bottom: 0,
    backgroundColor: 'white',
    padding: 2,
    minWidth: 120,
  },
});

export default NavigationMap;
