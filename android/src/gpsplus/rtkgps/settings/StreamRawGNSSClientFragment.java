package gpsplus.rtkgps.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

import javax.annotation.Nonnull;

import gpsplus.rtkgps.BuildConfig;
import gpsplus.rtkgps.MainActivity;
import gpsplus.rtkgps.R;
import gpsplus.rtklib.RtkServerSettings.TransportSettings;
import gpsplus.rtklib.constants.StreamType;


public class StreamRawGNSSClientFragment extends PreferenceFragment {

    private static final boolean DBG = BuildConfig.DEBUG & true;


    private String mSharedPrefsName;

    public static final class Value implements TransportSettings, Cloneable {

        private String mPath;
        @Override
        public StreamType getType() {
            return StreamType.RAWGNSS;
        }

        @Nonnull
        public static String rgLocalSocketName(String stream) {
            return "rg_" + stream; // + "_" + address.replaceAll("\\W", "_");
        }

        @Override
        public String getPath() {
            return mPath;
        }

        public void updatePath(Context context, String sharedPrefsName) {
            mPath = MainActivity.getLocalSocketPath(context,
                    rgLocalSocketName(sharedPrefsName)).getAbsolutePath();
        }

        @Override
        protected Value clone() {
            try {
                return (Value)super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Value copy() {
            return clone();
        }


    }


    public StreamRawGNSSClientFragment() {
        super();
        mSharedPrefsName = StreamRawGNSSClientFragment.class.getSimpleName();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments;

        arguments = getArguments();
        if (arguments == null || !arguments.containsKey(StreamDialogActivity.ARG_SHARED_PREFS_NAME)) {
            throw new IllegalArgumentException("ARG_SHARED_PREFFS_NAME argument not defined");
        }

        mSharedPrefsName = arguments.getString(StreamDialogActivity.ARG_SHARED_PREFS_NAME);

        if (DBG) Log.v(mSharedPrefsName, "onCreate()");

        getPreferenceManager().setSharedPreferencesName(mSharedPrefsName);



    }



    @Override
    public void onResume() {
        super.onResume();
        if (DBG) Log.v(mSharedPrefsName, "onResume()");
    }

    @Override
    public void onPause() {
        if (DBG) Log.v(mSharedPrefsName, "onPause()");
        super.onPause();
    }




    public static Value readSettings(Context context, SharedPreferences prefs, String sharedPrefsName) {

        Value v = new Value();
        v.updatePath(context, sharedPrefsName);
        return v;
    }

    public static String readSummary(SharedPreferences prefs) {
        return "RawGNSS";
    }


}
