package gpsplus.rtkgps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class RawGNSSToRtklib {

    static final String TAG = RawGNSSToRtklib.class.getSimpleName();
    private static final boolean DBG = BuildConfig.DEBUG & true;
    final LocalSocketThread mLocalSocketThread;
    final RawGNSSSServiceThread rawThread;

    private Callbacks mCallbacks;


    public interface Callbacks {

        public void onConnected();

        public void onStopped();

        public void onConnectionLost();

    }

    public void setCallbacks(Callbacks callbacks) {
        if (callbacks == null) throw new IllegalStateException();
        mCallbacks = callbacks;
    }


    public RawGNSSToRtklib(Context serviceContext, @Nonnull String localSocketPath) {
        mLocalSocketThread = new LocalSocketThread(localSocketPath);
        mLocalSocketThread.setBindpoint(localSocketPath);
        rawThread = new RawGNSSSServiceThread(serviceContext);
    }

    public void start() {
        Log.v(TAG, "RawGNSSToRtklib started");
        mLocalSocketThread.start();
        rawThread.start();
    }

    public void stop() {
        rawThread.cancel();
        mLocalSocketThread.cancel();
    }


    private final class LocalSocketThread extends RtklibLocalSocketThread {

        public LocalSocketThread(String socketPath) {
            super(socketPath);
        }

        @Override
        protected boolean isDeviceReady() {
            return true;
        }

        @Override
        protected void waitDevice() {
            if (DBG) Log.v(TAG, "wait RAWGNSS");
        }

        @Override
        protected boolean onDataReceived(byte[] buffer, int offset, int count) {
            Log.v(TAG, "DATA RECEIVED");
            // TODO: here the correction data is received?
            return true;
        }

        @Override
        protected void onLocalSocketConnected() {
            Log.v(TAG, "LOCALSOCKET CONNECTED");
            mCallbacks.onConnected();
        }
    }

    private class RawGNSSSServiceThread extends Thread {

        private final LocationListener locationListener =
                new LocationListener() {

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.v(TAG, "locationListener/onProviderEnabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.v(TAG, "locationListener/onProviderDisabled");
                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        // Log.v(TAG, "locationListener/onLocationChanged\"");
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        Log.v(TAG, "locationListener/onStatuschanged");
                    }
                };
        private final long LOCATION_RATE_GPS_MS = TimeUnit.SECONDS.toMillis(1L);
        private final long LOCATION_RATE_NETWORK_MS = TimeUnit.SECONDS.toMillis(60L);
        private final GnssMeasurementsEvent.Callback gnssMeasurementsEventListener =
                new GnssMeasurementsEvent.Callback() {
                    @Override
                    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {


                      //  Log.v(TAG, "GnssMeasurementsEvent onGnssMeasurementsReceived " + event);
                        Log.v(TAG, "GnssMeasurementsEvent onGnssMeasurementsReceived");
                        try {
                            // TODO: sent rtcm 3 message here to test
                            mLocalSocketThread.write(new byte[4096], 0, 1000);
                            Log.v(TAG, "wrote message");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStatusChanged(int status) {
                        String statusString = "";
                        if (status == 0){
                            statusString = "STATUS_NOT_SUPPORTED";
                        } else if (status == 1) {
                            statusString ="STATUS_READY";
                        } else if (status == 2) {
                            statusString = "STATUS_LOCATION_DISABLED";
                        }
                        Log.v(TAG, "GnssMeasurementsEvent onStatusChanged " + statusString);
                    }
                };
        private final GnssNavigationMessage.Callback gnssNavigationMessageListener =
                new GnssNavigationMessage.Callback() {
                    @Override
                    public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {

                        Log.v(TAG, "GnssNavigationMessage data " + event.getData());
                    }

                    @Override
                    public void onStatusChanged(int status) {
                        String statusString = "";
                        if (status == 0){
                            statusString = "STATUS_NOT_SUPPORTED";
                        } else if (status == 1) {
                            statusString ="STATUS_READY";
                        } else if (status == 2) {
                            statusString = "STATUS_LOCATION_DISABLED";
                        }
                        Log.v(TAG, "GnssNavigationMessage onStatusChanged " + statusString);
                    }
                };
        private final GnssStatus.Callback gnssStatusListener =
                new GnssStatus.Callback() {
                    @Override
                    public void onStarted() {
                        Log.v(TAG, "GnssStatus onStarted");
                    }

                    @Override
                    public void onStopped() {
                        Log.v(TAG, "GnssStatus onStopped");
                    }

                    @Override
                    public void onFirstFix(int ttff) {
                        Log.v(TAG, "GnssStatus onFirstFix");
                    }

                    @Override
                    public void onSatelliteStatusChanged(GnssStatus status) {
                       // Log.v(TAG, "GnssStatus onSatelliteStatusChanged" + status);
                    }
                };
        private final OnNmeaMessageListener nmeaListener =
                new OnNmeaMessageListener() {
                    @Override
                    public void onNmeaMessage(String s, long l) {
                      //  Log.v(TAG, "OnNmeaMessageListener onNmeaMessage" + s);
                    }
                };
        private boolean isRunning;
        private LocationManager locationManager;
        // TODO:
        private long registrationTimeNanos = 0L;
        private long firstLocationTimeNanos = 0L;
        private long ttff = 0L;
        private boolean firstTime = true;



        public RawGNSSSServiceThread(Context context) {
            this.locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            this.isRunning = true;
            this.registerAll();
        }

        public void cancel() {
            this.isRunning = false;
            mCallbacks.onStopped();
            this.unregisterAll();
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN RawGNSSToLocalSocket-RG");
            setName("RawGNSSToLocalSocket-RG");
/*
            try {
                while (this.isRunning) {
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.isRunning = false;
            }

  */
        }

        private void registerLocation() throws SecurityException {
            boolean isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGpsProviderEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        LOCATION_RATE_NETWORK_MS,
                        0.0f /* minDistance */,
                        locationListener);
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOCATION_RATE_GPS_MS,
                        0.0f /* minDistance */,
                        locationListener);
            }
        }

        public void unregisterLocation() {
            locationManager.removeUpdates(locationListener);
        }

        public void registerMeasurements() throws SecurityException {
            locationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventListener);
        }

        public void unregisterMeasurements() {
            locationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsEventListener);
        }

        public void registerNavigation() {

            locationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageListener);
        }

        public void unregisterNavigation() {
            locationManager.unregisterGnssNavigationMessageCallback(gnssNavigationMessageListener);
        }

        public void registerGnssStatus() throws SecurityException {
            locationManager.registerGnssStatusCallback(gnssStatusListener);
        }

        public void unregisterGpsStatus() {
            locationManager.unregisterGnssStatusCallback(gnssStatusListener);
        }

        public void registerNmea()throws SecurityException  {
            locationManager.addNmeaListener(nmeaListener);
        }

        public void unregisterNmea() {
            locationManager.removeNmeaListener(nmeaListener);
        }

        private void registerAll() {
            Log.v(TAG, "registerAll");
            try {
                registerLocation();
                registerMeasurements();
                registerNavigation();
                registerGnssStatus();
                registerNmea();
            }catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void unregisterAll() {
            Log.v(TAG, "unregisterAll");
            unregisterLocation();
            unregisterMeasurements();
            unregisterNavigation();
            unregisterGpsStatus();
            unregisterNmea();
        }

        private void registerSingleNetworkLocation() throws SecurityException {
            boolean isNetworkProviderEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkProviderEnabled) {
                locationManager.requestSingleUpdate(
                        LocationManager.NETWORK_PROVIDER, locationListener, null);
            }
        }
        // TODO:
        private void registerSingleGpsLocation() throws SecurityException {
            boolean isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGpsProviderEnabled) {
                this.firstTime = true;
                registrationTimeNanos = SystemClock.elapsedRealtimeNanos();
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }
        }

    }


}
