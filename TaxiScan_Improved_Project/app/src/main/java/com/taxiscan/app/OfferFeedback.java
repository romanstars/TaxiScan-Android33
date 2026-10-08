package com.taxiscan.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

/** Optional sound alert, throttled so rapid screen events do not ring repeatedly. */
final class OfferFeedback {
    private static long lastToneAt;
    private OfferFeedback() { }

    static synchronized void onOffer(Context context, String summary) {
        long now = System.currentTimeMillis();
        if (now - lastToneAt < 5000) return;
        lastToneAt = now;
        LocalEventLog.add(context, "Знайдено пропозицію");
        if (!context.getSharedPreferences("taxiscan_features", Context.MODE_PRIVATE).getBoolean("sound_enabled", false)) return;
        try {
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70);
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 180);
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(tone::release, 500);
        } catch (RuntimeException ignored) { }
    }
}
