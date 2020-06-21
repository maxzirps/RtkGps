package gpsplus.rtkgps.reactnative;


import android.content.Intent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import gpsplus.rtkgps.MainActivity;
import gpsplus.rtkgps.RtkNaviService;


public class ControlBridgeModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;


    ControlBridgeModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @ReactMethod
    public void start() {
        ReactApplicationContext context = getReactApplicationContext();
        final Intent rtkServiceIntent = new Intent(RtkNaviService.ACTION_START);
        rtkServiceIntent.putExtra(RtkNaviService.EXTRA_SESSION_CODE, "17217461274uhjas");
        rtkServiceIntent.setClass(context, RtkNaviService.class);

        rtkServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(rtkServiceIntent);
    }

    @ReactMethod
    public void stop() {
        ReactApplicationContext context = getReactApplicationContext();
        final Intent intent = new Intent(RtkNaviService.ACTION_STOP);
        intent.setClass(context, RtkNaviService.class);
        context.startService(intent);
    }

    @ReactMethod
    public void getPosition() {
        WritableMap params = Arguments.createMap();
        params.putString("eventProperty", "someValue bla bla bla");

        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("TEST", params);
    }

    @Override
    public String getName() {
        return "ControlBridge";
    }
}