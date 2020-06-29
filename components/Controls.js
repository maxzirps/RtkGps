import React, {useState} from 'react';
import {
  StyleSheet,
  Text,
  View,
  Button,
  NativeModules,
  Switch,
} from 'react-native';

const Controls: () => React$Node = ({
  isPathsVisible,
  setIsPathsVisible,
  clearPaths,
  loadPath,
  writePath,
}) => {
  const [isRunning, setIsRunning] = useState(false);

  const toggleRunning = () => {
    if (!isRunning) {
      NativeModules.ControlBridge.start();
    } else {
      writePath();
      NativeModules.ControlBridge.stop();
    }
    setIsRunning(previousState => !previousState);
  };
  const togglePaths = () => {
    setIsPathsVisible(previousState => !previousState);
  };
  return (
    <>
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
              <Switch onValueChange={togglePaths} value={isPathsVisible} />
            </View>
          </View>
        </View>
        <View style={styles.separator} />
        <View style={styles.subBox}>
          <Button
            style={styles.subBoxElement}
            onPress={() => loadPath()}
            title="Load path"
            accessibilityLabel="Load path to display"
          />
          <Button
            style={styles.subBoxElement}
            onPress={() => clearPaths()}
            title="Clear paths"
            accessibilityLabel="Load path to display"
          />
          <Button
            style={styles.subBoxElement}
            onPress={() => NativeModules.ActivityStarter.navigateToExample()}
            title="Settings"
            accessibilityLabel="Open settings"
          />
        </View>
      </View>
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
});

export default Controls;
