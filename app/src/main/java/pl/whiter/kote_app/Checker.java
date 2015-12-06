package pl.whiter.kote_app;


import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pl.whiter.kote_app.api.AnimalApi;
import pl.whiter.kote_app.model.Kote;
import rx.Subscriber;

public class Checker {

    private static final String TAG = Checker.class.getSimpleName();

    private static final int BASE_CHECK_TIME = 30;
    private static final TimeUnit BASE_CHECK_TIME_UNIT = TimeUnit.SECONDS;

    private ScheduledExecutorService scheduledExecutorService;


    public Checker() {
    }

    public void startChecking() {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleWithFixedDelay(checkRunnable, 0, BASE_CHECK_TIME, BASE_CHECK_TIME_UNIT);
        }
    }


    public void stopChecking() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"checking");
            AnimalApi.getService().getKoteAnimal()
                    .subscribe(new Subscriber<Kote>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Kote kote) {

                        }
                    });
        }
    };
}
