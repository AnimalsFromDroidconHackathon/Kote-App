package pl.whiter.kote_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.telephony.TelephonyManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import rx.Observable;
import rx.functions.Func0;

/**
 * Created by whiter
 */
public final class BitmapHelper {

    private static final String URL = "http://kote-web/%s";

    private BitmapHelper(){

    }

    public static Observable<Bitmap> createQrCode(final Context context){
        return Observable.defer(new Func0<Observable<Bitmap>>() {
            @Override
            public Observable<Bitmap> call() {
                QRCodeWriter writer = new QRCodeWriter();
                Bitmap bitmap = null;
                TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
}
