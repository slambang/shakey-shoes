package com.slambang.shakeyshoes.oboe;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.slambang.shakeyshoes.R;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class OboeActivity extends Activity {

    private static final long UPDATE_LATENCY_EVERY_MILLIS = 1000;

    private TextView mLatencyText;
    private Timer mLatencyUpdater;

    private boolean mIsPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oboe);
        mLatencyText = findViewById(R.id.latencyText);

        findViewById(R.id.root).setOnClickListener(v -> {
            mIsPlaying = !mIsPlaying;
            NativeApp.setToneOn(mIsPlaying);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NativeApp.create(this);
        setupLatencyUpdater();
    }

    @Override
    protected void onPause() {
        if (mLatencyUpdater != null) mLatencyUpdater.cancel();
        NativeApp.delete();
        mIsPlaying = false;
        super.onPause();
    }

    private void setupLatencyUpdater() {
        //Update the latency every 1s
        TimerTask latencyUpdateTask = new TimerTask() {
            @Override
            public void run() {
                final String latencyStr;
                if (NativeApp.isLatencyDetectionSupported()) {
                    double latency = NativeApp.getCurrentOutputLatencyMillis();
                    if (latency >= 0) {
                        latencyStr = String.format(Locale.getDefault(), "%.2fms", latency);
                    } else {
                        latencyStr = "Unknown";
                    }
                } else {
                    latencyStr = "Only supported on API 26";
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLatencyText.setText("Latency: " + latencyStr);
                    }
                });
            }
        };
        mLatencyUpdater = new Timer();
        mLatencyUpdater.schedule(latencyUpdateTask, 0, UPDATE_LATENCY_EVERY_MILLIS);
    }
}
