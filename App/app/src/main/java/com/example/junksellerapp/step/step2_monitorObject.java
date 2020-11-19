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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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

public class step2_monitorObject extends AppCompatActivity implements OnProgressBarListener {
    public Timer timer;
    private NumberProgressBar bnp;

    @BindView(R.id.step_monitor_Img) ImageView step_monitor_Img;
    @BindView(R.id.step_monitor_Img2) ImageView step_monitor_Img2;

    @BindView(R.id.step_monitor_Title) TextView step_monitor_Title;
    @BindView(R.id.editText2) TextView editText2;
    @BindView(R.id.btnMonitor) Button btnMonitor;
    private int monitorCheck = 0; // 모니터링 끝났는지,  0: 라즈베리 x , 1: 특정물품 가득참, 2:인식완료
    private String monitorResult = "";  // 영상정보
    @BindView(R.id.editText) TextView editText;

    // 블루투스 통신 관련
    private app.akexorcist.bluetotohspp.library.BluetoothSPP BT  = new app.akexorcist.bluetotohspp.library.BluetoothSPP(this); //Initializing;
    private String MAC_ADDRESS = "98:D3:41:FD:AA:79";
    private String RequestCommand = "IsObject";

    private String capacityCheck = "";
    // 블루투스 통신 관련
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_object);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
        WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면

        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/koverwatch.ttf");
        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");


        Intent intent = getIntent();
        capacityCheck = intent.getExtras().getString("수용량체크"); // 수용량체크 받아오기
        Log.d("TAG", "받아온 값 capacityCheck: "+capacityCheck);

        ButterKnife.bind(this);  // 버터나이프

        btConnect(); // 블루투스 연결


        bnp = (NumberProgressBar)findViewById(R.id.step_monitor_progress_bar);
        bnp.setOnProgressBarListener(this);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(20);

                        if(monitorCheck==2) {
                            if (timer != null) {
                                timer.cancel(); //스케쥴task과 타이머를 취소한다.
                                timer.purge(); //task큐의 모든 task를 제거한다.
                                timer = null;
                            }

                            Toast.makeText(step2_monitorObject.this, "인식완료", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(step2_monitorObject.this, step3_weightCheck.class);
                            intent.putExtra("품명", monitorResult);
                            startActivity(intent);
                            overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                            finish();

                        }
                        else if(monitorCheck==1) { // 수용량이 꽉찬 물품인 경우
                            if (timer != null) {
                                timer.cancel(); //스케쥴task과 타이머를 취소한다.
                                timer.purge(); //task큐의 모든 task를 제거한다.
                                timer = null;
                            }

                            Toast.makeText(step2_monitorObject.this, "인식완료/ 하지만 보관불가", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(step2_monitorObject.this, ReturnObject.class);
                            intent.putExtra("리턴사유",monitorResult);
                            startActivity(intent);
                            overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                            finish();

                        }
                    }
                });
            }
        }, 100, 300);

        step_monitor_Title.setTypeface(typeface2);
    }


    @OnClick(R.id.btnMonitor) // 버튼으로 강제 스킵 할 경우
    void onButtonClicked() {
        monitorResult = "can";
       // monitorCheck = 1;
        monitorCheck = 2;
        Toast.makeText(this, "스킵완료, 강제인식값="+monitorResult, Toast.LENGTH_SHORT).show();
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
            if (monitorCheck == 0) {
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
                        //Toast.makeText(getApplicationContext(), "블루투스3 연결완료 " + name + "\n" + address, Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.BLUE);
                        editText.setText( "블루투스3 연결완료 \n" + address);

                        Handler delayHandler = new Handler();
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // 블루투스 발신, 물건이 투입 됐는지?에 대한 리퀘스트
                                BT.send(RequestCommand, false); // CR(Carriage Return):\r , LF(Line Feed):\n

                            }
                        }, 5000); //블루투스 연결이되고 5초후 IsObject를 보냄. (* 아두이노가 라즈베리에서 정보를 받아오는 시간이 필요하기 때문임. )

                    }

                    public void onDeviceDisconnected() { //연결해제
                        //Toast.makeText(getApplicationContext(), "블루투스3 연결해제 ", Toast.LENGTH_SHORT).show();
                        editText.setText("블루투스3 연결해제");
                    }

                    public void onDeviceConnectionFailed() { //연결실패
                        //Toast.makeText(getApplicationContext(), "블루투스3 연결실패,\n 재시도중..", Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.RED);
                        editText.setText("블루투스3 연결실패,\n 재시도중..");
                        BT.connect(MAC_ADDRESS);
                    }
                });

            }
        }

        BT.setOnDataReceivedListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 이 곳에 UI작업을 한다
                        step_monitor_Title.setText("2. 카메라로 인식중 입니다.");
                        step_monitor_Img.setVisibility(View.INVISIBLE);
                        step_monitor_Img2.setVisibility(View.VISIBLE);

                    }
                });

                editText2.setText("RECEiVE DATA : " + message);

                Log.d("TAG", "RECEiVE DATA2 All " + message);

                if(message.equals("unuse_raspberry") ){
                    Log.d("TAG", "RECEiVE DATA2 " + message);
                    monitorCheck = 0;
                    monitorResult = message;
                }
                else if(message.equals("can") || message.equals("soju") || message.equals("makju") || message.equals("book"))
                {
                    String[] array = {"can","soju","makju","book"};

                    int cnt=0;

                    for(int cnt_sub=0;cnt_sub<4;cnt_sub++) // 인식된 물체의 배열 번호를 확인하는 과정
                    {
                        if(message.equals(array[cnt_sub]))
                        {
                            cnt = cnt_sub;
                            Log.d("TAG", "step2 cnt: "+cnt);
                            Log.d("TAG", "step2 cnt_sub: "+cnt_sub);
                            break;
                        }
                    }
                    Log.d("TAG", "step2 cnt2: "+cnt);
                    Log.d("TAG", "capacityCheck.charAt(cnt): "+capacityCheck.charAt(cnt));
                    if(capacityCheck.charAt(cnt) == '0') {
                        monitorCheck = 2;
                        monitorResult = message;
                    }
                    else if (capacityCheck.charAt(cnt) == '1') // 품목의 보관함이 찼을 경우
                    {
                        monitorCheck = 1;
                        monitorResult = message;
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

    public String returnObject(){
        return monitorResult;
    }
}
