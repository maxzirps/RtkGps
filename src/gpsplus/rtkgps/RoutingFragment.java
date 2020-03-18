package gpsplus.rtkgps;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import gpsplus.rtkgps.view.GTimeView;
import gpsplus.rtkgps.view.SolutionView;
import gpsplus.rtkgps.view.StreamIndicatorsView;
import gpsplus.rtklib.RtkCommon;
import gpsplus.rtklib.RtkControlResult;
import gpsplus.rtklib.RtkServerStreamStatus;
import gpsplus.rtklib.Solution;
import gpsplus.rtklib.constants.SolutionStatus;

import static junit.framework.Assert.assertNotNull;

public class RoutingFragment extends Fragment {
    static final String TAG = RoutingFragment.class.getSimpleName();
    private Timer mStreamStatusUpdateTimer;
    private RtkControlResult mRtkStatus;
    @BindView(R.id.streamIndicatorsView)
    StreamIndicatorsView mStreamIndicatorsView;
    @BindView(R.id.gtimeView)
    GTimeView mGTimeView;
    @BindView(R.id.solutionView)
    SolutionView mSolutionView;
    private RtkServerStreamStatus mStreamStatus;
    private MapView mMapView;
    @BindView(R.id.map_container) ViewGroup mMapViewContainer;
    @BindView(R.id.compass_container) ViewGroup mCompassContainer;

    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;

