package com.example.junksellerapp.step;

import androidx.appcompat.app.AppCompatActivity;

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
import com.example.junksellerapp.MainActivity;
import com.example.junksellerapp.R;

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

public class step3_weightCheck extends AppCompatActivity implements OnProgressBarListener  {

    public Timer timer;
    private NumberProgressBar bnp;
    @BindView(R.id.btnMove) Button btnMove;
    @BindView(R.id.step_move_Title) TextView step_move_Title;
    @BindView(R.id.editTextWeight) TextView editTextWeight;

    private String  WeightValid = ""; // 무게값이 유효한지
    private boolean WeightCheck = false; // 무게인식이 됬는지?
    private double WeightValue = 0.0;
    @BindView(R.id.editText) TextView editText;
    private String ObjectName = "";

    // 소켓통신
    public String serverURL = "http://103.124.101.163:3000/";
    Socket socket;

    // 블루투스 통신 관련
    private app.akexorcist.bluetotohspp.library.BluetoothSPP BT  = new app.akexorcist.bluetotohspp.library.BluetoothSPP(this); //Initializing;
    private String MAC_ADDRESS = "98:D3:41:FD:AA:79";
    private String RequestCommand = "IsWeight";


    // 블루투스 통신 관련
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
        WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        setContentView(R.layout.activity_weight_check);
        ButterKnife.bind(this);  // 버터나이프

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");
        step_move_Title.setTypeface(typeface2);

        Intent intent = getIntent();
        ObjectName = intent.getExtras().getString("품명"); // 품목 정보 받아오기

        InitialSocket(); // 소켓통신 연결, 이벤트리스너 등 함수화
        btConnect(); // 블루투스 연결

        bnp = (NumberProgressBar)findViewById(R.id.step_move_progress_bar);
        bnp.setOnProgressBarListener(this);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(20);
                        Log.d("TAG", "step3 WeightValid: "+WeightValid);
                        if( WeightCheck && WeightValid.equals("true"))
                        {
                            if(timer!=null){
                                timer.cancel(); //스케쥴task과 타이머를 취소한다.
                                timer.purge(); //task큐의 모든 task를 제거한다.
                                timer=null;

                                Toast.makeText(step3_weightCheck.this, "측정완료", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(step3_weightCheck.this, step_3_2_movingObject.class);
                                intent.putExtra("품명",ObjectName);
                                intent.putExtra("무게",WeightValue);
                                startActivity(intent);
                                overridePendingTransition(0,0); // 액티비티 전환 애니메이션 제거
                                finish();
                            }

                        }
                        else if ( !WeightCheck && WeightValid.equals("false") ) // 무게인식이 됬지만, 유효하지 않은 무게일때
                        {
                            Log.d("TAG", "step3 WeightValid: "+WeightValid);
                            if(timer!=null){
                                timer.cancel(); //스케쥴task과 타이머를 취소한다.
                                timer.purge(); //task큐의 모든 task를 제거한다.
                                timer=null;

                                Toast.makeText(step3_weightCheck.this, "무게 비정상", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(step3_weightCheck.this, ReturnObject.class);
                                intent.putExtra("품명",ObjectName);
                                intent.putExtra("무게",WeightValue);
                                intent.putExtra("리턴사유","무게비정상");
                                startActivity(intent);
                                overridePendingTransition(0,0); // 액티비티 전환 애니메이션 제거
                                finish();
                            }

                        }

                    }
                });
            }
        }, 100, 300);

    }

    @OnClick(R.id.btnMove)
    void onButtonClicked() {

        WeightValue = 400;
        WeightCheck = true;
        WeightValid = "true";
        //WeightValid = "false";

        Toast.makeText(this, "스킵완료, 품목:"+ ObjectName+" 무게:"+WeightValue, Toast.LENGTH_SHORT).show();

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
            if (WeightCheck == false) {
                Toast.makeText(this, "제한시간이 초과되었습니다. 다시시도 해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
            }

        }
    }

    public void InitialSocket(){

        try {
            socket = IO.socket(serverURL);
        }catch (Exception e) {
            e.printStackTrace();
        }
        socket.connect(); // 소켓통신 시작

        socket.on("ValidWeight", ValidWeight);  // EVENT_CONNECT = connect, 접속했을때
    }

    private Emitter.Listener ValidWeight = new Emitter.Listener() { // 중복검사
        @Override
        public void call(Object... args) {

            String data = args[0].toString();

            if(data.equals("true")) // 무게값이 정상일 경우
            {
                WeightValid = "true";
                BT.send("Weight_correct", false); // CR(Carriage Return):\r , LF(Line Feed):\n
                Log.d("TAG", "step3 SEND: Weight_correct");
            }
            else if(data.equals("false"))  // 무게값이 비정상일 경우
            {
                WeightValid = "false";
                BT.send("Weight_incorrect", false); // CR(Carriage Return):\r , LF(Line Feed):\n
                Log.d("TAG", "step3 SEND: Weight_incorrect");
            }

        }
    };

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
                        //Toast.makeText(getApplicationContext(), "블루투스4 연결완료 " + name + "\n" + address, Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.BLUE);
                        editText.setText( "블루투스4 연결완료 \n" + address);


                        Handler delayHandler = new Handler();
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // 블루투스 발신, 물건이 투입 됐는지?에 대한 리퀘스트
                                BT.send(RequestCommand, false); // CR(Carriage Return):\r , LF(Line Feed):\n

                            }
                        }, 5000); //블루투스 연결이되고 5초후 IsWeight를 보냄. (* 아두이노가 라즈베리에서 정보를 받아오는 시간이 필요하기 때문임. )



                    }

                    public void onDeviceDisconnected() { //연결해제
                        //Toast.makeText(getApplicationContext(), "블루투스4 연결해제 ", Toast.LENGTH_SHORT).show();
                        editText.setText("블루투스4 연결해제");
                    }

                    public void onDeviceConnectionFailed() { //연결실패
                        //Toast.makeText(getApplicationContext(), "블루투스4 연결실패,\n 재시도중..", Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.RED);
                        editText.setText("블루투스4 연결실패,\n 재시도중..");
                        BT.connect(MAC_ADDRESS);
                    }
                });

            }
        }

        BT.setOnDataReceivedListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {

                Log.d("TAG", "step3 message: "+message);
                if(message.equals("invalid_weight") ){
                    Log.d("TAG", "RECEiVE DATA3 " + message);
                    WeightCheck = false;
                    WeightValue = 0;
               }
                else if(message.equals("SUC_MOVE")) {
                    WeightCheck = true;
                }
                else { // 무게값이 존재할 경우.
                    Log.d("TAG", "RECEiVE DATA(IsWeight) " + message);
                    WeightValue = Math.round(Float.parseFloat(message));
                    String str = Double.toString(WeightValue);
                    editTextWeight.setText("물체무게: "+ str);
                    JSONObject sendData = new JSONObject();
                    try {

                        sendData.put("WeightValue", WeightValue);
                        sendData.put("ObjectName", ObjectName);
                        socket.emit("checkWeight", sendData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    public double returnWeight(){
        return WeightValue;
    }
}
