import React, {useEffect, useRef} from 'react';
import {View, StyleSheet} from 'react-native';
import MapView from 'react-native-maps';
import carImage from './assets/car.png';

const NavigationMap: () => React$Node = ({prevPos, curPos}) => {
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
      </MapView>
    </View>
  );
};

const styles = StyleSheet.create({
  flex: {
    flex: 1,
    width: '100%',
  },
});

export default NavigationMap;
