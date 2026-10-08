package com.taxiscan.app;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/** Consent-first hub for the system access needed by optional offer scanning. */
public class PermissionSetupActivity extends Activity {
    private static final int ACCESSIBILITY=1, RESET=2, BATTERY=3, OVERLAY=4, NOTIFICATIONS=5, LOCATION=6, LISTENER=7, AUTOSTART=8, APP_SETTINGS=9;
    private final int bg=Color.rgb(15,18,18), card=Color.rgb(18,43,29), green=Color.rgb(84,184,91), blue=Color.rgb(35,153,218), white=Color.WHITE, muted=Color.rgb(185,200,187);
    private final ArrayList<CardRef> cards=new ArrayList<>();
    private LinearLayout page;

    @Override protected void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);build();}
    @Override protected void onResume(){super.onResume();refreshStatuses();}

    private void build(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(bg);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(18),dp(20),dp(18),dp(28));scroll.addView(page);
        TextView heading=text("Підключення TaxiScan",25,white,true);page.addView(heading);
        TextView sub=text("Дозволи для аналізу пропозицій Bolt та Uklon",15,muted,false);page.addView(sub,lp(0,4,0,15));
        LinearLayout note=panel(false);page.addView(note,lp(0,0,0,14));
        note.addView(text("ПРИВАТНІСТЬ І КЕРУВАННЯ",12,green,true));
        note.addView(text("Після вашого дозволу TaxiScan читає видимий текст лише у Bolt/Uklon, щоб показати суму й кілометраж поверх програми. Дані обробляються на телефоні. TaxiScan не натискає кнопки та не приймає замовлення.",14,white,false),lp(0,6,0,0));
        TextView label=text("Системні дозволи",18,white,true);page.addView(label,lp(2,0,0,7));
        addCard(ACCESSIBILITY,"Дозвіл Accessibility","Читання видимого тексту пропозиції у Bolt/Uklon",green,()->openSettings(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addCard(RESET,"Не скидати дозволи","У налаштуваннях Android відкрий TaxiScan та вимкни «Призупиняти роботу, якщо додаток не використовується»",green,()->openAppDetails());
        addCard(BATTERY,"Енергозбереження","Дозволь роботу без обмежень батареї для стабільного показу картки",green,()->openBatterySettings());
        addCard(OVERLAY,"Показувати поверх інших додатків","Дозвіл потрібен для плаваючого розрахунку",green,()->openOverlaySettings());
        addCard(NOTIFICATIONS,"Дозвіл на сповіщення","Показ статусу сканування TaxiScan",green,()->requestNotifications());
        addCard(LOCATION,"Дозвіл на геолокацію (GPS)","Необов'язково. Ця версія не отримує та не зберігає координати",green,()->requestLocation());
        addCard(LISTENER,"Доступ до сповіщень","Необов'язковий аналіз сповіщень Bolt/Uklon",green,()->openNotificationListenerSettings());
        addCard(AUTOSTART,"Автозапуск у фоні","Для Xiaomi/MIUI увімкни автозапуск вручну в системних налаштуваннях",blue,()->openAutostartSettings());
        addCard(APP_SETTINGS,"Відкрити налаштування програми","Додаткові дозволи та параметри TaxiScan",blue,()->openAppDetails());
        Button done=button("Готово",green,Color.rgb(12,35,19));page.addView(done,lp(0,12,0,0));done.setOnClickListener(v->finish());
        setContentView(scroll);
    }

    private void addCard(int kind,String title,String details,int accent,Runnable action){
        LinearLayout outer=panel(true);outer.setBackground(round(card,dp(14),accent,dp(1)));page.addView(outer,lp(0,0,0,9));
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);outer.addView(row,new LinearLayout.LayoutParams(-1,-2));
        TextView number=text(kind>=1&&kind<=8?String.valueOf(kind):"↗",18,white,true);number.setGravity(Gravity.CENTER);number.setBackground(round(accent,dp(50),Color.TRANSPARENT,0));row.addView(number,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);copy.setPadding(dp(10),0,dp(8),0);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        copy.addView(text(title,16,accent,true));copy.addView(text(details,12,muted,false),lp(0,3,0,0));
        Button open=button("Відкрити",Color.rgb(222,224,224),Color.rgb(25,27,27));row.addView(open,new LinearLayout.LayoutParams(dp(112),dp(48)));
        open.setOnClickListener(v->action.run());cards.add(new CardRef(kind,open));
    }

    private void refreshStatuses(){for(CardRef r:cards){boolean ok=isEnabled(r.kind);r.button.setText(labelFor(r.kind,ok));if(ok)r.button.setTextColor(Color.rgb(28,94,42));}}
    private String labelFor(int kind,boolean ok){switch(kind){case ACCESSIBILITY:return ok?"Увімкнено":"Увімкнути";case RESET:return "Перевірити";case BATTERY:return ok?"Без обмежень":"Батарея";case OVERLAY:return ok?"Увімкнено":"Дозвіл";case NOTIFICATIONS:return ok?"Дозволено":"Дозвіл";case LOCATION:return ok?"Дозволено":"Дозвіл";case LISTENER:return ok?"Увімкнено":"Дозвіл";case AUTOSTART:return "Автозапуск";default:return "Відкрити";}}
    private boolean isEnabled(int kind){switch(kind){case ACCESSIBILITY:return isAccessibilityEnabled();case BATTERY:return isBatteryUnrestricted();case OVERLAY:return Build.VERSION.SDK_INT<23||Settings.canDrawOverlays(this);case NOTIFICATIONS:return Build.VERSION.SDK_INT<33||hasPermission(Manifest.permission.POST_NOTIFICATIONS);case LOCATION:return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)||hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);case LISTENER:return isNotificationListenerEnabled();default:return false;}}

    private boolean isAccessibilityEnabled(){String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);if(enabled==null)return false;String wanted=new ComponentName(this,TaxiScanAccessibilityService.class).flattenToString();for(String item:enabled.split(":"))if(item.equalsIgnoreCase(wanted)||item.equalsIgnoreCase(new ComponentName(this,TaxiScanAccessibilityService.class).flattenToShortString()))return true;return false;}
    private boolean isNotificationListenerEnabled(){String enabled=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");if(enabled==null)return false;String wanted=new ComponentName(this,TaxiScanNotificationListener.class).flattenToString();for(String item:enabled.split(":"))if(item.equalsIgnoreCase(wanted))return true;return false;}
    private boolean hasPermission(String permission){return Build.VERSION.SDK_INT<23||checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED;}
    private boolean isBatteryUnrestricted(){if(Build.VERSION.SDK_INT<23)return true;PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);return pm!=null&&pm.isIgnoringBatteryOptimizations(getPackageName());}

    private void requestNotifications(){if(Build.VERSION.SDK_INT>=33){if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},101);else Toast.makeText(this,"Сповіщення вже дозволені",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"Дозвіл на сповіщення керується в налаштуваннях програми",Toast.LENGTH_SHORT).show();}
    private void requestLocation(){if(Build.VERSION.SDK_INT<23){Toast.makeText(this,"На цій версії Android GPS-дозвіл не потрібен",Toast.LENGTH_LONG).show();return;}if(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)||hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)){Toast.makeText(this,"Дозвіл уже надано; координати не збираються",Toast.LENGTH_LONG).show();return;}requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},102);}
    private void openOverlaySettings(){if(Build.VERSION.SDK_INT>=23)openSettings(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));else Toast.makeText(this,"На цій версії Android перевір дозвіл у налаштуваннях TaxiScan",Toast.LENGTH_LONG).show();}
    private void openBatterySettings(){if(Build.VERSION.SDK_INT>=23)openSettings(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));else openAppDetails();}
    private void openNotificationListenerSettings(){if(Build.VERSION.SDK_INT>=22)openSettings(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));else openAppDetails();}
    private void openAutostartSettings(){
        Intent miui=new Intent();miui.setComponent(new ComponentName("com.miui.securitycenter","com.miui.permcenter.autostart.AutoStartManagementActivity"));
        if(miui.resolveActivity(getPackageManager())!=null){openSettings(miui);Toast.makeText(this,"Увімкни автозапуск для TaxiScan",Toast.LENGTH_LONG).show();}
        else {openAppDetails();Toast.makeText(this,"Якщо є у твоїй прошивці: Програми → TaxiScan → Автозапуск",Toast.LENGTH_LONG).show();}
    }
    private void openAppDetails(){openSettings(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}
    private void openSettings(Intent intent){try{if(intent.resolveActivity(getPackageManager())!=null)startActivity(intent);else openAppDetails();}catch(Exception ex){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){Toast.makeText(this,"Не вдалося відкрити системні налаштування",Toast.LENGTH_SHORT).show();}}}

    private LinearLayout panel(boolean vertical){LinearLayout l=new LinearLayout(this);l.setOrientation(vertical?LinearLayout.VERTICAL:LinearLayout.VERTICAL);l.setPadding(dp(12),dp(12),dp(12),dp(12));l.setBackground(round(card,dp(15),green,dp(1)));return l;}
    private Button button(String title,int color,int textColor){Button b=new Button(this);b.setText(title);b.setTextColor(textColor);b.setTextSize(14);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);b.setPadding(dp(4),0,dp(4),0);b.setBackground(round(color,dp(5),Color.TRANSPARENT,0));return b;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable round(int color,int radius,int stroke,int width){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(width>0)d.setStroke(width,stroke);return d;}
    private LinearLayout.LayoutParams lp(int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){super.onRequestPermissionsResult(requestCode,permissions,results);refreshStatuses();if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)Toast.makeText(this,"Дозвіл надано",Toast.LENGTH_SHORT).show();}
    private static final class CardRef{final int kind;final Button button;CardRef(int k,Button b){kind=k;button=b;}}
}
