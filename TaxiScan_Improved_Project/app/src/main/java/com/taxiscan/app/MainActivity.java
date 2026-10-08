package com.taxiscan.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "taxiscan_local";
    private final int bg = Color.rgb(11,16,32), panel = Color.rgb(20,29,49), muted = Color.rgb(157,173,198), white = Color.WHITE, green = Color.rgb(82,214,160);
    private EditText fare, rideKm, pickupKm, commission, fuelUse, fuelPrice, wear;
    private TextView netValue, breakdown, todayValue, todayKmValue, history;
    private TripCalculator.Result lastResult;
    private NumberFormat money;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        money = NumberFormat.getNumberInstance(new Locale("uk", "UA")); money.setMinimumFractionDigits(2); money.setMaximumFractionDigits(2);
        buildScreen(); loadSettings(); refreshHistory();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(20),dp(16),dp(20),dp(32)); page.setBackgroundColor(bg); scroll.addView(page);
        TextView brand = text("TaxiScan", 28, white, true); page.addView(brand);
        TextView subtitle = text("Порахуй реальний прибуток поїздки", 14, muted, false); page.addView(subtitle, margin(0,2,0,12));
        Button connect = button("Підключити Bolt / Uklon", green, Color.rgb(8,24,21)); page.addView(connect, margin(0,0,0,16));
        connect.setOnClickListener(v -> startActivity(new android.content.Intent(this, PermissionSetupActivity.class)));

        LinearLayout profit = card(); page.addView(profit, margin(0,0,0,14));
        profit.addView(text("ЧИСТИЙ ЗАРОБІТОК",12,muted,true));
        netValue = text("₴ 0,00",34,green,true); profit.addView(netValue, margin(0,6,0,0));
        breakdown = text("Комісія, пальне й амортизація будуть відняті автоматично.",13,muted,false); profit.addView(breakdown, margin(0,6,0,0));

        LinearLayout stats = new LinearLayout(this); stats.setOrientation(LinearLayout.HORIZONTAL); page.addView(stats, margin(0,0,0,14));
        LinearLayout todayCard = card(); stats.addView(todayCard, new LinearLayout.LayoutParams(0,-2,1));
        todayCard.addView(text("СЬОГОДНІ",11,muted,true)); todayValue=text("₴ 0,00",19,white,true); todayCard.addView(todayValue,margin(0,5,0,0));
        LinearLayout kmCard = card(); LinearLayout.LayoutParams kmLp = new LinearLayout.LayoutParams(0,-2,1); kmLp.setMargins(dp(10),0,0,0); stats.addView(kmCard,kmLp);
        kmCard.addView(text("КІЛОМЕТРАЖ",11,muted,true)); todayKmValue=text("0,0 км",19,white,true); kmCard.addView(todayKmValue,margin(0,5,0,0));

        section(page,"Нова поїздка");
        fare = field(page,"Вартість поїздки, ₴","0");
        rideKm = field(page,"Відстань із пасажиром, км","0");
        pickupKm = field(page,"Подача до пасажира, км","0");
        section(page,"Витрати та налаштування");
        commission = field(page,"Комісія сервісу, %","15");
        fuelUse = field(page,"Витрата пального, л / 100 км","8");
        fuelPrice = field(page,"Ціна пального, ₴ / л","95");
        wear = field(page,"Амортизація, ₴ / км","1.50");

        Button calculate = button("Порахувати прибуток",green,Color.rgb(8,24,21)); page.addView(calculate,margin(0,12,0,10));
        calculate.setOnClickListener(v -> calculate());
        Button save = button("Зберегти поїздку",Color.rgb(36,53,77),white); page.addView(save,margin(0,0,0,18));
        save.setOnClickListener(v -> saveTrip());
        section(page,"Останні поїздки");
        history = text("Поки що поїздок немає.",14,muted,false); page.addView(history,margin(0,0,0,8));
        setContentView(scroll);
    }

    private EditText field(LinearLayout parent,String label,String hint) {
        TextView title=text(label,13,muted,true); parent.addView(title,margin(0,8,0,5));
        EditText input=new EditText(this); input.setSingleLine(true); input.setTextSize(16); input.setTextColor(white); input.setHintTextColor(Color.rgb(100,117,143)); input.setHint(hint); input.setPadding(dp(14),dp(11),dp(14),dp(11)); input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED); input.setBackground(round(panel,dp(13),Color.rgb(44,58,83),dp(1))); parent.addView(input,new LinearLayout.LayoutParams(-1,dp(52))); return input;
    }

    private void section(LinearLayout p,String title){ p.addView(text(title,18,white,true),margin(0,14,0,5)); }
    private LinearLayout card(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(16),dp(15),dp(16),dp(15)); l.setBackground(round(panel,dp(18),Color.TRANSPARENT,0)); return l; }
    private Button button(String label,int color,int textColor){ Button b=new Button(this); b.setText(label); b.setTextColor(textColor); b.setTextSize(15); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setAllCaps(false); b.setBackground(round(color,dp(14),Color.TRANSPARENT,0)); b.setMinHeight(dp(52)); return b; }
    private TextView text(String s,int size,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t; }
    private GradientDrawable round(int color,int radius,int stroke,int width){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); if(width>0)d.setStroke(width,stroke); return d; }
    private LinearLayout.LayoutParams margin(int l,int t,int r,int b){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p; }
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}

    private double value(EditText e,String name){ String s=e.getText().toString().trim().replace(',','.'); if(s.isEmpty())return 0; try{return Double.parseDouble(s);}catch(Exception ex){throw new IllegalArgumentException("Перевір поле «"+name+"».");} }
    private void calculate(){
        lastResult=null;
        try{
            lastResult=TripCalculator.calculate(value(fare,"вартість"),value(rideKm,"відстань"),value(pickupKm,"подача"),value(commission,"комісія"),value(fuelUse,"витрата пального"),value(fuelPrice,"ціна пального"),value(wear,"амортизація"));
            netValue.setText("₴ "+money.format(lastResult.net));
            breakdown.setText("Комісія ₴ "+money.format(lastResult.commission)+"  ·  пальне ₴ "+money.format(lastResult.fuelCost)+"  ·  авто ₴ "+money.format(lastResult.wearCost)+"\n"+money.format(lastResult.totalKm)+" км загалом  ·  ₴ "+money.format(lastResult.netPerKm)+" / км чистими");
            saveSettings();
        }catch(IllegalArgumentException ex){Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();}
    }
    private void saveSettings(){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("commission",commission.getText().toString()).putString("fuelUse",fuelUse.getText().toString()).putString("fuelPrice",fuelPrice.getText().toString()).putString("wear",wear.getText().toString()).apply();}
    private void loadSettings(){android.content.SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE); commission.setText(p.getString("commission","15")); fuelUse.setText(p.getString("fuelUse","8")); fuelPrice.setText(p.getString("fuelPrice","95")); wear.setText(p.getString("wear","1.50"));}
    private void saveTrip(){
        calculate(); if(lastResult==null)return;
        try{
            JSONArray a=new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString("trips","[]")); JSONObject x=new JSONObject(); x.put("fare",lastResult.fare);x.put("km",lastResult.totalKm);x.put("net",lastResult.net);x.put("time",System.currentTimeMillis());a.put(0,x);while(a.length()>50)a.remove(a.length()-1);
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("trips",a.toString()).apply();refreshHistory();Toast.makeText(this,"Поїздку збережено",Toast.LENGTH_SHORT).show();
        }catch(Exception e){Toast.makeText(this,"Не вдалося зберегти поїздку",Toast.LENGTH_SHORT).show();}
    }
    private void refreshHistory(){
        try{
            JSONArray a=new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString("trips","[]"));double today=0,todayKm=0;StringBuilder b=new StringBuilder();SimpleDateFormat day=new SimpleDateFormat("yyyyMMdd",Locale.US),stamp=new SimpleDateFormat("HH:mm · dd.MM",new Locale("uk","UA"));String now=day.format(new Date());
            for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);long time=x.optLong("time");if(day.format(new Date(time)).equals(now)){today+=x.optDouble("net");todayKm+=x.optDouble("km");}if(i<8){if(b.length()>0)b.append("\n\n");b.append(stamp.format(new Date(time))).append("   ·   ").append(money.format(x.optDouble("km"))).append(" км\nЧистими: ₴ ").append(money.format(x.optDouble("net")));}}
            todayValue.setText("₴ "+money.format(today));todayKmValue.setText(money.format(todayKm)+" км");history.setText(b.length()==0?"Поки що поїздок немає.":b.toString());
        }catch(Exception ignored){history.setText("Історію не вдалося прочитати.");}
    }
}
