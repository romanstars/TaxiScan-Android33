package com.taxiscan.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "taxiscan_local";
    private final int bg = Color.rgb(8,10,11), panel = Color.rgb(19,22,24), muted = Color.rgb(155,162,168), white = Color.rgb(245,247,248), green = Color.rgb(0,211,133), yellow = Color.rgb(255,211,0), line = Color.rgb(43,48,51);
    private EditText fare, rideKm, pickupKm, commission, fuelUse, fuelPrice, wear;
    private TextView netValue, breakdown, todayValue, todayKmValue, todayTripsValue, avgRateValue, dailyDelta, orderStatus, history, scanState;
    private EarningsChart earningsChart;
    private ScrollView scroll;
    private View analyticsAnchor, historyAnchor;
    private TripCalculator.Result lastResult;
    private NumberFormat money;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg); getWindow().getDecorView().setSystemUiVisibility(0);
        money = NumberFormat.getNumberInstance(new Locale("uk", "UA")); money.setMinimumFractionDigits(2); money.setMaximumFractionDigits(2);
        buildScreen(); loadSettings(); refreshHistory(); refreshScanState();
    }

    @Override protected void onResume() { super.onResume(); if (scanState != null) refreshScanState(); }

    private void buildScreen() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg);
        scroll=new ScrollView(this); scroll.setFillViewport(false); scroll.setClipToPadding(false); scroll.setBackgroundColor(bg);
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(17),dp(12),dp(17),dp(18)); page.setBackgroundColor(bg); scroll.addView(page,new ScrollView.LayoutParams(-1,-2));

        LinearLayout brandRow=new LinearLayout(this); brandRow.setGravity(Gravity.CENTER_VERTICAL); page.addView(brandRow,margin(0,0,0,12));
        ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.ic_taxianalytic_brand); logo.setPadding(dp(2),dp(2),dp(2),dp(2)); brandRow.addView(logo,new LinearLayout.LayoutParams(dp(62),dp(62)));
        LinearLayout brandCopy=new LinearLayout(this); brandCopy.setOrientation(LinearLayout.VERTICAL); brandCopy.setPadding(dp(7),0,0,0); brandRow.addView(brandCopy,new LinearLayout.LayoutParams(0,-2,1));
        brandCopy.addView(text("TaxiAnalytic",23,white,true)); brandCopy.addView(text("АНАЛІТИКА ПОЇЗДОК",10,green,true),margin(0,1,0,0));
        TextView profile=iconButton("♙"); brandRow.addView(profile,new LinearLayout.LayoutParams(dp(42),dp(42))); profile.setOnClickListener(v->openSettings());
        TextView settings=iconButton("⚙"); LinearLayout.LayoutParams gearLp=new LinearLayout.LayoutParams(dp(42),dp(42)); gearLp.setMargins(dp(7),0,0,0); brandRow.addView(settings,gearLp); settings.setOnClickListener(v->openSettings());

        LinearLayout status=card(); status.setOrientation(LinearLayout.HORIZONTAL); status.setGravity(Gravity.CENTER_VERTICAL); status.setPadding(dp(14),dp(10),dp(13),dp(10)); status.setBackground(round(Color.rgb(9,34,25),dp(24),Color.rgb(0,111,73),dp(1))); page.addView(status,margin(0,0,0,13));
        TextView dot=text("●",16,green,true); status.addView(dot,new LinearLayout.LayoutParams(dp(26),-2));
        scanState=text("ПІДКЛЮЧЕННЯ АНАЛІЗУ",14,green,true); status.addView(scanState,new LinearLayout.LayoutParams(0,-2,1));
        TextView chevron=text("›",27,green,true); chevron.setGravity(Gravity.CENTER); status.addView(chevron,new LinearLayout.LayoutParams(dp(24),-2)); status.setOnClickListener(v->openSettings());

        LinearLayout earnings=card(); earnings.setPadding(dp(17),dp(16),dp(17),dp(13)); page.addView(earnings,margin(0,0,0,11));
        LinearLayout earningsHeading=new LinearLayout(this); earningsHeading.setGravity(Gravity.CENTER_VERTICAL); earnings.addView(earningsHeading,new LinearLayout.LayoutParams(-1,-2));
        TextView earningsTitle=text("ЗАРОБІТОК СЬОГОДНІ",13,muted,true); earningsHeading.addView(earningsTitle,new LinearLayout.LayoutParams(0,-2,1)); earningsHeading.addView(text("›",27,muted,true));
        todayValue=text("₴ 0,00",38,white,true); earnings.addView(todayValue,margin(0,5,0,0));
        LinearLayout deltaRow=new LinearLayout(this); deltaRow.setGravity(Gravity.CENTER_VERTICAL); earnings.addView(deltaRow,margin(0,1,0,1));
        dailyDelta=text("↗  За збереженими поїздками",14,green,true); deltaRow.addView(dailyDelta);
        earningsChart=new EarningsChart(this); earnings.addView(earningsChart,new LinearLayout.LayoutParams(-1,dp(116)));
        LinearLayout times=new LinearLayout(this); times.setGravity(Gravity.CENTER_VERTICAL); earnings.addView(times,new LinearLayout.LayoutParams(-1,-2));
        String[] labels={"00:00","06:00","12:00","18:00","24:00"}; for(String label:labels){TextView t=text(label,10,muted,false);t.setGravity(Gravity.CENTER);times.addView(t,new LinearLayout.LayoutParams(0,-2,1));}

        LinearLayout stats=new LinearLayout(this); stats.setOrientation(LinearLayout.HORIZONTAL); page.addView(stats,margin(0,0,0,11));
        LinearLayout rateCard=card(); stats.addView(rateCard,new LinearLayout.LayoutParams(0,-2,1));
        rateCard.addView(text("СЕРЕДНЯ СТАВКА",11,muted,true)); avgRateValue=text("₴ 0,00 /км",19,white,true); rateCard.addView(avgRateValue,margin(0,6,0,0)); todayKmValue=text("0,0 км за день",11,muted,false); rateCard.addView(todayKmValue,margin(0,2,0,0));
        LinearLayout tripsCard=card(); LinearLayout.LayoutParams tripsLp=new LinearLayout.LayoutParams(0,-2,1); tripsLp.setMargins(dp(9),0,0,0); stats.addView(tripsCard,tripsLp);
        tripsCard.addView(text("ПОЇЗДКИ",11,muted,true)); todayTripsValue=text("0",22,white,true); tripsCard.addView(todayTripsValue,margin(0,5,0,0));

        analyticsAnchor=new View(this); page.addView(analyticsAnchor,new LinearLayout.LayoutParams(1,dp(1)));
        LinearLayout order=card(); order.setPadding(dp(15),dp(15),dp(15),dp(15)); order.setBackground(round(panel,dp(20),Color.rgb(0,104,71),dp(1))); page.addView(order,margin(0,0,0,11));
        LinearLayout orderTitleRow=new LinearLayout(this); orderTitleRow.setGravity(Gravity.CENTER_VERTICAL); order.addView(orderTitleRow,new LinearLayout.LayoutParams(-1,-2));
        TextView orderTitle=text("АНАЛІЗ ЗАМОВЛЕННЯ",13,muted,true); orderTitleRow.addView(orderTitle,new LinearLayout.LayoutParams(0,-2,1));
        orderStatus=text("ВВЕДИ ДАНІ",10,muted,true); orderStatus.setPadding(dp(9),dp(6),dp(9),dp(6)); orderStatus.setBackground(round(Color.rgb(28,33,35),dp(18),line,dp(1))); orderTitleRow.addView(orderStatus);
        LinearLayout fieldsRow=new LinearLayout(this); fieldsRow.setOrientation(LinearLayout.HORIZONTAL); fieldsRow.setGravity(Gravity.TOP); order.addView(fieldsRow,margin(0,12,0,2));
        LinearLayout fareCol=new LinearLayout(this); fareCol.setOrientation(LinearLayout.VERTICAL); fieldsRow.addView(fareCol,new LinearLayout.LayoutParams(0,-2,1));
        fare=field(fareCol,"Оплата, ₴","0");
        LinearLayout kmCol=new LinearLayout(this); kmCol.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams kmColLp=new LinearLayout.LayoutParams(0,-2,1); kmColLp.setMargins(dp(8),0,0,0); fieldsRow.addView(kmCol,kmColLp);
        rideKm=field(kmCol,"З пасажиром, км","0");
        LinearLayout pickupCol=new LinearLayout(this); pickupCol.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams pickupLp=new LinearLayout.LayoutParams(0,-2,1); pickupLp.setMargins(dp(8),0,0,0); fieldsRow.addView(pickupCol,pickupLp);
        pickupKm=field(pickupCol,"Подача, км","0");
        TextView orderOutputLabel=text("ЧИСТИМИ ПІСЛЯ ВИТРАТ",11,muted,true); order.addView(orderOutputLabel,margin(0,9,0,0));
        netValue=text("₴ 0,00",26,white,true); order.addView(netValue,margin(0,3,0,0));
        breakdown=text("Вкажи суму й кілометраж та натисни «Перевірити».",12,muted,false); breakdown.setLineSpacing(dp(2),1f); order.addView(breakdown,margin(0,3,0,0));
        Button calculate=button("ПЕРЕВІРИТИ  ›",yellow,Color.rgb(18,18,18)); order.addView(calculate,margin(0,12,0,0)); calculate.setOnClickListener(v->calculate());
        Button save=button("ЗБЕРЕГТИ ПОЇЗДКУ",panel,green); save.setBackground(round(panel,dp(15),Color.rgb(0,117,76),dp(1))); order.addView(save,margin(0,8,0,0)); save.setOnClickListener(v->saveTrip());

        section(page,"Витрати автомобіля","КОМІСІЯ ТА СОБІВАРТІСТЬ");
        LinearLayout costForm=card(); page.addView(costForm,margin(0,0,0,11));
        commission=field(costForm,"Комісія сервісу, %","15"); fuelUse=field(costForm,"Витрата пального, л / 100 км","8"); fuelPrice=field(costForm,"Ціна пального, ₴ / л","95"); wear=field(costForm,"Амортизація, ₴ / км","1.50");

        historyAnchor=new View(this); page.addView(historyAnchor,new LinearLayout.LayoutParams(1,dp(1)));
        section(page,"Історія поїздок","ОСТАННІ ЗБЕРЕЖЕНІ РОЗРАХУНКИ");
        LinearLayout historyCard=card(); page.addView(historyCard,margin(0,0,0,8)); history=text("Збережені поїздки з'являться тут.",13,muted,false); history.setLineSpacing(dp(4),1f); historyCard.addView(history);

        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(bottomNavigation(),new LinearLayout.LayoutParams(-1,dp(62)));
        setContentView(root);
    }

    private LinearLayout bottomNavigation(){
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setBackgroundColor(Color.rgb(16,18,20));
        TextView home=navItem("⌂","Головна",true);bar.addView(home,new LinearLayout.LayoutParams(0,-1,1));home.setOnClickListener(v->{scroll.smoothScrollTo(0,0);selectNav(bar,home);});
        TextView analytics=navItem("▥","Аналітика",false);bar.addView(analytics,new LinearLayout.LayoutParams(0,-1,1));analytics.setOnClickListener(v->{scroll.smoothScrollTo(0,analyticsAnchor.getTop());selectNav(bar,analytics);});
        TextView trips=navItem("◷","Історія",false);bar.addView(trips,new LinearLayout.LayoutParams(0,-1,1));trips.setOnClickListener(v->{scroll.smoothScrollTo(0,historyAnchor.getTop());selectNav(bar,trips);});
        TextView profile=navItem("♙","Профіль",false);bar.addView(profile,new LinearLayout.LayoutParams(0,-1,1));profile.setOnClickListener(v->openSettings());
        return bar;
    }
    private TextView navItem(String icon,String label,boolean selected){TextView t=text(icon+"\n"+label,11,selected?yellow:muted,selected);t.setGravity(Gravity.CENTER);t.setLineSpacing(0,0.9f);return t;}
    private void selectNav(LinearLayout bar,TextView selected){for(int i=0;i<bar.getChildCount();i++){TextView item=(TextView)bar.getChildAt(i);item.setTextColor(item==selected?yellow:muted);item.setTypeface(Typeface.DEFAULT,item==selected?Typeface.BOLD:Typeface.NORMAL);}}
    private void openSettings(){startActivity(new Intent(this,PermissionSetupActivity.class));}

    private EditText field(LinearLayout parent,String label,String hint){
        TextView title=text(label,11,muted,true);parent.addView(title,margin(0,0,0,5));
        EditText input=new EditText(this);input.setSingleLine(true);input.setTextSize(15);input.setTextColor(white);input.setHintTextColor(Color.rgb(105,113,118));input.setHint(hint);input.setPadding(dp(11),dp(8),dp(8),dp(8));input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);input.setBackground(round(Color.rgb(11,13,14),dp(11),line,dp(1)));parent.addView(input,new LinearLayout.LayoutParams(-1,dp(46)));return input;
    }
    private void section(LinearLayout p,String title,String caption){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);p.addView(row,margin(0,12,0,7));row.addView(text(title,18,white,true));row.addView(text(caption,10,green,true),margin(0,2,0,0));}
    private LinearLayout card(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(14),dp(13),dp(14),dp(13));l.setBackground(round(panel,dp(18),line,dp(1)));return l;}
    private Button button(String label,int color,int textColor){Button b=new Button(this);b.setText(label);b.setTextColor(textColor);b.setTextSize(15);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);b.setBackground(round(color,dp(15),Color.TRANSPARENT,0));b.setMinHeight(dp(50));b.setElevation(dp(1));return b;}
    private TextView iconButton(String icon){TextView t=text(icon,20,white,true);t.setGravity(Gravity.CENTER);t.setBackground(round(panel,dp(30),line,dp(1)));t.setContentDescription("Відкрити налаштування TaxiAnalytic");return t;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable round(int color,int radius,int stroke,int width){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(width>0)d.setStroke(width,stroke);return d;}
    private LinearLayout.LayoutParams margin(int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}

    private void refreshScanState(){
        boolean enabled=getSharedPreferences("taxiscan_features",MODE_PRIVATE).getBoolean("scanning_enabled",true)&&isAccessibilityEnabled();
        scanState.setText(enabled?"АНАЛІЗ УВІМКНЕНО":"ПІДКЛЮЧИТИ АНАЛІЗ" ); scanState.setTextColor(enabled?green:yellow);
    }
    private boolean isAccessibilityEnabled(){String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);if(enabled==null)return false;String wanted=new ComponentName(this,TaxiScanAccessibilityService.class).flattenToString();for(String item:enabled.split(":"))if(item.equalsIgnoreCase(wanted)||item.equalsIgnoreCase(new ComponentName(this,TaxiScanAccessibilityService.class).flattenToShortString()))return true;return false;}

    private double value(EditText e,String name){String s=e.getText().toString().trim().replace(',','.');if(s.isEmpty())return 0;try{return Double.parseDouble(s);}catch(Exception ex){throw new IllegalArgumentException("Перевір поле «"+name+"».");}}
    private void calculate(){
        lastResult=null;
        try{
            lastResult=TripCalculator.calculate(value(fare,"вартість"),value(rideKm,"відстань"),value(pickupKm,"подача"),value(commission,"комісія"),value(fuelUse,"витрата пального"),value(fuelPrice,"ціна пального"),value(wear,"амортизація"));
            netValue.setText("₴ "+money.format(lastResult.net));
            breakdown.setText("Комісія ₴ "+money.format(lastResult.commission)+"  ·  пальне ₴ "+money.format(lastResult.fuelCost)+"  ·  авто ₴ "+money.format(lastResult.wearCost)+"\n"+money.format(lastResult.totalKm)+" км загалом  ·  ₴ "+money.format(lastResult.netPerKm)+" / км чистими");
            orderStatus.setText(lastResult.net>0?"ВИГІДНО":"ПЕРЕВІР ДАНІ");orderStatus.setTextColor(lastResult.net>0?green:yellow);orderStatus.setBackground(round(Color.rgb(12,42,31),dp(18),Color.rgb(0,111,73),dp(1)));
            saveSettings();
        }catch(IllegalArgumentException ex){Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();}
    }
    private void saveSettings(){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("commission",commission.getText().toString()).putString("fuelUse",fuelUse.getText().toString()).putString("fuelPrice",fuelPrice.getText().toString()).putString("wear",wear.getText().toString()).apply();}
    private void loadSettings(){android.content.SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);commission.setText(p.getString("commission","15"));fuelUse.setText(p.getString("fuelUse","8"));fuelPrice.setText(p.getString("fuelPrice","95"));wear.setText(p.getString("wear","1.50"));}
    private void saveTrip(){
        calculate();if(lastResult==null)return;
        try{JSONArray a=new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString("trips","[]"));JSONObject x=new JSONObject();x.put("fare",lastResult.fare);x.put("km",lastResult.totalKm);x.put("net",lastResult.net);x.put("time",System.currentTimeMillis());a.put(0,x);while(a.length()>50)a.remove(a.length()-1);getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("trips",a.toString()).apply();refreshHistory();Toast.makeText(this,"Поїздку збережено",Toast.LENGTH_SHORT).show();}
        catch(Exception e){Toast.makeText(this,"Не вдалося зберегти поїздку",Toast.LENGTH_SHORT).show();}
    }
    private void refreshHistory(){
        try{
            JSONArray a=new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString("trips","[]"));double today=0,todayKm=0,yesterday=0;int todayCount=0;double[] hourly=new double[24];Calendar start=Calendar.getInstance();start.set(Calendar.HOUR_OF_DAY,0);start.set(Calendar.MINUTE,0);start.set(Calendar.SECOND,0);start.set(Calendar.MILLISECOND,0);long dayStart=start.getTimeInMillis(),now=System.currentTimeMillis(),yesterdayStart=dayStart-24L*60*60*1000;StringBuilder b=new StringBuilder();SimpleDateFormat stamp=new SimpleDateFormat("HH:mm · dd.MM",new Locale("uk","UA"));
            for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);long time=x.optLong("time");double net=x.optDouble("net"),km=x.optDouble("km");if(time>=dayStart&&time<=now){today+=net;todayKm+=km;todayCount++;Calendar tripTime=Calendar.getInstance();tripTime.setTimeInMillis(time);hourly[tripTime.get(Calendar.HOUR_OF_DAY)]+=net;}else if(time>=yesterdayStart&&time<dayStart){yesterday+=net;}if(i<8){if(b.length()>0)b.append("\n\n");b.append(stamp.format(new Date(time))).append("   ·   ").append(money.format(km)).append(" км\nЧистими: ₴ ").append(money.format(net));}}
            todayValue.setText("₴ "+money.format(today));todayKmValue.setText(money.format(todayKm)+" км за день");avgRateValue.setText("₴ "+money.format(todayKm>0?today/todayKm:0)+" /км");todayTripsValue.setText(String.valueOf(todayCount));
            if(yesterday>0){double change=(today-yesterday)*100/yesterday;dailyDelta.setText((change>=0?"↗  +":"↘  ")+money.format(change)+"% порівняно зі вчора");dailyDelta.setTextColor(change>=0?green:yellow);}else dailyDelta.setText("↗  За збереженими поїздками");
            earningsChart.setHourly(hourly);history.setText(b.length()==0?"Збережені поїздки з'являться тут.":b.toString());
        }catch(Exception ignored){if(history!=null)history.setText("Історію не вдалося прочитати.");}
    }

    private final class EarningsChart extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private double[] hourly=new double[24];private EarningsChart(Activity c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void setHourly(double[] values){hourly=values.clone();invalidate();}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float w=getWidth(),h=getHeight(),left=dp(2),right=w-dp(4),top=dp(8),bottom=h-dp(5);paint.setShader(null);paint.setStrokeWidth(dp(1));paint.setColor(Color.rgb(43,48,51));for(int i=0;i<4;i++){float y=top+(bottom-top)*i/3f;canvas.drawLine(left,y,right,y,paint);}double[] cumulative=new double[24];double total=0,max=0;for(int i=0;i<24;i++){total+=hourly[i];cumulative[i]=total;max=Math.max(max,total);}if(max<=0)max=1;Path linePath=new Path();for(int i=0;i<24;i++){float x=left+(right-left)*i/23f;float y=bottom-(float)(cumulative[i]/max)*(bottom-top-dp(5));if(i==0)linePath.moveTo(x,y);else linePath.lineTo(x,y);}Path area=new Path(linePath);area.lineTo(right,bottom);area.lineTo(left,bottom);area.close();paint.setShader(new LinearGradient(0,top,0,bottom,Color.argb(90,0,211,133),Color.argb(0,0,211,133),Shader.TileMode.CLAMP));canvas.drawPath(area,paint);paint.setShader(null);paint.setColor(green);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(3));paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);canvas.drawPath(linePath,paint);paint.setStyle(Paint.Style.FILL);if(total>0){float x=right;float y=bottom-(float)(total/max)*(bottom-top-dp(5));paint.setColor(green);canvas.drawCircle(x,y,dp(4),paint);paint.setColor(Color.WHITE);canvas.drawCircle(x,y,dp(2),paint);}}
    }
}
