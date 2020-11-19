package com.example.junksellerapp.step;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.example.junksellerapp.FinalScreen;
import com.example.junksellerapp.MainActivity;
import com.example.junksellerapp.R;
import com.example.junksellerapp.database.AppDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class step6_DropCoin extends AppCompatActivity implements OnProgressBarListener {

    public Timer timer;
    private NumberProgressBar bnp;
    @BindView(R.id.btnDrop)  Button btnDrop;
    private boolean InsertCheck = false;

    @BindView(R.id.editText) TextView editText;
    @BindView(R.id.step_drop_Title) TextView step_drop_Title;

    // 소켓통신
    public String serverURL = "http://103.124.101.163:3000/";
    Socket socket;

    // 동전분리기
    private int[] haveCoin = { 0,0,0,0 };
    private int[] arrayDropCoin = { 0,0,0,0 };
    private int[] priceDivide_temp = { 0,0 };
    private int checkCoin = 0;
    private int objectPrice = 0;
    private  String deviceID = "";
    private String objectName = "";


    // 블루투스 통신 관련
    private app.akexorcist.bluetotohspp.library.BluetoothSPP BT  = new app.akexorcist.bluetotohspp.library.BluetoothSPP(this); //Initializing;
    private String MAC_ADDRESS = "98:D3:11:F8:1C:CC";
    private String RequestCommand = "";
    // 블루투스 통신 관련

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_coin);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
        WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        ButterKnife.bind(this);  // 버터나이프

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");
        step_drop_Title.setTypeface(typeface2);

        // Room 라이브러리 (db)
        AppDatabase db = Room.databaseBuilder(this,AppDatabase.class,"device-db")
                .allowMainThreadQueries()
                .build();

        Intent intent = getIntent();
        deviceID = db.todoDao().getAll().get(0).getTitle();
        objectName = intent.getExtras().getString("품명");  // 가격정보 불러옴.
        objectPrice = intent.getExtras().getInt("가격");  // 가격정보 불러옴.



        InitialSocket(); // 소켓통신 연결, 이벤트리스너 등 함수화

        JSONObject sendData = new JSONObject();
        try {

            sendData.put("objectName", objectName);
            sendData.put("deviceID", deviceID);
            Log.d("TAG","objectName = "+objectName);
            socket.emit("CHECK_PRICE", sendData);

        } catch (JSONException e) {
            e.printStackTrace();
        }



        bnp = (NumberProgressBar)findViewById(R.id.step_drop_progress_bar);
        bnp.setOnProgressBarListener(this);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(20);

                        if (InsertCheck == true) {
                            if (timer != null) {
                                timer.cancel(); //스케쥴task과 타이머를 취소한다.
                                timer.purge(); //task큐의 모든 task를 제거한다.
                                timer = null;
                            }

                            Toast.makeText(step6_DropCoin.this, "배출완료", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(step6_DropCoin.this, FinalScreen.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                            finish();
                        }

                    }
                });
            }
        }, 100, 300);

    }

    @OnClick(R.id.btnDrop)
    void onButtonClicked() {
        if(timer!=null){
            timer.cancel(); //스케쥴task과 타이머를 취소한다.
            timer.purge(); //task큐의 모든 task를 제거한다.
            timer=null;
        }

        InsertCheck = true;
        Toast.makeText(this, "배출완료", Toast.LENGTH_SHORT).show();

        JSONObject sendData = new JSONObject();

        try {
            sendData.put("deviceID", deviceID); // 기기 아이디
            sendData.put("objectName", objectName); // 기기 아이디
            sendData.put("a", haveCoin[0]);     // a: 500원 개수
            sendData.put("b", haveCoin[1]);     // b: 100원 개수
            sendData.put("c", haveCoin[2]);     // c:  50원 개수
            sendData.put("d", haveCoin[3]);     // d:  10원 개수
            socket.emit("SEND_HM_LOG", sendData);


        } catch (JSONException e) {
            e.printStackTrace();
        }



        Intent intent = new Intent(this, FinalScreen.class);
        startActivity(intent);
        overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer!=null) {
            timer.cancel();
        }
        BT.stopService(); //블루투스 중지
    }


    public void onProgressChange(int current, int max) {
        if(current == max) {
            if (InsertCheck == false) {
                Toast.makeText(this, "제한시간이 초과되었습니다. 다시시도 해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
            }

        }
    }

    @Override
    public void onBackPressed(){
        // 안드로이드 백버튼 막기
        return;

    }

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

                BT.connect(MAC_ADDRESS); // 기기 연결시도

                BT.setBluetoothConnectionListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
                    public void onDeviceConnected(String name, String address) {
                        //Toast.makeText(getApplicationContext(), "블루투스2 연결완료 " + name + "\n" + address, Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.BLUE);
                        editText.setText( "블루투스2 연결완료 \n" + address);
                        // 블루투스 발신, 물건이 투입 됐는지?에 대한 리퀘스트
                        BT.send("DropCoin:"+arrayDropCoin[0]+","+arrayDropCoin[1]+","+arrayDropCoin[2]+","+arrayDropCoin[3], false); // CR(Carriage Return):\r , LF(Line Feed):\n
                    }

                    public void onDeviceDisconnected() { //연결해제
                        //Toast.makeText(getApplicationContext(), "블루투스2 연결해제 ", Toast.LENGTH_SHORT).show();
                        editText.setText("블루투스2 연결해제");
                    }

                    public void onDeviceConnectionFailed() { //연결실패
                        //Toast.makeText(getApplicationContext(), "블루투스2 연결실패,\n 재시도중..", Toast.LENGTH_SHORT).show();
                        BT.connect(MAC_ADDRESS);
                        editText.setTextColor(Color.RED);
                        editText.setText("블루투스2 연결실패,\n 재시도중..");
                    }
                });

            }
        }

        BT.setOnDataReceivedListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {

                Log.d("TAG", "RECEiVE DATA2 " + message);

                if(message.equals("Done") ) // 배출 완료됬다라는 메세지
                {
                    JSONObject sendData = new JSONObject();
                    try {
                        sendData.put("deviceID", deviceID); // 기기 아이디
                        sendData.put("objectName", objectName); // 기기 아이디
                        sendData.put("a", haveCoin[0]);     // a: 500원 개수
                        sendData.put("b", haveCoin[1]);     // b: 100원 개수
                        sendData.put("c", haveCoin[2]);     // c:  50원 개수
                        sendData.put("d", haveCoin[3]);     // d:  10원 개수
                          socket.emit("SEND_HM_LOG", sendData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    InsertCheck = true;

                }
                else if(message.equals("Error") ){
                    Log.d("TAG", "RECEiVE DATA2 " + message);
                    InsertCheck = false;
                }
            }

        });

    }

    public void onDivideCoin(int dropCoin){

        Log.d("TAG", "[** 소지금] 500원 - "+ haveCoin[0] +" 개, 100원 - "+ haveCoin[1] +" 개, 50원 - "+ haveCoin[2] +" 개, 10원 - "+ haveCoin[3]+"\n");

        priceDivide_temp[0] = dropCoin / 500;
        priceDivide_temp[1] = dropCoin % 500;


        if (priceDivide_temp[0] > 0) {

            if (haveCoin[0] >= priceDivide_temp[0])
            {
                arrayDropCoin[0] = priceDivide_temp[0];
                haveCoin[0] = haveCoin[0] - priceDivide_temp[0];
                dropCoin = dropCoin - (arrayDropCoin[0] * 500); // 나머지 금액 정보를 변환후 temp[0]에 저장
                checkCoin = 1;
            }
            else if (haveCoin[0] < priceDivide_temp[0]) // 가지고 있는 금액보다 큰 경우는
            {

                if (haveCoin[0] == 0) // 소유한 500원이 아예 없을 경우.
                {
                    arrayDropCoin[0] = 0;
                    checkCoin = 1;
                }
                else if(haveCoin[0] != 0) { // 가지고 있는 500원의 금액은 부족하지만 0원은 아닌 경우

                    arrayDropCoin[0] = haveCoin[0]; // 가지고있는 모든 500원을 temp[0]에 저장.
                    haveCoin[0] = 0;

                    dropCoin = dropCoin-(arrayDropCoin[0]*500); // 나머지 금액 정보를 변환후 temp[0]에 저장
                    checkCoin = 1;
                }
            }

        }

        if ( priceDivide_temp[0]==0 || checkCoin == 1) {

            priceDivide_temp[0] = dropCoin / 100;
            priceDivide_temp[1] = dropCoin % 100;

            if (haveCoin[1] >= priceDivide_temp[0])
            {
                arrayDropCoin[1] = priceDivide_temp[0];
                haveCoin[1] = haveCoin[1] - priceDivide_temp[0];
                dropCoin = dropCoin - (arrayDropCoin[1] * 100); // 나머지 금액 정보를 변환후 temp[0]에 저장
                checkCoin = 2;
            }
            else if (haveCoin[1] < priceDivide_temp[0]) // 가지고 있는 금액보다 큰 경우는
            {
                if (haveCoin[1] == 0) // 소유한 100원이 아예 없을 경우.
                {
                    arrayDropCoin[1] = 0;
                    checkCoin = 2;
                }
                else if(haveCoin[1] != 0) { // 가지고 있는 100원의 금액은 부족하지만 0원은 아닌 경우

                    arrayDropCoin[1] = haveCoin[1]; // 가지고있는 모든 500원을 temp[0]에 저장.
                    haveCoin[0] = 0;

                    dropCoin = dropCoin - (arrayDropCoin[1] * 100); // 나머지 금액 정보를 변환후 temp[0]에 저장
                    checkCoin = 2;
                }
            }

        }

        if ( priceDivide_temp[0]==0  || checkCoin == 2 ){
            priceDivide_temp[0] = dropCoin / 50;
            priceDivide_temp[1] = dropCoin % 50;

            if (haveCoin[2] >= priceDivide_temp[0])
            {
                arrayDropCoin[2] = priceDivide_temp[0];
                haveCoin[2] = haveCoin[2] - priceDivide_temp[0];
                dropCoin = dropCoin - (arrayDropCoin[2] * 50); // 나머지 금액 정보를 변환후 temp[0]에 저장
                checkCoin = 3;
            }
            else if (haveCoin[2] < priceDivide_temp[0]) // 가지고 있는 금액보다 큰 경우는
            {
                if (haveCoin[2] == 0) // 소유한 50원이 아예 없을 경우.
                {
                    arrayDropCoin[2] = 0;
                    checkCoin = 3;
                }
                else if(haveCoin[2] != 0) { // 가지고 있는 50원의 금액은 부족하지만 0원은 아닌 경우

                    arrayDropCoin[2] = haveCoin[2]; // 가지고있는 모든 50을 temp[0]에 저장.
                    haveCoin[2] = 0;

                    dropCoin = dropCoin - (arrayDropCoin[2] * 50); // 나머지 금액 정보를 변환후 temp[0]에 저장
                    checkCoin = 3;
                }
            }
        }

        if ( priceDivide_temp[0]==0  || checkCoin == 3)
        {
            priceDivide_temp[0] = dropCoin / 10;
            priceDivide_temp[1] = dropCoin % 10;

            if (haveCoin[3] >= priceDivide_temp[0])
            {
                arrayDropCoin[3] = priceDivide_temp[0];
                haveCoin[3] = haveCoin[3] - priceDivide_temp[0];
            }
            else if (haveCoin[3] < priceDivide_temp[0]) // 가지고 있는 금액보다 큰 경우는
            {
                if (haveCoin[3] == 0) // 소유한 50원이 아예 없을 경우.
                {
                    arrayDropCoin[3] = 0;
                    checkCoin = 4;
                }
                else if(haveCoin[3] != 0) { // 가지고 있는 50원의 금액은 부족하지만 0원은 아닌 경우

                    arrayDropCoin[3] = haveCoin[3]; // 가지고있는 모든 10을 temp[0]에 저장.
                    haveCoin[3] = 0;
                    dropCoin = dropCoin - (arrayDropCoin[3] * 10); // 나머지 금액 정보를 변환후 temp[0]에 저장

                    checkCoin = 4;
                }
            }
        }

        if (checkCoin == 4)
        {
            Log.d("TAG", "[** 배출할 동전이 부족합니다. ]");
        }
        if (checkCoin != 4)
        {
            Log.d("TAG", "[** 계산할 돈] 500원 - "+ arrayDropCoin[0] +" 개, 100원 - "+ arrayDropCoin[1] +" 개, 50원 - "+ arrayDropCoin[2] +" 개, 10원 - "+ arrayDropCoin[3]+"\n");
            Log.d("TAG", "[** 남은 돈] 500원 - "+ haveCoin[0] +" 개, 100원 - "+ haveCoin[1] +" 개, 50원 - "+ haveCoin[2] +" 개, 10원 - "+ haveCoin[3]+"\n");

        }



    }


    public void InitialSocket(){

        try {
            socket = IO.socket(serverURL);
        }catch (Exception e) {
            e.printStackTrace();
        }
        socket.connect(); // 소켓통신 시작
        socket.on("RECEIVE_HAVE_MONEY",RECEIVE_HAVE_MONEY);
        socket.on("RECEIVE_PRICE",RECEIVE_PRICE);

    }



    private Emitter.Listener RECEIVE_PRICE = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String data = args[0].toString();
            objectPrice = Integer.parseInt(data);
            Log.d("TAG",  "계산된 가격: "+ objectPrice);
        }
    };


    private Emitter.Listener RECEIVE_HAVE_MONEY = new Emitter.Listener() {
        @Override
        public void call(Object... args2) {

            Log.d("TAG", "step6:: args[0].toString() "+ args2[0].toString());
            try
            {
                JSONObject jsonObject = new JSONObject(args2[0].toString());


                haveCoin[0] = Integer.parseInt(jsonObject.getString("a"));
                haveCoin[1] = Integer.parseInt(jsonObject.getString("b"));
                haveCoin[2] = Integer.parseInt(jsonObject.getString("c"));
                haveCoin[3] = Integer.parseInt(jsonObject.getString("d"));


            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Log.d("TAG","에러: "+ e.toString());
            }

            for(int i=0 ;i<4;i++)
            {

                Log.d("TAG",  "haveCoin["+i+"]: "+ haveCoin[i]);
            }

            onDivideCoin(objectPrice);

            RequestCommand = "DropCoin:"+arrayDropCoin[0]+","+arrayDropCoin[1]+","+arrayDropCoin[2]+","+arrayDropCoin[3]; // 환전해야할 동전 리퀘스트 ( 500/100/50/10 )
            Log.d("TAG", "[동전 분리기로 명령어 전송] => " + RequestCommand);

            btConnect(); // 블루투스 연결됨과 동시에 가격 명령어 전송


        }
    };



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
}
