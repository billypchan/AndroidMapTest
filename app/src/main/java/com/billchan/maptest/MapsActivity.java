package com.billchan.maptest;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;

import android.location.LocationListener;
import android.view.View;
import android.widget.Button;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    LatLng myLatLng;
    LatLng shopLatLng;
    Boolean isDirectionDrawn = false;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button showDirectionsButton = (Button) findViewById(R.id.resetRouteButton);
        showDirectionsButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                new LongOperation().execute("");
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        double latitude = 48.144744;
        double longitude = 11.560171;
        shopLatLng = new LatLng(latitude, longitude);

        mMap.addMarker(new MarkerOptions()
                        .position(shopLatLng)
                        .title("Lou Lan Cha")
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(shopLatLng));

        mMap.setMyLocationEnabled(true);

        moveToCurrentLocation(shopLatLng);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 1, this);

        try {

            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
        catch (Exception e)
        {
            ///possible error:
            /// null location
        }

        new LongOperation().execute("");
    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }



    private void zoomToPoints() {
        try {


            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            //        for (Marker marker : markers) {
            builder.include(myLatLng);
            builder.include(shopLatLng);
            //        }
            LatLngBounds bounds = builder.build();

            int padding = 50; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            mMap.animateCamera(cu);
        }
        catch (Exception e)
        {
            ///possible error:
            /// java.lang.NullPointerException: Attempt to invoke interface method 'org.w3c.dom.NodeList org.w3c.dom.Document.getElementsByTagName(java.lang.String)' on a null object reference
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        myLatLng = new LatLng(latitude, longitude);

        zoomToPoints();

        if(!isDirectionDrawn) {

            new LongOperation().execute("");
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(!isDirectionDrawn) {
            //            zoomToPoints();
            new LongOperation().execute("");
        }
    }

    private class LongOperation extends AsyncTask<String, Void, PolylineOptions> {

        private PolylineOptions getDirection() {
            try {
                GMapV2Direction md = new GMapV2Direction();

                Document doc = md.getDocument(myLatLng, shopLatLng,
                        GMapV2Direction.MODE_WALKING);

                ArrayList<LatLng> directionPoint = md.getDirection(doc);
                PolylineOptions rectLine = new PolylineOptions().width(9).color(
                        Color.GREEN);

                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                isDirectionDrawn = true;

                return rectLine;
            }
            catch (Exception e)
            {
                ///possible error:
                ///java.lang.IllegalStateException: Error using newLatLngBounds(LatLngBounds, int): Map size can't be 0. Most likely, layout has not yet occured for the map view.  Either wait until layout has occurred or use newLatLngBounds(LatLngBounds, int, int, int) which allows you to specify the map's dimensions.
                return null;
            }

        }

        @Override
        protected PolylineOptions doInBackground(String... params) {
            PolylineOptions polylineOptions = null;
            try {
                polylineOptions = getDirection();
            } catch (Exception e) {
                Thread.interrupted();
            }
            return polylineOptions;
        }

        @Override
        protected void onPostExecute(PolylineOptions result) {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you

            mMap.clear();///TODO: clean the path only.

            mMap.addMarker(new MarkerOptions()
                            .position(shopLatLng)
                            .title("Lou Lan Cha")
            );

            mMap.addPolyline(result);
            zoomToPoints();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
