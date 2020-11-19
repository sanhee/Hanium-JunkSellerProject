package com.example.junksellerapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.model.CalendarEvent;
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.example.junksellerapp.database.AppDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class daily_statistics extends AppCompatActivity {

    private String deviceID = "";
    private HorizontalCalendar horizontalCalendar;
    private Pie pie;
    private int updatePeriod = 1200;


    private int Total_soju, Total_makju, Total_can, Total_book = 0;


    private int receiveCheck = 0;

    // 소켓통신
    public String serverURL = "http://103.124.101.163:3000/";
    Socket socket;


    //ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        setContentView(R.layout.activity_daily_statistics);

        // Room 라이브러리 (db)
        AppDatabase db = Room.databaseBuilder(this,AppDatabase.class,"device-db")
                .allowMainThreadQueries()
                .build();

        deviceID = db.todoDao().getAll().get(0).getTitle(); //  기기 ID 불러오기

        InitialSocket(); // 소켓통신 연결, 이벤트리스너 등 함수화

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(true);


        /* start 2 months ago from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        /* end after 2 months from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);

        // Default Date set to Today.
        Calendar defaultSelectedDate = Calendar.getInstance();

        horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .configure()
                .formatTopText("MMM")
                .formatMiddleText("dd")
                .formatBottomText("EEE")
                .showTopText(true)
                .showBottomText(true)
                .textColor(Color.LTGRAY, Color.WHITE)
                .colorTextMiddle(Color.LTGRAY, Color.parseColor("#ffd54f"))
                .end()
                .defaultSelectedDate(defaultSelectedDate)
                .addEvents(new CalendarEventsPredicate() {

                    Random rnd = new Random();
                    @Override
                    public List<CalendarEvent> events(Calendar date) {
                        List<CalendarEvent> events = new ArrayList<>();
                        int count = rnd.nextInt(6);

                        for (int i = 0; i <= count; i++){
                            events.add(new CalendarEvent(Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)), "event"));
                        }

                        return events;
                    }
                })
                .build();

        Log.i("Default Date", DateFormat.format("EEE, MMM d, yyyy", defaultSelectedDate).toString());



        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));
        APIlib.getInstance().setActiveAnyChartView(anyChartView);

        pie = AnyChart.pie();
        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                //Toast.makeText(daily_statistics.this, event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });


        JSONObject sendData = new JSONObject();
        try {

            sendData.put("deviceID", deviceID);
            socket.emit("Request_Today_Statistic", sendData);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        //dialog = new ProgressDialog(daily_statistics.this);
        //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //dialog.setMessage("확인");
       // dialog.show();
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //여기에 딜레이 후 시작할 작업들을 입력

                    if (receiveCheck == 1) {

                    List<DataEntry> data = new ArrayList<>();
                    data.add(new ValueDataEntry("소주병", Total_soju));
                    data.add(new ValueDataEntry("맥주병", Total_makju));
                    data.add(new ValueDataEntry("캔", Total_can));
                    data.add(new ValueDataEntry("책", Total_book));

                    pie.data(data);

                    // pie.title("일간 통계");

                    pie.labels().position("outside");

                    pie.legend().title().enabled(true);
                    pie.legend().title()
                            .text("종류")
                            .padding(0d, 0d, 10d, 0d);

                    pie.legend()
                            .position("center-bottom")
                            .itemsLayout(LegendLayout.HORIZONTAL)
                            .align(Align.CENTER);

                    anyChartView.setChart(pie);


                    }
                    else if (receiveCheck == 0) {

                    List<DataEntry> data = new ArrayList<>();
                    data.add(new ValueDataEntry("데이터 없음", 1));

                    pie.data(data);

                    // pie.title("일간 통계");

                    pie.labels().position("outside");

                    pie.legend().title().enabled(true);
                    pie.legend().title()
                            .text("종류")
                            .padding(0d, 0d, 10d, 0d);

                    pie.legend()
                            .position("center-bottom")
                            .itemsLayout(LegendLayout.HORIZONTAL)
                            .align(Align.CENTER);

                    anyChartView.setChart(pie);


                }
            }
        }, updatePeriod);// 0.5초 정도 딜레이를 준 후 시작


        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {

                String selectedDate = DateFormat.format("yyyy-MM-dd", date).toString();
                //Toast.makeText(Statistics.this, selectedDate + " selected!", Toast.LENGTH_SHORT).show();
                //Log.i("onDateSelected", selectedDate + " - Position = " + position);

                pie.legend().title()
                        .text(selectedDate)
                        .padding(0d, 0d, 10d, 0d);

                JSONObject sendData = new JSONObject();
                try {

                    sendData.put("deviceID", deviceID);
                    sendData.put("selectedDate", selectedDate);
                    socket.emit("Request_Daily_Statistic", sendData);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //여기에 딜레이 후 시작할 작업들을 입력

                        if (receiveCheck == 2) {

                            List<DataEntry> data = new ArrayList<>();

                            data.add(new ValueDataEntry("소주병", Total_soju));
                            data.add(new ValueDataEntry("맥주병", Total_makju));
                            data.add(new ValueDataEntry("캔", Total_can));
                            data.add(new ValueDataEntry("책", Total_book));

                            pie.data(data);
                            receiveCheck = 0;

                        }
                        else if (receiveCheck == 0 || receiveCheck == 1) {

                            List<DataEntry> data = new ArrayList<>();

                            data.add(new ValueDataEntry("데이터 없음", 1));
                            pie.data(data);
                        }

                    }
                }, updatePeriod);// 0.5초 정도 딜레이를 준 후 시작

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.week_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.month:
                //select account item
                finish();
                Intent intent = new Intent(this, week_statistics.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                break;
            case android.R.id.home:
                //select back button
                finish();
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent2);
                overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void InitialSocket(){

        try {
            socket = IO.socket(serverURL);
        }catch (Exception e) {
            e.printStackTrace();
        }
        socket.connect(); // 소켓통신 시작

        socket.on("Send_Today_Statistic", Send_Today_Statistic);  // EVENT_CONNECT = connect, 접속했을때
        socket.on("Send_Daily_Statistic", Send_Daily_Statistic);  // EVENT_CONNECT = connect, 접속했을때

    }

    private Emitter.Listener Send_Today_Statistic = new Emitter.Listener() {
        @Override
        public void call(Object... args2) {

            receiveCheck = 1;
            try
            {
                JSONObject jsonObject = new JSONObject(args2[0].toString());

                Total_soju = Integer.parseInt(jsonObject.getString("soju"));
                Total_makju = Integer.parseInt(jsonObject.getString("makju"));
                Total_can = Integer.parseInt(jsonObject.getString("can"));
                Total_book = Integer.parseInt(jsonObject.getString("book"));

            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Log.d("TAG","에러: "+ e.toString());
            }


        }
    };

    private Emitter.Listener Send_Daily_Statistic = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            receiveCheck = 2;
            try
            {
                JSONObject jsonObject = new JSONObject(args[0].toString());

                Total_soju = Integer.parseInt(jsonObject.getString("soju"));
                Total_makju = Integer.parseInt(jsonObject.getString("makju"));
                Total_can = Integer.parseInt(jsonObject.getString("can"));
                Total_book = Integer.parseInt(jsonObject.getString("book"));

            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Log.d("TAG","에러: "+ e.toString());
            }


        }
    };

}


