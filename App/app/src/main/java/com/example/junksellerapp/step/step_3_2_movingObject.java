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

import java.util.Timer;
import java.util.TimerTask;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class step_3_2_movingObject extends AppCompatActivity implements OnProgressBarListener {


    public Timer timer;
    private NumberProgressBar bnp;
    @BindView(R.id.btnMove2)
    Button btnMove2;


    private boolean WeightCheck = false;
    private double WeightValue = 0;
    @BindView(R.id.editText)
    TextView editText;
    @BindView(R.id.step_move_Title)
    TextView step_move_Title;
    private String ObjectName = "";


    // 블루투스 통신 관련
    private app.akexorcist.bluetotohspp.library.BluetoothSPP BT  = new app.akexorcist.bluetotohspp.library.BluetoothSPP(this); //Initializing;
    private String MAC_ADDRESS = "00:18:E4:35:56:7F";
    //private String RequestCommand = "IsWeight";


    // 블루투스 통신 관련
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        setContentView(R.layout.activity_step_3_2_moving_object);
        ButterKnife.bind(this);  // 버터나이프
        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");
        step_move_Title.setTypeface(typeface2);

        Intent intent = getIntent();
        ObjectName = intent.getExtras().getString("품명"); // 품목 정보 받아오기
        WeightValue = intent.getExtras().getDouble("무게"); // 품목 정보 받아오기

        Log.d("TAG", "3_2 ObjectName: "+ObjectName);

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

                        if( WeightCheck == true )
                        {
                            if(timer!=null){
                                timer.cancel(); //스케쥴task과 타이머를 취소한다.
                                timer.purge(); //task큐의 모든 task를 제거한다.
                                timer=null;

                                Toast.makeText(step_3_2_movingObject.this, "운반완료", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(step_3_2_movingObject.this, step4_carPrice.class);
                                intent.putExtra("품명",ObjectName);
                                intent.putExtra("무게",WeightValue);
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

    @OnClick(R.id.btnMove2)
    void onButtonClicked() {
        if(timer!=null){
            timer.cancel(); //스케쥴task과 타이머를 취소한다.
            timer.purge(); //task큐의 모든 task를 제거한다.
            timer=null;
        }

        WeightCheck = true;
        Toast.makeText(this, "운반완료", Toast.LENGTH_SHORT).show();


        //WeightValue = 100.0;
        BT.send(ObjectName, false); // CR(Carriage Return):\r , LF(Line Feed):\n



        Intent intent = new Intent(this, step4_carPrice.class);

        intent.putExtra("품명",ObjectName);
        intent.putExtra("무게",WeightValue);

        startActivity(intent);
        overridePendingTransition(0,0); // 액티비티 전환 애니메이션 제거
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

                        //Handler delayHandler = new Handler();
                        //delayHandler.postDelayed(new Runnable() {
                           // @Override
                           // public void run() {

                                // 블루투스 발신, 물건이 투입 됐는지?에 대한 리퀘스트
                                BT.send(ObjectName, false); // CR(Carriage Return):\r , LF(Line Feed):\n

                           // }
                       // }, 15000);  // 블루투스 연결이되고 5초후 IsWeight를 보냄. (* 아두이노가 라즈베리에서 정보를 받아오는 시간이 필요하기 때문임. )


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

                 if(message.equals("END_DEV") )
                 {
                     Log.d("TAG", "RECEiVE DATA(IsWeight) " + message);
                     WeightCheck = true;
                     //WeightValue = Integer.parseInt(message);
                 }
                //else if(message.equals("Unavailable") ){
                //   Log.d("TAG", "RECEiVE DATA3 " + message);
                //WeightCheck = false;
                //  WeightValue = 0;
                // }
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
