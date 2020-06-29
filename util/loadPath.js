import DocumentPicker from 'react-native-document-picker';
const RNFS = require('react-native-fs');

const loadPath = async () => {
  try {
    // Open a document picker to select the file to load
    const res = await DocumentPicker.pick({
      type: [DocumentPicker.types.allFiles],
    });

    // Load the file content
    let fileContent = await RNFS.readFile(res.uri, 'utf8');
    let path;
    try {
      path = JSON.parse(fileContent);
      console.log(path);
    } catch (e) {
      console.error(e);
      return [];
    }

    // Small validation of loaded content
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
