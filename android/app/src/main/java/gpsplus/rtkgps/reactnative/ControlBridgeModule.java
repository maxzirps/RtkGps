package gpsplus.rtkgps.reactnative;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Timer;
import java.util.TimerTask;

import gpsplus.rtkgps.MainActivity;
import gpsplus.rtkgps.RtkNaviService;
import gpsplus.rtkgps.StatusFragment;


public class ControlBridgeModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;



    ControlBridgeModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @ReactMethod
    public void start() {
        final Intent rtkServiceIntent = new Intent(RtkNaviService.ACTION_START);
        rtkServiceIntent.setClass(reactContext, RtkNaviService.class);

        reactContext.startService(rtkServiceIntent);

    }

    @ReactMethod
    public void stop() {
        final Intent intent = new Intent(RtkNaviService.ACTION_STOP);
        intent.setClass(reactContext, RtkNaviService.class);
        reactContext.startService(intent);
    }

    public static void sendToJS(String message) {
        WritableMap params = Arguments.createMap();
        params.putString("eventProperty", message);

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("TEST", params);
    }


    @Override
    public String getName() {
        return "ControlBridge";
    }

    RtkNaviService mRtkService;
    boolean mRtkServiceBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get
            // LocalService instance
            RtkNaviService.RtkNaviServiceBinder binder = (RtkNaviService.RtkNaviServiceBinder) service;
            mRtkService = binder.getService();
            mRtkServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mRtkServiceBound = false;
            mRtkService = null;
        }
    };
}