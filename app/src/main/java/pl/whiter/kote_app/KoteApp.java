package pl.whiter.kote_app;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.firebase.client.Firebase;


/**
 * Created by whiter
 */
public class KoteApp extends Application {

    public static String uuid;

    public static Firebase firebase;


    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uuid = tManager.getDeviceId();
        firebase = new Firebase("https://kote.firebaseio.com");
    }
}
