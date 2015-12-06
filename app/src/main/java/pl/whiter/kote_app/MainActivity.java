package pl.whiter.kote_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.whiter.kote_app.location.LocationManager;
import pl.whiter.kote_app.model.Kote;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements LocationManager.Callback, Checker.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static final String PERMISSION_PHONE = Manifest.permission.READ_PHONE_STATE;
    public static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int REQUEST_CODE_PHONE = 1;
    public static final int REQUEST_CODE_LOCATION = 2;

    private Checker checker;

    private ImageView qrCodeImage;
    private CoordinatorLayout rootView;
    private TextView gpsCoordinates;
    private LocationManager locationManager;

    private List<Action0> locationSuccessList = new ArrayList<>();
    private boolean requestingLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button getQrCodeButton = (Button) findViewById(R.id.get_qr_code);
        getQrCodeButton.setOnClickListener(new QrCodeButtonClickListener());

        qrCodeImage = (ImageView) findViewById(R.id.qr_code);
        rootView = (CoordinatorLayout) findViewById(R.id.root_view);
        gpsCoordinates = (TextView) findViewById(R.id.gps_coordinates);


        checker = new Checker(this);
        checker.startChecking();
        locationManager = new LocationManager(this);
        if (checkPlayServices()) {
            locationManager.create(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkLocationPermission(stopAction, false);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (REQUEST_CODE_PHONE == requestCode) {
            onPhoneRequestResult(permissions, grantResults);
        } else if (REQUEST_CODE_LOCATION == requestCode) {
            onLocationRequestResult(permissions, grantResults);
        }
    }

    private void onLocationRequestResult(String[] permissions, int[] grantResults) {
        requestingLocation = false;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onLocationRequestResult: success list size= " + locationSuccessList.size());
            for (Action0 action : locationSuccessList) {
                action.call();
            }
        } else {
            Snackbar.make(rootView, "Not able to find your cat!", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void onPhoneRequestResult(String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            generateQrCode();
        } else {
            Snackbar.make(rootView, "Not able to generate qr code", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checker.stopChecking();
    }

    @Override
    public void onKoteLost() {
        checkLocationPermission(scanAction, true);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        gpsCoordinates.setText(String.format("%f %f", latitude, longitude));
        Firebase firebase = KoteApp.firebase.child("animals").child(KoteApp.uuid);

        Map<String, Object> map = new HashMap<>();
        map.put("lat",latitude);
        map.put("lnt", longitude);

        firebase.updateChildren(map);
    }

    private void checkLocationPermission(Action0 actionSuccess, boolean request) {
        int locationPermission = ContextCompat.checkSelfPermission(this, PERMISSION_LOCATION);
        if (PackageManager.PERMISSION_GRANTED == locationPermission) {
            actionSuccess.call();
        } else if(request){
            if(requestingLocation) {
                locationSuccessList.add(actionSuccess);
                return;
            }
            requestingLocation = true;
            requestLocationPermission();
        }
    }

    private class QrCodeButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int phonePermission = ContextCompat.checkSelfPermission(MainActivity.this, PERMISSION_PHONE);
            if (PackageManager.PERMISSION_GRANTED == phonePermission) {
                generateQrCode();
            } else {
                requestPhonePermission();
            }
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_LOCATION)) {
            Snackbar.make(rootView, "Grant location for meow", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Gimme", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestLocation();
                        }
                    }).show();
        } else {
            requestLocation();
        }
    }

    private void requestPhonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_PHONE)) {
            Snackbar.make(rootView, "Grant read phone, please :)", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Gimme", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPhone();
                        }
                    }).show();
        } else {
            requestPhone();
        }
    }

    private void requestLocation() {
        ActivityCompat.requestPermissions(this,
                new String[]{PERMISSION_LOCATION},
                REQUEST_CODE_LOCATION);
    }

    private void requestPhone() {
        ActivityCompat.requestPermissions(this,
                new String[]{PERMISSION_PHONE},
                REQUEST_CODE_PHONE);
    }

    private void generateQrCode() {
        BitmapHelper.createQrCode(this)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        qrCodeImage.setImageBitmap(bitmap);
                    }
                });
    }

    private Action0 scanAction = new Action0() {
        @Override
        public void call() {
            locationManager.getLocation();
        }
    };

    private Action0 stopAction = new Action0() {
        @Override
        public void call() {
            locationManager.stop();
        }
    };

}

