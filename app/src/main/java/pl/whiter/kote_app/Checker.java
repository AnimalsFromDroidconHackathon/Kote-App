package pl.whiter.kote_app;


import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import pl.whiter.kote_app.model.Kote;

public class Checker {

    private static final String TAG = Checker.class.getSimpleName();


    public interface Callback {
        void onKoteLost();
    }


    private ValueEventListener listener;
    private Callback callback;

    public Checker(Callback callback) {
        this.callback = callback;
    }

    public void startChecking() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) {
                    return;
                }
                String childPath = KoteApp.uuid;
                DataSnapshot child = dataSnapshot.child("animals").child(childPath);
                Log.d(TAG, "onDataChange: child " + child);
                if (child.exists()) {
                    Log.d(TAG, "onDataChange: exists");
                    final Kote kote = child.getValue(Kote.class);

                    if (kote.lost) {
                        callback.onKoteLost();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        KoteApp.firebase.addValueEventListener(listener);
    }


    public void stopChecking() {

        if (listener != null) {
            KoteApp.firebase.removeEventListener(listener);
        }

    }

}