    public RoutingFragment() {
        mStreamStatus = new RtkServerStreamStatus();
        mRtkStatus = new RtkControlResult();
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMapView = new MapView(inflater.getContext());
        View v = inflater.inflate(R.layout.fragment_routing, container, false);
        ButterKnife.bind(this, v);
        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
        this.mLocationOverlay = new MyLocationNewOverlay(mMyLocationProvider,mMapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        // load a bitmap from the drawable folder
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);

        Bitmap scaled = Bitmap.createScaledBitmap(b, 48, 48, true);
        mLocationOverlay.setPersonIcon(scaled);
        IMapController mapController = mMapView.getController();
        mapController.setZoom(14);
        mapController.setCenter(mLocationOverlay.getMyLocation());
        mMapView.getOverlays().add(mLocationOverlay);
        mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), mMapView);
        mCompassOverlay.enableCompass();
        mMapView.getOverlays().add(mCompassOverlay);

        //needed for pinch zooms
        mMapView.setMultiTouchControls(true);

        //scales tiles to the current screen's DPI, helps with readability of labels
        mMapView.setTilesScaledToDpi(true);

        mMapView.setTileSource(TileSourceFactory.MAPNIK);

        Polyline line = new Polyline();   //see note below!
        List<GeoPoint> pts = new ArrayList<>();


        pts.add(new GeoPoint(48.08609227831706,16.284162998199463
                ));
        pts.add(new GeoPoint( 48.087195985118406,       16.28396987915039
                ));
        pts.add(new GeoPoint( 48.08835700177577,       16.283755302429
                ));
        pts.add(new GeoPoint( 48.08950365916834,       16.28339052200317
                ));
        pts.add(new GeoPoint( 48.091209264740755,       16.28349781036377
                ));
        line.setPoints(pts);
        line.setColor(Color.RED);
        mMapView.getOverlays().add(line);

        mMapViewContainer.addView(mMapView, 0);


        // set a global layout listener which will be called when the layout pass is completed and the view is drawn
        mMapView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        //Remove the listener before proceeding
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mMapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        // measure your views here
                        final Rect rect = mMapView.getProjection().getScreenRect();

                        mCompassOverlay.setCompassCenter(rect.width()/3 - 30, rect.height()/3 - 30);

                    }
                }
        );

        return v;
    }


    @Override
    public void onStart() {
        super.onStart();

        // XXX
        mStreamStatusUpdateTimer = new Timer();
        mStreamStatusUpdateTimer.scheduleAtFixedRate(
                new TimerTask() {
                    Runnable updateStatusRunnable = new Runnable() {
                        @Override
                        public void run() {
                            RoutingFragment.this.updateStatus();
                        }
                    };
                    @Override
                    public void run() {
                        Activity a = getActivity();
                        if (a == null) return;
                        a.runOnUiThread(updateStatusRunnable);
                    }
                }, 200, 2500);
    }


    @Override
    public void onResume() {
        super.onResume();
      //  loadMapPreferences();
        mLocationOverlay.enableMyLocation(mMyLocationProvider);
        mLocationOverlay.enableFollowLocation();
        mCompassOverlay.enableCompass(this.mCompassOverlay.getOrientationProvider());
    }

    @Override
    public void onStop() {
        super.onStop();
        //mPathOverlay.clearPath();
        mStreamStatusUpdateTimer.cancel();
        mStreamStatusUpdateTimer = null;
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mMapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
       // saveMapPreferences();
        mLocationOverlay.disableMyLocation();
        mCompassOverlay.disableCompass();
    }

    void updateStatus() {
        MainActivity ma;
        RtkNaviService rtks;
        int serverStatus;

        // XXX
        ma = (MainActivity)getActivity();

        if (ma == null) return;

        rtks = ma.getRtkService();
        if (rtks == null) {
            serverStatus = RtkServerStreamStatus.STATE_CLOSE;
            mStreamStatus.clear();
        }else {
            rtks.getStreamStatus(mStreamStatus);
            rtks.getRtkStatus(mRtkStatus);
            serverStatus = rtks.getServerStatus();
            // appendSolutions(rtks.readSolutionBuffer());
            mMyLocationProvider.setStatus(mRtkStatus, !mMapView.isAnimating());
            mGTimeView.setTime(mRtkStatus.getSolution().getTime());
            mSolutionView.setStats(mRtkStatus);
        }

        assertNotNull(mStreamStatus.mMsg);

        mStreamIndicatorsView.setStats(mStreamStatus, serverStatus);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView = null;
        mLocationOverlay = null;
        mCompassOverlay = null;
        // ButterKnife.reset(this);
    }

    MyLocationProvider mMyLocationProvider = new MyLocationProvider();
    static class MyLocationProvider implements IMyLocationProvider {

        private Location mLastLocation = new Location("");
        private boolean mLocationKnown = false;
        private IMyLocationConsumer mConsumer;

        @Override
        public boolean startLocationProvider(
                IMyLocationConsumer myLocationConsumer) {
            mConsumer = myLocationConsumer;
            return true;
        }

        @Override
        public void stopLocationProvider() {
            mConsumer = null;
        }

        @Override
        public Location getLastKnownLocation() {
            return mLocationKnown ? mLastLocation : null;
        }

        @Override
        public void destroy() {
            mConsumer = null;
        }

        private void setSolution(Solution s, boolean notifyConsumer) {
            RtkCommon.Position3d pos;
            if (MainActivity.getDemoModeLocation().isInDemoMode() && RtkNaviService.mbStarted) {
                pos=MainActivity.getDemoModeLocation().getPosition();
                if (pos == null)
                    return;
            }else{
                if (s.getSolutionStatus() == SolutionStatus.NONE) {
                    return;
                }
                pos = RtkCommon.ecef2pos(s.getPosition());
            }


            mLastLocation.setTime(s.getTime().getUtcTimeMillis());
            mLastLocation.setLatitude(Math.toDegrees(pos.getLat()));
            mLastLocation.setLongitude(Math.toDegrees(pos.getLon()));
            mLastLocation.setAltitude(pos.getHeight());

            mLocationKnown = true;
            if (mConsumer != null) {
                if (notifyConsumer) {
                    mConsumer.onLocationChanged(mLastLocation, this);
                }else {
                    // XXX
                   // if (DBG) Log.v(TAG, "onLocationChanged() skipped while animating");
                }
            }
        }

        public void setStatus(RtkControlResult status, boolean notifyConsumer) {
            setSolution(status.getSolution(), notifyConsumer);
        }

    };

}
