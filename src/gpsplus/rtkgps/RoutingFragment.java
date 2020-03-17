package gpsplus.rtkgps;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import gpsplus.rtkgps.routing.StatusOverlay;
import gpsplus.rtkgps.view.GTimeView;

public class RoutingFragment extends Fragment {
    private MapView mMapView;
    @BindView(R.id.map_container) ViewGroup mMapViewContainer;
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
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx),mMapView);
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

        CompassOverlay mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), mMapView);
        mCompassOverlay.enableCompass();
        mMapView.getOverlays().add(mCompassOverlay);


        mMapView.setMultiTouchControls(true);

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
        mMapView.getOverlays().add(new StatusOverlay());

        mMapViewContainer.addView(mMapView, 0);

        return v;
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mMapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mMapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}
