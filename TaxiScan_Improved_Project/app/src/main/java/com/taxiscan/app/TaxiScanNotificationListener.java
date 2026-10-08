package com.taxiscan.app;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;
import java.util.Locale;

/** Optional, user-enabled notification parsing limited to supported taxi apps. */
public class TaxiScanNotificationListener extends NotificationListenerService {
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if(!getSharedPreferences("taxiscan_features",MODE_PRIVATE).getBoolean("scanning_enabled",true))return;
        if (sbn == null || sbn.getPackageName() == null) return;
        String pkg = sbn.getPackageName().toLowerCase(Locale.ROOT);
        if (!pkg.equals("ee.mtakso.driver") && !pkg.equals("ua.com.uklon.uklondriver")) return;
        Notification n = sbn.getNotification();
        if (n == null || n.extras == null) return;
        StringBuilder text = new StringBuilder();
        append(text,n.extras.getCharSequence(Notification.EXTRA_TITLE));
        append(text,n.extras.getCharSequence(Notification.EXTRA_TEXT));
        append(text,n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
        append(text,n.extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
        String summary = OfferParser.summarize(text);
        if (summary != null) {summary=OfferParser.withFilter(this,summary);OfferFeedback.onOffer(this,summary);OfferOverlay.show(this, summary);}
    }
    private void append(StringBuilder to, CharSequence value) { if (value != null) to.append(value).append(' '); }
}
