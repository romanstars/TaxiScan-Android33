package com.taxiscan.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

/** Non-interactive overlay; it never captures taps or clicks on the rideshare app. */
final class OfferOverlay {
    private static WindowManager manager;
    private static TextView view;
    private OfferOverlay() { }

    static synchronized void show(Context context, String message) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) return;
        if (manager == null) manager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (manager == null) return;
        if (view == null) {
            TextView chip = new TextView(context.getApplicationContext());
            android.content.SharedPreferences prefs=context.getSharedPreferences("taxiscan_features",Context.MODE_PRIVATE);
            chip.setTextColor(Color.WHITE); chip.setTextSize(prefs.getInt("overlay_text_size",15)); chip.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            chip.setPadding(dp(context,16),dp(context,12),dp(context,16),dp(context,12));
            GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.argb(prefs.getInt("overlay_opacity",235),18,48,35)); bg.setCornerRadius(dp(context,18)); bg.setStroke(dp(context,2),Color.rgb(76,190,97)); chip.setBackground(bg);
            int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL; lp.y = dp(context,prefs.getInt("overlay_y",70));
            try { manager.addView(chip,lp); view = chip; } catch (RuntimeException ignored) { return; }
        }
        android.content.SharedPreferences prefs=context.getSharedPreferences("taxiscan_features",Context.MODE_PRIVATE);
        view.setTextSize(prefs.getInt("overlay_text_size",15));
        view.setText(message);
    }

    static synchronized void hide() {
        if (manager != null && view != null) {
            try { manager.removeView(view); } catch (RuntimeException ignored) { }
            view = null;
        }
    }
    private static int dp(Context c,int value){return (int)(value*c.getResources().getDisplayMetrics().density+0.5f);}
}
