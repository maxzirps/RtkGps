import DocumentPicker from 'react-native-document-picker';
const RNFS = require('react-native-fs');

const loadPath = async () => {
  try {
    const res = await DocumentPicker.pick({
      type: [DocumentPicker.types.allFiles],
    });

    let fileContent = await RNFS.readFile(res.uri, 'utf8');
    let path;
    try {
      path = JSON.parse(fileContent);
      console.log(path);
    } catch (e) {
      console.error(e);
      return [];
    }

    if (Array.isArray(path)) {
      let isValidPath = true;
      path.forEach(coordinates => {
        if (!coordinates.latitude || !coordinates.longitude) {
          isValidPath = false;
        }
      });
      if (isValidPath) {
        return path;
      }
    }
    return [];
  } catch (err) {
    if (DocumentPicker.isCancel(err)) {
      // User cancelled the picker, exit any dialogs or menus and move on
    } else {
      throw err;
    }
  }
};

export default loadPath;
