# ⚛️ React-Native-RTKGPS

Adding a react-native GUI on [RtkGps](https://github.com/eltorio/RtkGps)

## Setup

1. [Set up your local developer environment for react-native](https://reactnative.dev/docs/environment-setup)
2. `git submodule init && git submodule update`
3. `yarn install`
4. Open the Android sdk manager and install
* NDK
* Google Play services
5. Add your google maps api key in  the [AndroidManifest](/android/app/src/main/AndroidManifest.xml)
```
<meta-data
android:name="com.google.android.geo.API_KEY"
android:value="<YOUR_KEY_HERE"/>
```
See [this](https://github.com/react-native-community/react-native-maps/blob/master/docs/installation.md) for more information.

6. Plug in your android device
7. Open two terminals and enter
- `yarn start` in the first terminal
- `yarn adb-reverse && yarn android` in the second terminal

## Controls & Configuration

### Main view
![start](/doc/start.png) 

- *Running* starts the location service
- *Display paths* shows/hides the loaded path and driven path
- *Load Path* opens a window to load a path from a .json file to display on the map
the file needs to have the following structure:
```json
[
  { "longitude": -122.081949, "latitude": -90 },
  { "longitude": -123.081949, "latitude": -91 },
  ...
]
```
- *Clear paths* deletes the loaded and driven path
- *Settings* Opens the legacy settings page (see below)

### Settings
![settings](/doc/settings.png)  

Here you can see a detailed status and configure the location service.

### Status
![status](/doc/status.png)  
The upper half displays a detailed status of the positioning service (e.g. number of satellites). Once it displays **FLOAT** it calculated an accurate position.

In the top right corner you can see some status indicators: 
1. input rover
2. input base
3. input correction
4. processing status
5. output solution 1
6. output solution 2 

The lower half displays the satellites of the rover and of the base and their signal strength.

### Input rover
![input_rover](/doc/input_rover.png)  
(In the settings click on input streams.)

Here you can set the input configuration for the rover. The picture shows the settings for a Navilock receiver via usb. (Antenna, commands and receiver option stay unchanged.)

### Input base
![input_base](/doc/input_base.png) 

You can use free NTRIP casters (e.g. [Rtk2go](http://rtk2go.com/))

Simply change the stream settings and format to your desired NTRIP stream. (everything else stays unchanged)


### Processing options
![processing](/doc/processing.png)  

Change the processing options to your desired needs. For the Navilock receiver the displayed settings are chosen. (Only need to set *Positioning mode, frequencies, navigation system*)

### Output solutions
![output](/doc/output.png)  

If you want to write the calculated position to a file, set your settings here. I recommend enabling both, solution 1 and 2, since for me only one of both worked.
At the beginning of your recording also check the status indicators (see status chapter above) to see if it really writes something to the drive.
