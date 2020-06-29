package gpsplus.rtkgps.reactnative;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

import gpsplus.rtkgps.RtkNaviService;


public class ControlBridgeModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    private static RtkNaviService rtkNaviService = null;

    public static void setRtkNaviService(RtkNaviService rtkNaviServiceInput) {
        rtkNaviService = rtkNaviServiceInput;
    }

    public static RtkNaviService getRtkNaviService() {
       return rtkNaviService;
    }


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

    public static void sendToJS(Double latitutde, Double longitude) {
        WritableMap params = Arguments.createMap();
        JSONObject json = new JSONObject();
        try {
            json
                    .put("latitude", latitutde)
                    .put("longitude", longitude)
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        params.putString("solution", json.toString());

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("solution", params);
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