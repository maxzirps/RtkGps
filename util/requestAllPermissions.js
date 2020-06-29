import {PermissionsAndroid} from 'react-native';

const requestAllPermission = async () => {
  const permissions = [
    PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
    PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
  ];

  try {
    await PermissionsAndroid.requestMultiple(permissions);
  } catch (err) {
    console.error(err);
  }
};

export default requestAllPermission;
