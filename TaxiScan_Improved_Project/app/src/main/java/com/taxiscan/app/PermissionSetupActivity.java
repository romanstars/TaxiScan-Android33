package com.taxiscan.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.CountDownTimer;
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
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

/** Consent-first hub for the system access needed by optional offer scanning. */
public class PermissionSetupActivity extends Activity {
    private static final int ACCESSIBILITY=1, RESET=2, BATTERY=3, OVERLAY=4, NOTIFICATIONS=5, LOCATION=6, LISTENER=7, AUTOSTART=8, APP_SETTINGS=9;
    private static final int EXPORT_BACKUP=201, IMPORT_BACKUP=202;
    private final int bg=Color.rgb(9,11,12), card=Color.rgb(20,23,25), green=Color.rgb(0,211,133), blue=Color.rgb(32,177,230), white=Color.rgb(245,247,248), muted=Color.rgb(157,164,170);
    private final ArrayList<CardRef> cards=new ArrayList<>();
    private LinearLayout page;
    private CountDownTimer activeTimer;

    @Override protected void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);getWindow().getDecorView().setSystemUiVisibility(0);build();}
    @Override protected void onResume(){super.onResume();refreshStatuses();}

    private void build(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(bg);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(18),dp(20),dp(18),dp(28));scroll.addView(page);
        TextView heading=text("Підключення TaxiAnalytic",25,white,true);page.addView(heading);
        TextView sub=text("Дозволи для аналізу пропозицій Bolt та Uklon",15,muted,false);page.addView(sub,lp(0,4,0,15));
        LinearLayout note=panel(false);page.addView(note,lp(0,0,0,14));
        note.addView(text("ПРИВАТНІСТЬ І КЕРУВАННЯ",12,green,true));
        note.addView(text("Після вашого дозволу TaxiAnalytic читає видимий текст лише у Bolt/Uklon, щоб показати суму й кілометраж поверх програми. Дані обробляються на телефоні. TaxiAnalytic не натискає кнопки та не приймає замовлення.",14,white,false),lp(0,6,0,0));
        TextView label=text("Системні дозволи",18,white,true);page.addView(label,lp(2,0,0,7));
        addCard(ACCESSIBILITY,"Дозвіл Accessibility","Читання видимого тексту пропозиції у Bolt/Uklon",green,()->openSettings(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addCard(RESET,"Не скидати дозволи","У налаштуваннях Android відкрий TaxiAnalytic та вимкни «Призупиняти роботу, якщо додаток не використовується»",green,()->openAppDetails());
        addCard(BATTERY,"Енергозбереження","Дозволь роботу без обмежень батареї для стабільного показу картки",green,()->openBatterySettings());
        addCard(OVERLAY,"Показувати поверх інших додатків","Дозвіл потрібен для плаваючого розрахунку",green,()->openOverlaySettings());
        addCard(NOTIFICATIONS,"Дозвіл на сповіщення","Показ статусу сканування TaxiAnalytic",green,()->requestNotifications());
        addCard(LOCATION,"Дозвіл на геолокацію (GPS)","Необов'язково. Ця версія не отримує та не зберігає координати",green,()->requestLocation());
        addCard(LISTENER,"Доступ до сповіщень","Необов'язковий аналіз сповіщень Bolt/Uklon",green,()->openNotificationListenerSettings());
        addCard(AUTOSTART,"Автозапуск у фоні","Для Xiaomi/MIUI увімкни автозапуск вручну в системних налаштуваннях",blue,()->openAutostartSettings());
        addCard(APP_SETTINGS,"Відкрити налаштування програми","Додаткові дозволи та параметри TaxiAnalytic",blue,()->openAppDetails());
        TextView toolsLabel=text("Функції TaxiAnalytic",18,white,true);page.addView(toolsLabel,lp(2,10,0,7));
        addCard(10,"Робота з Bolt / Uklon","Увімкнути або призупинити аналіз пропозицій",green,()->toggleScanning());
        addCard(11,"Фільтр пропозицій","Мінімальна сума, ціна за км та гранична відстань",green,()->editOfferFilter());
        addCard(12,"Ціна кілометра авто","Витрата пального, його ціна та амортизація",blue,()->editVehicleCosts());
        addCard(13,"Налаштування звуку","Сигнал при появі нової пропозиції",green,()->toggleSound());
        addCard(14,"Інформація та статистика","Локальна статистика та швидкі дії",blue,()->showInfo());
        addCard(15,"Налаштування плаваючого вікна","Розмір тексту, прозорість і вертикальне розташування",blue,()->editOverlay());
        addCard(16,"Таймери поїздки","Прийняття, зустріч, очікування та загальний час",blue,()->showTimers());
        addCard(17,"Резервна копія налаштувань","Експорт або відновлення на телефоні",blue,()->showBackup());
        addCard(18,"Оновлення програми","Відкрити сторінку проєкту та перевірити релізи",blue,()->openUpdates());
        addCard(19,"AI Assistant — прогноз часу","Розрахунок часу з ручним запасом на затори",green,()->showTripForecast());
        addCard(20,"Запустити Bolt","Відкрити застосунок водія, якщо він встановлений",green,()->launchBolt());
        addCard(21,"Налаштування плаваючих кнопок","Керування розміром, позицією та прозорістю картки",blue,()->editOverlay());
        addCard(22,"Логи програми","Локальні службові події без тексту замовлень",blue,()->showLogs());
        addCard(23,"Авто кліки","Сценарії збережені як вимкнена опція; жести не виконуються",blue,()->showNoAutomation("Авто кліки вимкнені. TaxiAnalytic не виконує натискань або жестів у Bolt/Uklon."));
        addCard(24,"Bolt автоприйом попередніх","Підказки доступні, автоматичне підтвердження вимкнене",green,()->showNoAutomation("Автоприйняття замовлень вимкнене. Перевір пропозицію і підтверди її вручну в Bolt."));
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
    private String labelFor(int kind,boolean ok){switch(kind){case ACCESSIBILITY:return ok?"Увімкнено":"Увімкнути";case RESET:return "Перевірити";case BATTERY:return ok?"Без обмежень":"Батарея";case OVERLAY:return ok?"Увімкнено":"Дозвіл";case NOTIFICATIONS:return ok?"Дозволено":"Дозвіл";case LOCATION:return ok?"Дозволено":"Дозвіл";case LISTENER:return ok?"Увімкнено":"Дозвіл";case AUTOSTART:return "Автозапуск";case 10:return ok?"Пауза":"Увімкнути";case 13:return features().getBoolean("sound_enabled",false)?"Звук увімкнено":"Звук вимкнено";default:return "Відкрити";}}
    private boolean isEnabled(int kind){switch(kind){case ACCESSIBILITY:return isAccessibilityEnabled();case BATTERY:return isBatteryUnrestricted();case OVERLAY:return Build.VERSION.SDK_INT<23||Settings.canDrawOverlays(this);case NOTIFICATIONS:return Build.VERSION.SDK_INT<33||hasPermission(Manifest.permission.POST_NOTIFICATIONS);case LOCATION:return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)||hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);case LISTENER:return isNotificationListenerEnabled();case 10:return getSharedPreferences("taxiscan_features",MODE_PRIVATE).getBoolean("scanning_enabled",true);default:return false;}}

    private boolean isAccessibilityEnabled(){String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);if(enabled==null)return false;String wanted=new ComponentName(this,TaxiScanAccessibilityService.class).flattenToString();for(String item:enabled.split(":"))if(item.equalsIgnoreCase(wanted)||item.equalsIgnoreCase(new ComponentName(this,TaxiScanAccessibilityService.class).flattenToShortString()))return true;return false;}
    private boolean isNotificationListenerEnabled(){String enabled=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");if(enabled==null)return false;String wanted=new ComponentName(this,TaxiScanNotificationListener.class).flattenToString();for(String item:enabled.split(":"))if(item.equalsIgnoreCase(wanted))return true;return false;}
    private boolean hasPermission(String permission){return Build.VERSION.SDK_INT<23||checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED;}
    private boolean isBatteryUnrestricted(){if(Build.VERSION.SDK_INT<23)return true;PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);return pm!=null&&pm.isIgnoringBatteryOptimizations(getPackageName());}

    private void requestNotifications(){if(Build.VERSION.SDK_INT>=33){if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},101);else Toast.makeText(this,"Сповіщення вже дозволені",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"Дозвіл на сповіщення керується в налаштуваннях програми",Toast.LENGTH_SHORT).show();}
    private void requestLocation(){if(Build.VERSION.SDK_INT<23){Toast.makeText(this,"На цій версії Android GPS-дозвіл не потрібен",Toast.LENGTH_LONG).show();return;}if(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)||hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)){Toast.makeText(this,"Дозвіл уже надано; координати не збираються",Toast.LENGTH_LONG).show();return;}requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},102);}
    private void openOverlaySettings(){if(Build.VERSION.SDK_INT>=23)openSettings(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));else Toast.makeText(this,"На цій версії Android перевір дозвіл у налаштуваннях TaxiAnalytic",Toast.LENGTH_LONG).show();}
    private void openBatterySettings(){if(Build.VERSION.SDK_INT>=23)openSettings(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));else openAppDetails();}
    private void openNotificationListenerSettings(){if(Build.VERSION.SDK_INT>=22)openSettings(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));else openAppDetails();}
    private void openAutostartSettings(){
        Intent miui=new Intent();miui.setComponent(new ComponentName("com.miui.securitycenter","com.miui.permcenter.autostart.AutoStartManagementActivity"));
        if(miui.resolveActivity(getPackageManager())!=null){openSettings(miui);Toast.makeText(this,"Увімкни автозапуск для TaxiAnalytic",Toast.LENGTH_LONG).show();}
        else {openAppDetails();Toast.makeText(this,"Якщо є у твоїй прошивці: Програми → TaxiAnalytic → Автозапуск",Toast.LENGTH_LONG).show();}
    }
    private void openAppDetails(){openSettings(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}
    private void openSettings(Intent intent){try{if(intent.resolveActivity(getPackageManager())!=null)startActivity(intent);else openAppDetails();}catch(Exception ex){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){Toast.makeText(this,"Не вдалося відкрити системні налаштування",Toast.LENGTH_SHORT).show();}}}

    private android.content.SharedPreferences features(){return getSharedPreferences("taxiscan_features",MODE_PRIVATE);}
    private void toggleScanning(){boolean next=!features().getBoolean("scanning_enabled",true);features().edit().putBoolean("scanning_enabled",next).apply();if(!next)OfferOverlay.hide();LocalEventLog.add(this,next?"Аналіз увімкнено":"Аналіз призупинено");refreshStatuses();Toast.makeText(this,next?"Аналіз пропозицій увімкнено":"Аналіз призупинено",Toast.LENGTH_SHORT).show();}
    private EditText field(String hint,String value){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setText(value);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);return e;}
    private LinearLayout form(){LinearLayout l=new LinearLayout(this);l.setPadding(dp(20),dp(4),dp(20),0);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private void editOfferFilter(){
        android.content.SharedPreferences p=features();LinearLayout f=form();EditText fare=field("Мінімальна сума, ₴",p.getString("filter_min_fare","80"));EditText rate=field("Мінімальна ціна, ₴/км",p.getString("filter_min_rate","8"));EditText distance=field("Максимальна відстань, км",p.getString("filter_max_km","40"));
        f.addView(fare);f.addView(rate);f.addView(distance);new AlertDialog.Builder(this).setTitle("Фільтр пропозицій").setMessage("TaxiAnalytic позначає відповідність умовам. Рішення та прийняття залишаються за водієм.").setView(f).setNegativeButton("Скасувати",null).setPositiveButton("Зберегти",(d,w)->{p.edit().putString("filter_min_fare",fare.getText().toString()).putString("filter_min_rate",rate.getText().toString()).putString("filter_max_km",distance.getText().toString()).apply();LocalEventLog.add(this,"Фільтр пропозицій оновлено");Toast.makeText(this,"Фільтр збережено",Toast.LENGTH_SHORT).show();}).show();
    }
    private void editVehicleCosts(){
        android.content.SharedPreferences p=getSharedPreferences("taxiscan_local",MODE_PRIVATE);LinearLayout f=form();EditText use=field("Витрата пального, л/100 км",p.getString("fuelUse","8"));EditText price=field("Ціна пального, ₴/л",p.getString("fuelPrice","95"));EditText wear=field("Амортизація, ₴/км",p.getString("wear","1.50"));f.addView(use);f.addView(price);f.addView(wear);
        new AlertDialog.Builder(this).setTitle("Ціна 1 км авто").setView(f).setNegativeButton("Скасувати",null).setPositiveButton("Зберегти",(d,w)->{p.edit().putString("fuelUse",use.getText().toString()).putString("fuelPrice",price.getText().toString()).putString("wear",wear.getText().toString()).apply();double cost=safe(use.getText().toString(),8)*safe(price.getText().toString(),95)/100.0+safe(wear.getText().toString(),1.5);Toast.makeText(this,String.format(java.util.Locale.getDefault(),"Собівартість: %.2f ₴/км + комісія",cost),Toast.LENGTH_LONG).show();LocalEventLog.add(this,"Витрати авто оновлено");}).show();
    }
    private double safe(String s,double d){try{return Double.parseDouble(s.trim().replace(',','.'));}catch(Exception ignored){return d;}}
    private void toggleSound(){boolean next=!features().getBoolean("sound_enabled",false);features().edit().putBoolean("sound_enabled",next).apply();LocalEventLog.add(this,next?"Звуковий сигнал увімкнено":"Звуковий сигнал вимкнено");refreshStatuses();Toast.makeText(this,next?"Сигнал нової пропозиції увімкнено":"Сигнал вимкнено",Toast.LENGTH_SHORT).show();}
    private void showInfo(){
        JSONArray trips;try{trips=new JSONArray(getSharedPreferences("taxiscan_local",MODE_PRIVATE).getString("trips","[]"));}catch(Exception ignored){trips=new JSONArray();}int today=0;long day=System.currentTimeMillis()/86400000L;double net=0;
        for(int i=0;i<trips.length();i++){JSONObject x=trips.optJSONObject(i);if(x!=null&&x.optLong("time",0)/86400000L==day){today++;net+=x.optDouble("net",0);}}
        String s="Версія 2.2.0\nЗбережено поїздок: "+trips.length()+"\nСьогодні: "+today+" · чистими ₴ "+String.format(java.util.Locale.getDefault(),"%.2f",net)+"\n\nОбробка пропозицій відбувається на пристрої. Текст замовлень не додається до логів.";
        new AlertDialog.Builder(this).setTitle("TaxiAnalytic · інформація").setMessage(s).setPositiveButton("Готово",null).setNeutralButton("Відкрити калькулятор",(d,w)->finish()).show();
    }
    private void editOverlay(){
        android.content.SharedPreferences p=features();LinearLayout f=form();EditText size=field("Розмір тексту (11–22)",String.valueOf(p.getInt("overlay_text_size",15)));EditText y=field("Відступ згори в dp (40–220)",String.valueOf(p.getInt("overlay_y",70)));EditText opacity=field("Прозорість (100–255)",String.valueOf(p.getInt("overlay_opacity",235)));f.addView(size);f.addView(y);f.addView(opacity);
        new AlertDialog.Builder(this).setTitle("Плаваюча картка").setView(f).setNegativeButton("Скасувати",null).setPositiveButton("Зберегти",(d,w)->{p.edit().putInt("overlay_text_size",(int)bound(safe(size.getText().toString(),15),11,22)).putInt("overlay_y",(int)bound(safe(y.getText().toString(),70),40,220)).putInt("overlay_opacity",(int)bound(safe(opacity.getText().toString(),235),100,255)).apply();OfferOverlay.hide();Toast.makeText(this,"Параметри картки збережено",Toast.LENGTH_SHORT).show();LocalEventLog.add(this,"Плаваюче вікно налаштовано");}).show();
    }
    private double bound(double v,double min,double max){return Math.max(min,Math.min(max,v));}
    private void showTimers(){String[] names={"Прийняв замовлення","Зустріч із пасажиром","Очікування","Загальний час"};new AlertDialog.Builder(this).setTitle("Таймер поїздки").setItems(names,(d,which)->startTimer(names[which])).setNegativeButton("Закрити",null).show();}
    private void startTimer(String label){EditText minutes=field("Тривалість, хв",label.equals("Зустріч із пасажиром")?"5":"30");new AlertDialog.Builder(this).setTitle(label).setMessage("Таймер лише нагадує про час, він не керує Bolt/Uklon.").setView(minutes).setNegativeButton("Скасувати",null).setPositiveButton("Старт",(d,w)->{if(activeTimer!=null)activeTimer.cancel();long ms=(long)bound(safe(minutes.getText().toString(),30),1,600)*60000L;LocalEventLog.add(this,"Запущено таймер: "+label);activeTimer=new CountDownTimer(ms,1000){public void onTick(long left){} public void onFinish(){LocalEventLog.add(PermissionSetupActivity.this,"Таймер завершився: "+label);Toast.makeText(PermissionSetupActivity.this,"Таймер завершився: "+label,Toast.LENGTH_LONG).show();}}.start();Toast.makeText(this,"Таймер запущено",Toast.LENGTH_SHORT).show();}).show();}
    private void showBackup(){new AlertDialog.Builder(this).setTitle("Резервна копія").setMessage("Експортуються локальні налаштування калькулятора, фільтра та картки. Історію поїздок і вміст замовлень не експортуємо.").setPositiveButton("Зберегти файл",(d,w)->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"TaxiAnalytic_settings_backup.json");startActivityForResult(i,EXPORT_BACKUP);}).setNeutralButton("Відновити",(d,w)->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");startActivityForResult(i,IMPORT_BACKUP);}).setNegativeButton("Закрити",null).show();}
    private JSONObject backupJson(){JSONObject root=new JSONObject();try{root.put("schema",1);root.put("local",prefsJson(getSharedPreferences("taxiscan_local",MODE_PRIVATE).getAll()));root.put("features",prefsJson(features().getAll()));}catch(Exception ignored){}return root;}
    private JSONObject prefsJson(Map<String,?> values){JSONObject j=new JSONObject();for(Map.Entry<String,?> e:values.entrySet()){try{Object v=e.getValue();if(v instanceof String||v instanceof Number||v instanceof Boolean)j.put(e.getKey(),v);}catch(Exception ignored){}}return j;}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;try{if(requestCode==EXPORT_BACKUP){OutputStream out=getContentResolver().openOutputStream(data.getData());if(out!=null){out.write(backupJson().toString(2).getBytes(StandardCharsets.UTF_8));out.close();Toast.makeText(this,"Резервну копію збережено",Toast.LENGTH_LONG).show();}}else if(requestCode==IMPORT_BACKUP){String json=readUri(data.getData());JSONObject root=new JSONObject(json);if(root.optInt("schema")!=1)throw new IllegalArgumentException("Непідтримуваний формат файлу");restorePrefs("taxiscan_local",root.optJSONObject("local"));restorePrefs("taxiscan_features",root.optJSONObject("features"));Toast.makeText(this,"Налаштування відновлено",Toast.LENGTH_LONG).show();refreshStatuses();}}catch(Exception e){Toast.makeText(this,"Не вдалося обробити файл резервної копії",Toast.LENGTH_LONG).show();}}
    private String readUri(Uri uri)throws Exception{java.io.InputStream in=getContentResolver().openInputStream(uri);if(in==null)throw new IllegalArgumentException("Файл недоступний");java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)b.write(buf,0,n);in.close();return new String(b.toByteArray(),StandardCharsets.UTF_8);}
    private void restorePrefs(String name,JSONObject values){if(values==null)return;android.content.SharedPreferences.Editor e=getSharedPreferences(name,MODE_PRIVATE).edit();e.clear();java.util.Iterator<String> keys=values.keys();while(keys.hasNext()){String k=keys.next();Object v=values.opt(k);if(v instanceof String)e.putString(k,(String)v);else if(v instanceof Boolean)e.putBoolean(k,(Boolean)v);else if(v instanceof Integer)e.putInt(k,(Integer)v);else if(v instanceof Number)e.putFloat(k,((Number)v).floatValue());}e.apply();}
    private void openUpdates(){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/romanstars/TaxiScan-Android33/releases")));}catch(Exception e){Toast.makeText(this,"Сторінка оновлень недоступна",Toast.LENGTH_SHORT).show();}}
    private void showTripForecast(){LinearLayout f=form();EditText km=field("Відстань, км","10");EditText speed=field("Середня швидкість, км/год","25");EditText delay=field("Запас на затори, хв","10");f.addView(km);f.addView(speed);f.addView(delay);new AlertDialog.Builder(this).setTitle("Прогноз часу поїздки").setMessage("Оцінка на телефоні за відстанню, швидкістю та вашим запасом на затори. Для онлайн-прогнозу потрібен окремо налаштований API.").setView(f).setNegativeButton("Закрити",null).setPositiveButton("Порахувати",(d,w)->{double minutes=safe(km.getText().toString(),0)/Math.max(1,safe(speed.getText().toString(),25))*60+safe(delay.getText().toString(),10);Toast.makeText(this,String.format(java.util.Locale.getDefault(),"Орієнтовно %.0f хв",minutes),Toast.LENGTH_LONG).show();}).show();}
    private void launchBolt(){Intent i=getPackageManager().getLaunchIntentForPackage("ee.mtakso.driver");if(i==null){Toast.makeText(this,"Bolt Driver не знайдено. Встанови його та спробуй ще раз.",Toast.LENGTH_LONG).show();return;}try{startActivity(i);}catch(Exception e){Toast.makeText(this,"Не вдалося відкрити Bolt Driver",Toast.LENGTH_SHORT).show();}}
    private void showLogs(){JSONArray logs=LocalEventLog.get(this);StringBuilder b=new StringBuilder();for(int i=0;i<logs.length();i++)b.append(logs.optString(i)).append('\n');if(logs.length()==0)b.append("Подій ще немає.");new AlertDialog.Builder(this).setTitle("Логи TaxiAnalytic").setMessage(b.toString()).setPositiveButton("Готово",null).setNeutralButton("Очистити",(d,w)->{LocalEventLog.clear(this);Toast.makeText(this,"Локальні логи очищено",Toast.LENGTH_SHORT).show();}).show();}
    private void showNoAutomation(String text){new AlertDialog.Builder(this).setTitle("Ручне підтвердження").setMessage(text).setPositiveButton("Зрозуміло",null).show();}

    private LinearLayout panel(boolean vertical){LinearLayout l=new LinearLayout(this);l.setOrientation(vertical?LinearLayout.VERTICAL:LinearLayout.VERTICAL);l.setPadding(dp(12),dp(12),dp(12),dp(12));l.setBackground(round(card,dp(15),green,dp(1)));return l;}
    private Button button(String title,int color,int textColor){Button b=new Button(this);b.setText(title);b.setTextColor(textColor);b.setTextSize(14);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);b.setPadding(dp(4),0,dp(4),0);b.setBackground(round(color,dp(5),Color.TRANSPARENT,0));return b;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable round(int color,int radius,int stroke,int width){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(width>0)d.setStroke(width,stroke);return d;}
    private LinearLayout.LayoutParams lp(int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){super.onRequestPermissionsResult(requestCode,permissions,results);refreshStatuses();if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)Toast.makeText(this,"Дозвіл надано",Toast.LENGTH_SHORT).show();}
    private static final class CardRef{final int kind;final Button button;CardRef(int k,Button b){kind=k;button=b;}}
}
