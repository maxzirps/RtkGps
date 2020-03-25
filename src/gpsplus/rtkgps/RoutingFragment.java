package gpsplus.rtkgps;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
    @BindView(R.id.map_container)
    ViewGroup mMapViewContainer;
    @BindView(R.id.compass_container)
    ViewGroup mCompassContainer;
    private Polyline drivenPath;
    private Polyline loadedPath;
    private Boolean followPosition;

    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;

    public RoutingFragment() {
        mStreamStatus = new RtkServerStreamStatus();
        mRtkStatus = new RtkControlResult();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_routing, menu);
    }

    private void clearPaths() {
        if (drivenPath != null) drivenPath.setPoints(new ArrayList<GeoPoint>());
        if (loadedPath != null) loadedPath.setPoints(new ArrayList<GeoPoint>());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_load_path:
                loadPath();
                break;
            case R.id.menu_clear_paths:
                clearPaths();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private ArrayList<GeoPoint> loadPath() {
        FileChooser fileChooser = new FileChooser(getActivity());

        fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                String filename = file.getAbsolutePath();
                Log.i("File Name", filename);
                StringBuilder content = new StringBuilder();
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        content.append(line);
                        content.append('\n');
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    ;
                }
                addPathToMap(content.toString());

            }
        });

        fileChooser.setExtension("json");
        fileChooser.showDialog();
        return new ArrayList<GeoPoint>();
    }

    private void addPathToMap(String text) {
        JSONArray array;
        ArrayList<GeoPoint> path = new ArrayList<>();
        try {
            array = new JSONObject(text).getJSONArray("coordinates");
            for (int i = 0; i < array.length(); i++) {
                path.add(new GeoPoint(array.getJSONArray(i).getDouble(1), array.getJSONArray(i).getDouble(0)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG, path.toString());
        if (path.size() == 0) {
            Toast.makeText(getActivity(), "Error reading path file",
                    Toast.LENGTH_LONG).show();
        } else {
            loadedPath = new Polyline();
            loadedPath.setPoints(path);
            loadedPath.getOutlinePaint().setColor(Color.rgb(255, 132, 0));
            loadedPath.getOutlinePaint().setStrokeWidth(2);
            mMapView.getOverlays().add(loadedPath);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        followPosition = false;
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
        this.mLocationOverlay = new MyLocationNewOverlay(mMyLocationProvider, mMapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        // load a bitmap from the drawable folder
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.car);

        Bitmap scaled = Bitmap.createScaledBitmap(b, 48, 48, true);
        mLocationOverlay.setPersonIcon(scaled);

        mMapView.getOverlays().add(mLocationOverlay);
        mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), mMapView);
        mCompassOverlay.enableCompass();
        mMapView.getOverlays().add(mCompassOverlay);

        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mMapView.setMultiTouchControls(true);

        //scales tiles to the current screen's DPI, helps with readability of labels
        mMapView.setTilesScaledToDpi(true);

        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        drivenPath = new Polyline();


        drivenPath.getOutlinePaint().setColor(Color.GREEN);
        drivenPath.getOutlinePaint().setStrokeWidth(3);
        mMapView.getOverlays().add(drivenPath);

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

                        mCompassOverlay.setCompassCenter(rect.width() / 3 - 30, rect.height() / 3 - 30);

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
                            RoutingFragment.this.drawDrivenRoute(mMyLocationProvider);
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

    public void onPause() {
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


    void drawDrivenRoute(MyLocationProvider mMyLocationProvider) {
        Location lastKnownLocation = mMyLocationProvider.getLastKnownLocation();
        if (lastKnownLocation != null) {
            drivenPath.addPoint(new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
            if (!followPosition) {
                // Set Zoom on position found
                IMapController mapController = mMapView.getController();
                mapController.setZoom(14.0);
                mapController.setCenter(mLocationOverlay.getMyLocation());
                followPosition = true;

            }

        }
    }

    void updateStatus() {
        MainActivity ma;
        RtkNaviService rtks;
        int serverStatus;

        // XXX
        ma = (MainActivity) getActivity();

        if (ma == null) return;

        rtks = ma.getRtkService();
        if (rtks == null) {
            serverStatus = RtkServerStreamStatus.STATE_CLOSE;
            mStreamStatus.clear();
        } else {
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
                pos = MainActivity.getDemoModeLocation().getPosition();
                if (pos == null)
                    return;
            } else {
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
                } else {
                    // XXX
                    // if (DBG) Log.v(TAG, "onLocationChanged() skipped while animating");
                }
            }
        }

        public void setStatus(RtkControlResult status, boolean notifyConsumer) {
            setSolution(status.getSolution(), notifyConsumer);
        }

    }

    ;

}
