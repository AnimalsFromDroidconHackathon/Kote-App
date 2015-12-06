package pl.whiter.kote_app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import pl.whiter.kote_app.location.LocationManager;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements LocationManager.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private static final String URL = "http://kote-web/%s";
    public static final String PERMISSION = Manifest.permission.READ_PHONE_STATE;
    public static final int REQUEST_CODE = 1;

    private Checker checker;

    private ImageView qrCodeImage;
    private CoordinatorLayout rootView;
    private TextView gpsCoordinates;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button getQrCodeButton = (Button) findViewById(R.id.get_qr_code);
        getQrCodeButton.setOnClickListener(new QrCodeButtonClickListener());
        qrCodeImage = (ImageView) findViewById(R.id.qr_code);
        rootView = (CoordinatorLayout) findViewById(R.id.root_view);
        gpsCoordinates = (TextView) findViewById(R.id.gps_coordinates);
        checker = new Checker();
        checker.startChecking();
        locationManager = new LocationManager(this);
        if (checkPlayServices()) {
            locationManager.create(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationManager.start();
    }


    @Override
    protected void onStop() {
        super.onStop();
        locationManager.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.pause();
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
        if (requestCode == REQUEST_CODE) {
            onPhoneRequestResult(permissions, grantResults);
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
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        gpsCoordinates.setText(String.format("%f %f", latitude, longitude));
    }

    private class QrCodeButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int phonePermission = ContextCompat.checkSelfPermission(MainActivity.this, PERMISSION);
            if (PackageManager.PERMISSION_GRANTED == phonePermission) {
                generateQrCode();
            } else {
                requestPhonePermission();
            }
        }
    }

    private void requestPhonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION)) {
            Snackbar.make(rootView, "Grant read phone, please :)", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Gimme", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            request();
                        }
                    }).show();
        } else {
            request();
        }
    }

    private void request() {
        ActivityCompat.requestPermissions(this,
                new String[]{PERMISSION},
                REQUEST_CODE);
    }

    private void generateQrCode() {
        getQrCodeBitmap
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


    private Observable<Bitmap> getQrCodeBitmap = Observable.defer(new Func0<Observable<Bitmap>>() {
        @Override
        public Observable<Bitmap> call() {
            QRCodeWriter writer = new QRCodeWriter();
            Bitmap bitmap = null;
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String uuid = tManager.getDeviceId();

            String content = String.format(URL, uuid);
            BitMatrix bitMatrix;
            try {
                bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }

            return Observable.just(bitmap);
        }
    });
}

