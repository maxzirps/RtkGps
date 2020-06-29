const RNFS = require('react-native-fs');

const writePathToFile = async drivenPath => {
  // create a path you want to write to
  // :warning: on iOS, you cannot write into `RNFS.MainBundlePath`,
  // but `RNFS.DocumentDirectoryPath` exists on both platforms and is writable
  var path =
    RNFS.ExternalStorageDirectoryPath + `/drivenpath_${Date.now()}.json`;
  console.log(path);
  // write the file
  RNFS.writeFile(path, JSON.stringify(drivenPath), 'utf8')
    .then(success => {
      console.log('FILE WRITTEN!');
    })
    .catch(err => {
      console.log(err.message);
    });
};

export default writePathToFile;
