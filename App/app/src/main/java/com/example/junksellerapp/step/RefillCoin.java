package com.example.junksellerapp.step;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junksellerapp.MainActivity;
import com.example.junksellerapp.R;
import com.example.junksellerapp.database.AppDatabase;
import com.example.junksellerapp.week_statistics;

import org.json.JSONException;
import org.json.JSONObject;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class RefillCoin extends AppCompatActivity {

    @BindView(R.id.btn500)
    Button btn500;
    @BindView(R.id.btn100)
    Button btn100;
    @BindView(R.id.btn50)
    Button btn50;
    @BindView(R.id.btn10)
    Button btn10;
    @BindView(R.id.btnMain)
    Button btnMain;

    @BindView(R.id.remainCoin500) TextView remainCoin500;
    @BindView(R.id.remainCoin500_2) TextView remainCoin500_2;

    @BindView(R.id.remainCoin100) TextView remainCoin100;
    @BindView(R.id.remainCoin100_2) TextView remainCoin100_2;

    @BindView(R.id.remainCoin50) TextView remainCoin50;
    @BindView(R.id.remainCoin50_2) TextView remainCoin50_2;

    @BindView(R.id.remainCoin10) TextView remainCoin10;
    @BindView(R.id.remainCoin10_2) TextView remainCoin10_2;

    @BindView(R.id.editText_BT) TextView editText_BT;

    private int[] haveCoin = { 0,0,0,0 };

    // 소켓통신
    public String serverURL = "http://103.124.101.163:3000/";
    Socket socket;
    private  String deviceID = "";

    // 블루투스 통신 관련
    private app.akexorcist.bluetotohspp.library.BluetoothSPP BT  = new app.akexorcist.bluetotohspp.library.BluetoothSPP(this); //Initializing;
    private String MAC_ADDRESS = "98:D3:11:F8:1C:CC";
    private String RequestCommand = "";
    // 블루투스 통신 관련

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill_coin);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        ButterKnife.bind(this);  // 버터나이프


        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");

        remainCoin500.setTypeface(typeface);
        remainCoin500_2.setTypeface(typeface);
        remainCoin100.setTypeface(typeface);
        remainCoin100_2.setTypeface(typeface);
        remainCoin50.setTypeface(typeface);
        remainCoin50_2.setTypeface(typeface);
        remainCoin10.setTypeface(typeface);
        remainCoin10_2.setTypeface(typeface);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Room 라이브러리 (db)
        AppDatabase db = Room.databaseBuilder(this,AppDatabase.class,"device-db")
                .allowMainThreadQueries()
                .build();

        deviceID = db.todoDao().getAll().get(0).getTitle();
        InitialSocket(); // 소켓통신 연결, 이벤트리스너 등 함수화

        JSONObject sendData = new JSONObject();
        try {

            sendData.put("objectName", "soju");
            sendData.put("deviceID", deviceID);
            socket.emit("CHECK_PRICE", sendData);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 이 곳에 UI작업을 한다
                            remainCoin500_2.setText("\t" + haveCoin[0] + "개");
                            remainCoin100_2.setText("\t" + haveCoin[1] + "개");
                            remainCoin50_2.setText("\t" + haveCoin[2] + "개");
                            remainCoin10_2.setText("\t" + haveCoin[3] + "개");

                    }
                });
            }
        }, 1500);// 1.5초 정도 딜레이를 준 후 시작



        btConnect();



    }

    @OnClick(R.id.btnMain)
    void onCallClick0() {
        Intent intent = new Intent(RefillCoin.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
        finish();
    }
    @OnClick(R.id.btn500)
    void onCallClick() {
        JSONObject sendData = new JSONObject();
        try {
            sendData.put("deviceID", deviceID);
            socket.emit("ADD500", sendData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject sendData1 = new JSONObject();
        try {

            sendData1.put("objectName", "soju");
            sendData1.put("deviceID", deviceID);
            socket.emit("CHECK_PRICE", sendData1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "500원 +25개 리필완료", Toast.LENGTH_SHORT).show();
    }
    @OnClick(R.id.btn100)
    void onCallClick2() {
        JSONObject sendData = new JSONObject();
        try {
            sendData.put("deviceID", deviceID);
            socket.emit("ADD100", sendData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "100원 +25개 리필완료", Toast.LENGTH_SHORT).show();
    }
    @OnClick(R.id.btn50)
    void onCallClick3() {
        JSONObject sendData = new JSONObject();
        try {
            sendData.put("deviceID", deviceID);
            socket.emit("ADD50", sendData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "50원 +25개 리필완료", Toast.LENGTH_SHORT).show();
    }
    @OnClick(R.id.btn10)
    void onCallClick4() {
        JSONObject sendData = new JSONObject();
        try {
            sendData.put("deviceID", deviceID);
            socket.emit("ADD10", sendData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "10원 +25개 리필완료", Toast.LENGTH_SHORT).show();
    }

    public void InitialSocket(){

        try {
            socket = IO.socket(serverURL);
        }catch (Exception e) {
            e.printStackTrace();
        }
        socket.connect(); // 소켓통신 시작
        socket.on("RECEIVE_HAVE_MONEY",RECEIVE_HAVE_MONEY);

    }

    private Emitter.Listener RECEIVE_HAVE_MONEY = new Emitter.Listener() {
        @Override
        public void call(Object... args2) {

            Log.d("TAG", "리필:: args[0].toString() "+ args2[0].toString());
            try
            {
                JSONObject jsonObject = new JSONObject(args2[0].toString());


                haveCoin[0] = Integer.parseInt(jsonObject.getString("a"));
                haveCoin[1] = Integer.parseInt(jsonObject.getString("b"));
                haveCoin[2] = Integer.parseInt(jsonObject.getString("c"));
                haveCoin[3] = Integer.parseInt(jsonObject.getString("d"));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 이 곳에 UI작업을 한다

                            remainCoin500_2.setText("\t" + haveCoin[0] + "개");
                            remainCoin100_2.setText("\t" + haveCoin[1] + "개");
                            remainCoin50_2.setText("\t" + haveCoin[2] + "개");
                            remainCoin10_2.setText("\t" + haveCoin[3] + "개");

                            remainCoin500_2.invalidate();
                            remainCoin100_2.invalidate();
                            remainCoin50_2.invalidate();
                            remainCoin10_2.invalidate();

                    }
                });

            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Log.d("TAG","에러: "+ e.toString());
            }

        }
    };

    public void btConnect(){
        if (!BT.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!BT.isBluetoothEnabled()) { // 블루투스 OFF
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // 권한요청
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        else { // 블루투스 ON
            if (!BT.isServiceAvailable()) { // 블루투스 서비스 OFF
                BT.setupService();
                BT.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리

                BT.connect(MAC_ADDRESS); // 기기 연결시도Z

                BT.setBluetoothConnectionListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
                    public void onDeviceConnected(String name, String address) {
                        Toast.makeText(getApplicationContext(), "블루투스2 연결완료 " + name + "\n" + address, Toast.LENGTH_SHORT).show();

                        editText_BT.setTextColor(Color.BLUE);
                        editText_BT.setText( "블루투스2 연결완료 \n" + address);
                        // 블루투스 발신, 물건이 투입 됐는지?에 대한 리퀘스트
                       BT.send("RefillReady", false); // CR(Carriage Return):\r , LF(Line Feed):\n

                    }

                    public void onDeviceDisconnected() { //연결해제
                        Toast.makeText(getApplicationContext(), "블루투스2 연결해제 ", Toast.LENGTH_SHORT).show();

                        editText_BT.setText("블루투스2 연결해제");
                    }

                    public void onDeviceConnectionFailed() { //연결실패
                        //Toast.makeText(getApplicationContext(), "블루투스2 연결실패,\n 재시도중..", Toast.LENGTH_SHORT).show();
                        BT.connect(MAC_ADDRESS);
                        editText_BT.setTextColor(Color.RED);

                        editText_BT.setText("블루투스2 연결실패,\n 재시도중..");
                    }
                });

            }
        }

        BT.setOnDataReceivedListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {

                Log.d("TAG", "RECEiVE DATA2 " + message);

                if(message.equals("ADD500") ) // 배출 완료됬다라는 메세지
                {
                    JSONObject sendData = new JSONObject();
                    try {
                        sendData.put("deviceID", deviceID);
                        socket.emit("ADD500", sendData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(RefillCoin.this, "500원 +25개 리필완료", Toast.LENGTH_SHORT).show();
                }
                else if(message.equals("ADD100") ){
                    JSONObject sendData = new JSONObject();
                    try {
                        sendData.put("deviceID", deviceID);
                        socket.emit("ADD100", sendData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(RefillCoin.this, "100원 +25개 리필완료", Toast.LENGTH_SHORT).show();
                }
                else if(message.equals("ADD50") ){
                    JSONObject sendData = new JSONObject();
                    try {

                        sendData.put("deviceID", deviceID);
                        socket.emit("ADD50", sendData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(RefillCoin.this, "50원 +25개 리필완료", Toast.LENGTH_SHORT).show();
                }
                else if(message.equals("ADD10") ){
                    JSONObject sendData = new JSONObject();
                    try {

                        sendData.put("deviceID", deviceID);
                        socket.emit("ADD10", sendData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(RefillCoin.this, "10원 +25개 리필완료", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                BT.setupService();
                BT.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth was not enabled.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BT.stopService(); //블루투스 중지
    }

    @Override
    public void onBackPressed(){
        // 안드로이드 백버튼 막기
        return;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
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