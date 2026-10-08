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
import android.widget.ImageView;
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
    private final int bg = Color.rgb(9,11,12), panel = Color.rgb(20,23,25), muted = Color.rgb(157,164,170), white = Color.rgb(245,247,248), green = Color.rgb(0,211,133), yellow = Color.rgb(255,211,0), line = Color.rgb(43,48,51);
    private EditText fare, rideKm, pickupKm, commission, fuelUse, fuelPrice, wear;
    private TextView netValue, breakdown, todayValue, todayKmValue, history;
    private TripCalculator.Result lastResult;
    private NumberFormat money;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(0);
        money = NumberFormat.getNumberInstance(new Locale("uk", "UA")); money.setMinimumFractionDigits(2); money.setMaximumFractionDigits(2);
        buildScreen(); loadSettings(); refreshHistory();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(18),dp(16),dp(18),dp(30)); page.setBackgroundColor(bg); scroll.addView(page);

        LinearLayout brandRow = new LinearLayout(this); brandRow.setGravity(Gravity.CENTER_VERTICAL); page.addView(brandRow, margin(0,2,0,16));
        ImageView mark = new ImageView(this); mark.setImageResource(R.drawable.ic_taxianalytic_brand); mark.setPadding(dp(5),dp(5),dp(5),dp(5)); mark.setBackground(round(Color.rgb(10,42,31),dp(16),Color.rgb(0,110,72),dp(1))); brandRow.addView(mark,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout brandCopy = new LinearLayout(this); brandCopy.setOrientation(LinearLayout.VERTICAL); brandCopy.setPadding(dp(11),0,0,0); brandRow.addView(brandCopy,new LinearLayout.LayoutParams(0,-2,1));
        brandCopy.addView(text("TaxiAnalytic",23,white,true)); brandCopy.addView(text("АНАЛІЗ ПОЇЗДОК І ПРИБУТКУ",10,green,true),margin(0,1,0,0));
        TextView badge=text("ВОДІЙ",10,muted,true); badge.setGravity(Gravity.CENTER); badge.setPadding(dp(11),dp(8),dp(11),dp(8)); badge.setBackground(round(panel,dp(30),line,dp(1))); brandRow.addView(badge);

        LinearLayout connect=card(); connect.setOrientation(LinearLayout.HORIZONTAL); connect.setGravity(Gravity.CENTER_VERTICAL); connect.setPadding(dp(15),dp(12),dp(15),dp(12)); connect.setBackground(round(Color.rgb(10,31,24),dp(16),Color.rgb(0,117,76),dp(1))); page.addView(connect,margin(0,0,0,14));
        TextView connectDot=text("●",14,green,true); connect.addView(connectDot,new LinearLayout.LayoutParams(dp(25),-2));
        LinearLayout connectCopy=new LinearLayout(this); connectCopy.setOrientation(LinearLayout.VERTICAL); connect.addView(connectCopy,new LinearLayout.LayoutParams(0,-2,1));
        connectCopy.addView(text("Підключення платформ",15,white,true)); connectCopy.addView(text("Bolt та Uklon доступні зараз",12,muted,false),margin(0,2,0,0));
        TextView connectArrow=text("›",28,green,true); connectArrow.setGravity(Gravity.CENTER); connect.addView(connectArrow,new LinearLayout.LayoutParams(dp(26),-2));
        connect.setOnClickListener(v -> startActivity(new android.content.Intent(this, PermissionSetupActivity.class)));

        LinearLayout profit = card(); profit.setBackground(round(panel,dp(22),line,dp(1))); page.addView(profit, margin(0,0,0,12));
        TextView profitLabel=text("ЧИСТИЙ ПРИБУТОК ЗА ПОЇЗДКУ",12,muted,true); profit.addView(profitLabel);
        netValue = text("₴ 0,00",38,green,true); profit.addView(netValue, margin(0,5,0,0));
        breakdown = text("Введи дані поїздки, щоб побачити суму після витрат.",13,muted,false); breakdown.setLineSpacing(dp(2),1f); profit.addView(breakdown, margin(0,7,0,0));

        LinearLayout stats = new LinearLayout(this); stats.setOrientation(LinearLayout.HORIZONTAL); page.addView(stats, margin(0,0,0,8));
        LinearLayout todayCard = card(); stats.addView(todayCard, new LinearLayout.LayoutParams(0,-2,1));
        todayCard.addView(text("₴  СЬОГОДНІ",11,muted,true)); todayValue=text("₴ 0,00",20,white,true); todayCard.addView(todayValue,margin(0,6,0,0));
        LinearLayout kmCard = card(); LinearLayout.LayoutParams kmLp = new LinearLayout.LayoutParams(0,-2,1); kmLp.setMargins(dp(10),0,0,0); stats.addView(kmCard,kmLp);
        kmCard.addView(text("↗  КІЛОМЕТРАЖ",11,muted,true)); todayKmValue=text("0,0 км",20,white,true); kmCard.addView(todayKmValue,margin(0,6,0,0));

        section(page,"Нова поїздка","ВАРТІСТЬ ТА ВІДСТАНЬ");
        LinearLayout rideForm=card(); page.addView(rideForm,margin(0,0,0,8));
        fare = field(rideForm,"Вартість поїздки, ₴","0");
        rideKm = field(rideForm,"Відстань із пасажиром, км","0");
        pickupKm = field(rideForm,"Подача до пасажира, км","0");
        section(page,"Витрати автомобіля","КОМІСІЯ ТА СОБІВАРТІСТЬ");
        LinearLayout costForm=card(); page.addView(costForm,margin(0,0,0,8));
        commission = field(costForm,"Комісія сервісу, %","15");
        fuelUse = field(costForm,"Витрата пального, л / 100 км","8");
        fuelPrice = field(costForm,"Ціна пального, ₴ / л","95");
        wear = field(costForm,"Амортизація, ₴ / км","1.50");

        Button calculate = button("Порахувати прибуток",yellow,Color.rgb(18,18,18)); page.addView(calculate,margin(0,14,0,9));
        calculate.setOnClickListener(v -> calculate());
        Button save = button("Зберегти поїздку",panel,green); save.setBackground(round(panel,dp(16),Color.rgb(0,117,76),dp(1))); page.addView(save,margin(0,0,0,14));
        save.setOnClickListener(v -> saveTrip());
        section(page,"Історія поїздок","ЗБЕРЕЖЕНІ РОЗРАХУНКИ");
        LinearLayout historyCard=card(); page.addView(historyCard,margin(0,0,0,8));
        history = text("Поки що поїздок немає.",14,muted,false); history.setLineSpacing(dp(4),1f); historyCard.addView(history);
        setContentView(scroll);
    }

    private EditText field(LinearLayout parent,String label,String hint) {
        TextView title=text(label,13,muted,true); parent.addView(title,margin(0,8,0,5));
        EditText input=new EditText(this); input.setSingleLine(true); input.setTextSize(16); input.setTextColor(white); input.setHintTextColor(Color.rgb(108,116,121)); input.setHint(hint); input.setPadding(dp(14),dp(10),dp(14),dp(10)); input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED); input.setBackground(round(Color.rgb(12,14,15),dp(12),line,dp(1))); parent.addView(input,new LinearLayout.LayoutParams(-1,dp(50))); return input;
    }

    private void section(LinearLayout p,String title,String caption){ LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.BOTTOM); row.setOrientation(LinearLayout.VERTICAL); p.addView(row,margin(0,15,0,8)); row.addView(text(title,19,white,true)); row.addView(text(caption,10,green,true),margin(0,2,0,0)); }
    private LinearLayout card(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(15),dp(14),dp(15),dp(14)); l.setBackground(round(panel,dp(18),line,dp(1))); return l; }
    private Button button(String label,int color,int textColor){ Button b=new Button(this); b.setText(label); b.setTextColor(textColor); b.setTextSize(15); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setAllCaps(false); b.setBackground(round(color,dp(15),Color.TRANSPARENT,0)); b.setMinHeight(dp(52)); b.setElevation(dp(1)); return b; }
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
