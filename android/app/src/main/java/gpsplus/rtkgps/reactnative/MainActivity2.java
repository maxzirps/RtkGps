package gpsplus.rtkgps.reactnative;

import android.os.Bundle;

import com.facebook.react.ReactActivity;

import gpsplus.rtkgps.settings.SettingsHelper;

public class MainActivity2 extends ReactActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsHelper.setDefaultValues(this, false);
    }

    @Override
    protected String getMainComponentName() {
        return "ReactNativeRTKGPS";
    }
}
