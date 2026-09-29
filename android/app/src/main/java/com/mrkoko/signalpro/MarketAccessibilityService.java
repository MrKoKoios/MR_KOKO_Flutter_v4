package com.mrkoko.signalpro;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class MarketAccessibilityService extends AccessibilityService {

    private static final String TAG = "MRKOKOAccessibility";
    private static MarketAccessibilityService instance;
    private boolean isScrolling = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int screenWidth  = 1080;
    private int screenHeight = 2400;
    private int scrollStep   = 0;
    private static final int MAX_SCROLL_STEPS = 20;
    private Runnable scrollRunnable;

    public static MarketAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        screenWidth  = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        Log.d(TAG, "Accessibility connected. Screen: " + screenWidth + "x" + screenHeight);
        sendBroadcast(new Intent("com.mrkoko.ACCESSIBILITY_CONNECTED"));
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        isScrolling = false;
    }

    public void startChartScan() {
        if (isScrolling) return;
        isScrolling = true;
        scrollStep  = 0;
        Log.d(TAG, "Chart scan started");

        scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isScrolling || scrollStep >= MAX_SCROLL_STEPS) {
                    isScrolling = false;
                    Intent done = new Intent("com.mrkoko.SCROLL_DONE");
                    done.putExtra("steps", scrollStep);
                    sendBroadcast(done);
                    Log.d(TAG, "Scan complete after " + scrollStep + " steps");
                    return;
                }
                performChartSwipe();
                scrollStep++;
                handler.postDelayed(this, 800);
            }
        };
        handler.post(scrollRunnable);
    }

    public void stopChartScan() {
        isScrolling = false;
        if (scrollRunnable != null) {
            handler.removeCallbacks(scrollRunnable);
        }
        Log.d(TAG, "Scan stopped at step " + scrollStep);
    }

    private void performChartSwipe() {
        // ডান থেকে বামে — পুরোনো candle দেখার জন্য
        int y      = screenHeight / 2;
        int startX = (int)(screenWidth * 0.85f);
        int endX   = (int)(screenWidth * 0.15f);

        Path path = new Path();
        path.moveTo(startX, y);
        path.lineTo(endX, y);

        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(path, 0, 400))
            .build();

        dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription g) {
                Log.d(TAG, "Swipe " + scrollStep + " done");
                Intent i = new Intent("com.mrkoko.SWIPE_DONE");
                i.putExtra("step", scrollStep);
                sendBroadcast(i);
            }
            @Override
            public void onCancelled(GestureDescription g) {
                Log.w(TAG, "Swipe " + scrollStep + " cancelled");
            }
        }, null);
    }

    @Override
    public void onDestroy() {
        instance = null;
        isScrolling = false;
        super.onDestroy();
    }
}
