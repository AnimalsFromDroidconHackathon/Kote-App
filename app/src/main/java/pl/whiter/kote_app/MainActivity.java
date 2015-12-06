package pl.whiter.kote_app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Checker checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checker = new Checker();
        checker.startChecking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checker.stopChecking();
    }
}

