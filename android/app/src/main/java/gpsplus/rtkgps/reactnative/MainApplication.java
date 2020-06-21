package gpsplus.rtkgps.reactnative;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StrictMode;
import android.util.Log;

// import com.airbnb.android.react.maps.MapsPackage;
import com.airbnb.android.react.maps.MapsPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.proj4.PJ;

import java.util.Arrays;
import java.util.List;

import gpsplus.ntripcaster.NTRIPCaster;
import gpsplus.rtkgps.BuildConfig;


public class MainApplication extends Application implements ReactApplication {

    private static final boolean DBG = BuildConfig.DEBUG & true;
    private static String VERSION = "";

    @Override
    public void onCreate() {
        if (DBG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate();
        Log.d("AAAAAAAAA", "AAAAAAAAAAAAAAAa");
        //ACRA.init(this);
        System.loadLibrary("proj");
        Log.v("Proj4","Proj4 version: "+PJ.getVersion());

        System.loadLibrary("ntripcaster");
        Log.v("ntripcaster","NTRIP Caster "+NTRIPCaster.getVersion());

        System.loadLibrary("rtkgps");

        //System.loadLibrary("gdalalljni"); //Automaticaly done
        ogr.RegisterAll();
        gdal.AllRegister();
        Log.v("GDAL",gdal.VersionInfo("--version"));
        //set version
        PackageInfo pi;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            MainApplication.VERSION = pi.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String getVersion() {
        return MainApplication.VERSION;
    }


    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            Log.d("TEST", "ADDED PACKAGES AAAAAAAAAAAAAAAA");
            return Arrays.asList(
                    new MainReactPackage(),
                    new ActivityStarterPackage(),
                    new ControlBridgePackage(),
                    new MapsPackage());
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }

    };

    @Override
    public ReactNativeHost getReactNativeHost() {

        Log.d("TEST", "AAAAAAAAAAAAAAAAAAAA");
        return mReactNativeHost;
    }
}
