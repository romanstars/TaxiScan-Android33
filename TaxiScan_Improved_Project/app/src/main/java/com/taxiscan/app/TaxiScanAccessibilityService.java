package com.taxiscan.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

/** Reads visible text only from Bolt/Uklon windows; does not perform gestures or clicks. */
public class TaxiScanAccessibilityService extends AccessibilityService {
    @Override public void onServiceConnected() {
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            info.notificationTimeout = 180;
            info.packageNames = new String[]{"ee.mtakso.driver", "ua.com.uklon.uklondriver"};
            setServiceInfo(info);
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) { OfferOverlay.hide(); return; }
        String pkg = event.getPackageName().toString().toLowerCase(Locale.ROOT);
        if (!pkg.equals("ee.mtakso.driver") && !pkg.equals("ua.com.uklon.uklondriver")) { OfferOverlay.hide(); return; }
        StringBuilder visible = new StringBuilder();
        for (CharSequence item : event.getText()) if (item != null) visible.append(item).append(' ');
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) { collectVisibleText(root, visible, 0); root.recycle(); }
        String summary = OfferParser.summarize(visible);
        if (summary != null) OfferOverlay.show(this, summary); else OfferOverlay.hide();
    }

    private void collectVisibleText(AccessibilityNodeInfo node, StringBuilder out, int depth) {
        if (node == null || depth > 18 || out.length() > 12000) return;
        CharSequence text = node.getText();
        if (text != null && text.length() > 0) out.append(text).append(' ');
        for (int i=0;i<node.getChildCount();i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) { collectVisibleText(child,out,depth+1); child.recycle(); }
        }
    }

    @Override public void onInterrupt() { OfferOverlay.hide(); }
    @Override public boolean onUnbind(android.content.Intent intent) { OfferOverlay.hide(); return super.onUnbind(intent); }
}
