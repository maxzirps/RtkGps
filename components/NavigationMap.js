import React, {useEffect, useRef} from 'react';
import {View, StyleSheet, Text} from 'react-native';
import MapView, {Polyline} from 'react-native-maps';
import carImage from '../assets/car.png';

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
        minZoomLevel={15}
        initialRegion={{
          ...curPos,
          latitudeDelta: 0.0922,
          longitudeDelta: 0.0421,
          curAng: 45,
        }}>
        <MapView.Marker
          coordinate={curPos}
          anchor={{x: 0.5, y: 0.5}}
          image={carImage}
        />
        {isPathsVisible && (
          <>
            <Polyline
              coordinates={loadedPath}
              strokeColor="#001eb2"
              strokeWidth={6}
            />
            <Polyline
              coordinates={drivenPath}
              strokeColor="#cd4746"
              strokeColors={[
                '#7F0000',
                '#00000000', // no color, creates a "long" gradient between the previous and next coordinate
                '#B24112',
                '#E5845C',
                '#238C23',
                '#7F0000',
              ]}
              strokeWidth={6}
            />
          </>
        )}
      </MapView>
      <View style={styles.bottomRightOfMap}>
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
