package com.taxiscan.app;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;

/** Extracts only a fare and kilometre values from visible ride-offer text. */
public final class OfferParser {
    private static final Pattern FARE = Pattern.compile("(?iu)(?:₴|грн\\.?|uah)\\s*([0-9][0-9\\s.,]*)|([0-9][0-9\\s.,]*)\\s*(?:₴|грн\\.?|uah)");
    private static final Pattern KM = Pattern.compile("(?iu)([0-9]{1,3}(?:[.,][0-9]{1,2})?)\\s*(?:км|km)(?=$|\\s|[,.])");
    private OfferParser() { }

    public static String summarize(CharSequence content) {
        if (content == null) return null;
        String text = content.toString();
        Matcher fm = FARE.matcher(text);
        if (!fm.find()) return null;
        String fareText = fm.group(1) != null ? fm.group(1) : fm.group(2);
        Double fare = number(fareText);
        if (fare == null || fare <= 0) return null;
        Matcher km = KM.matcher(text);
        ArrayList<Double> distances = new ArrayList<>();
        while (km.find()) {
            Double d = number(km.group(1));
            if (d != null && d > 0 && d < 500) distances.add(d);
        }
        if (distances.isEmpty()) return null;
        double total = 0;
        for (double d : distances) total += d;
        return String.format(new Locale("uk", "UA"), "Замовлення · ₴ %.0f · %.1f км", fare, total);
    }

    /** Adds a local pass/attention hint; it never accepts or rejects the ride. */
    public static String withFilter(Context context, String summary) {
        if (summary == null) return null;
        Matcher m = Pattern.compile("₴\\s*([0-9]+(?:[.,][0-9]+)?).*?([0-9]+(?:[.,][0-9]+)?)\\s*км").matcher(summary);
        if (!m.find()) return summary;
        Double fare=number(m.group(1)), km=number(m.group(2));
        if(fare==null||km==null||km<=0)return summary;
        android.content.SharedPreferences p=context.getSharedPreferences("taxiscan_features",Context.MODE_PRIVATE);
        double minFare=parse(p.getString("filter_min_fare","80"),80), minRate=parse(p.getString("filter_min_rate","8"),8), maxKm=parse(p.getString("filter_max_km","40"),40);
        boolean passes=fare>=minFare && fare/km>=minRate && km<=maxKm;
        return summary+(passes?" · ФІЛЬТР ✓":" · ПЕРЕВІР УМОВИ");
    }

    private static double parse(String s,double fallback){try{return Double.parseDouble(s.trim().replace(',','.'));}catch(Exception ignored){return fallback;}}

    private static Double number(String raw) {
        try {
            String s = raw.replaceAll("\\s", "").replace(',', '.');
            return Double.parseDouble(s);
        } catch (Exception ignored) { return null; }
    }
}
