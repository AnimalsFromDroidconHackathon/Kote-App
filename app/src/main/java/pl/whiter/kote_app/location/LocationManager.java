package pl.whiter.kote_app.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public interface Callback {
        void onLocationChanged(Location location);
    }

    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters


    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    private boolean mRequestingLocationUpdates = true;

    private LocationRequest mLocationRequest;

    private Callback callback;

    public LocationManager(Callback callback) {
        this.callback = callback;
    }

    public void create(Context context){
        buildGoogleApiClient(context);
        createLocationRequest();
    }

    public void start(){
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void stop(){
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void resume(){
        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public void pause(){
        stopLocationUpdates();
    }



    protected synchronized void buildGoogleApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.
                requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest,
                        this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.
                removeLocationUpdates(mGoogleApiClient,
                        this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        displayLocation();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    private void displayLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            callback.onLocationChanged(mLastLocation);
        }
    }
}
