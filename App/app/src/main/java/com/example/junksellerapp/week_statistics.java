package com.example.junksellerapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Cartesian3d;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Column3d;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.HoverMode;
import com.anychart.enums.ScaleStackMode;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.graphics.vector.SolidFill;
import com.example.junksellerapp.database.AppDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.model.CalendarEvent;
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate;
import io.socket.client.Socket;

public class week_statistics extends AppCompatActivity {
    private String deviceID = "";
    private HorizontalCalendar horizontalCalendar;
    private Pie pie;

    private int Total_soju, Total_makju, Total_can, Total_book = 0;
    private int receiveCheck = 0;

    // 소켓통신
    public String serverURL = "http://103.124.101.163:3000/";
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        setContentView(R.layout.activity_week_statistics);


        // Room 라이브러리 (db)
        AppDatabase db = Room.databaseBuilder(this,AppDatabase.class,"device-db")
                .allowMainThreadQueries()
                .build();

        deviceID = db.todoDao().getAll().get(0).getTitle(); //  기기 ID 불러오기

        //InitialSocket(); // 소켓통신 연결, 이벤트리스너 등 함수화

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(true);



        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));
        APIlib.getInstance().setActiveAnyChartView(anyChartView);

        Cartesian3d column3d = AnyChart.column3d();

        column3d.yScale().stackMode(ScaleStackMode.VALUE);

        column3d.animation(true);

        column3d.title("고물종류");
        column3d.title().padding(0d, 0d, 15d, 0d);

        List<DataEntry> seriesData = new ArrayList<>();
        seriesData.add(new CustomDataEntry("07/21", 3, 2, 5, 1));
        seriesData.add(new CustomDataEntry("07/22", 2, 5, null, 2));
        seriesData.add(new CustomDataEntry("07/23", 1, 1, 2, null));
        seriesData.add(new CustomDataEntry("07/24", 6, 3, null, null));
        seriesData.add(new CustomDataEntry("07/25", 4, null, 3, 2));
        seriesData.add(new CustomDataEntry("07/26", 1, null, 2, 2));
        seriesData.add(new CustomDataEntry("07/27", 2, 1, null, null));


        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Data = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Data = set.mapAs("{ x: 'x', value: 'value2' }");
        Mapping series3Data = set.mapAs("{ x: 'x', value: 'value3' }");
        Mapping series4Data = set.mapAs("{ x: 'x', value: 'value4' }");


        Column3d series1 = column3d.column(series1Data);
        series1.name("캔");
        series1.fill(new SolidFill("#3e2723", 1d));
        series1.stroke("1 #f7f3f3");
        series1.hovered().stroke("3 #f7f3f3");

        Column3d series2 = column3d.column(series2Data);
        series2.name("책");
        series2.fill(new SolidFill("#64b5f6", 1d));
        series2.stroke("1 #f7f3f3");
        series2.hovered().stroke("3 #f7f3f3");

        Column3d series3 = column3d.column(series3Data);
        series3.name("소주병");
        series3.fill(new SolidFill("#fff3e0", 1d));
        series3.stroke("1 #f7f3f3");
        series3.hovered().stroke("3 #f7f3f3");

        Column3d series4 = column3d.column(series4Data);
        series4.name("맥주병");
        series4.fill(new SolidFill("#bcaaa4", 1d));
        series4.stroke("1 #f7f3f3");
        series4.hovered().stroke("3 #f7f3f3");



        column3d.legend().enabled(true);
        column3d.legend().fontSize(13d);
        column3d.legend().padding(0d, 0d, 20d, 0d);

        column3d.yScale().ticks("[0, 3, 6, 9]");
        column3d.xAxis(0).stroke("1 #a18b7e");
        column3d.xAxis(0).labels().fontSize("#a18b7e");
        column3d.yAxis(0).stroke("1 #a18b7e");
        column3d.yAxis(0).labels().fontColor("#a18b7e");
        column3d.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        column3d.yAxis(0).title().enabled(true);
        column3d.yAxis(0).title().text("수집량");
        column3d.yAxis(0).title().fontColor("#a18b7e");

        column3d.interactivity().hoverMode(HoverMode.BY_X);

        column3d.tooltip()
                .displayMode(TooltipDisplayMode.UNION)
                .format("{%Value} {%SeriesName}");

        column3d.yGrid(0).stroke("#a18b7e", 1d, null, null, null);
        column3d.xGrid(0).stroke("#a18b7e", 1d, null, null, null);

        anyChartView.setChart(column3d);


    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2, Number value3, Number value4) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
            setValue("value4", value4);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dailymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.daily:
                //select account item
                finish();
                Intent intent = new Intent(this, daily_statistics.class);
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
}