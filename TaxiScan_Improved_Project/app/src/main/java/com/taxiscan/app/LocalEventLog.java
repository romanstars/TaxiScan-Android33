package com.taxiscan.app;

import android.content.Context;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Small, device-local diagnostic log. It never records offer text. */
final class LocalEventLog {
    private static final String PREFS = "taxiscan_local_log";
    private static final String KEY = "entries";
    private LocalEventLog() { }

    static synchronized void add(Context context, String message) {
        try {
            JSONArray entries = new JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]"));
            entries.put(0, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()) + " · " + message);
            while (entries.length() > 50) entries.remove(entries.length() - 1);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, entries.toString()).apply();
        } catch (Exception ignored) { }
    }

    static JSONArray get(Context context) {
        try { return new JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]")); }
        catch (Exception ignored) { return new JSONArray(); }
    }

    static void clear(Context context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply(); }
}
