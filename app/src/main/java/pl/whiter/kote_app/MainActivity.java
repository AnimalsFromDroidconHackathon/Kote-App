package pl.whiter.kote_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String URL = "http://kote-web/%s";

    private Checker checker;

    private ImageView qrCodeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button getQrCodeButton = (Button) findViewById(R.id.get_qr_code);
        getQrCodeButton.setOnClickListener(new QrCodeButtonClickListener());
        qrCodeImage = (ImageView) findViewById(R.id.qr_code);
        checker = new Checker();
        checker.startChecking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checker.stopChecking();
    }

    private class QrCodeButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            generateQrCode();
        }
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

