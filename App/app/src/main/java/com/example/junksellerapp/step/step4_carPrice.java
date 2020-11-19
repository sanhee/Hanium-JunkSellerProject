package com.example.junksellerapp.step;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.example.junksellerapp.MainActivity;
import com.example.junksellerapp.R;
import com.example.junksellerapp.database.AppDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class step4_carPrice extends AppCompatActivity implements OnProgressBarListener {
    public Timer timer;
    private NumberProgressBar bnp;
    Button btnCal;
    private boolean CalChcek = false;

    // 소켓통신
    public String serverURL = "http://103.124.101.163:3000/";
    Socket socket;


    private double weightValue;
    private  String deviceID = "";
    private String objectName;
    private int objectPrice;
    @BindView(R.id.step_cal_Title)
    TextView step_cal_Title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal__price);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
        WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        ButterKnife.bind(this);  // 버터나이프

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");
        step_cal_Title.setTypeface(typeface2);

        Intent intent = getIntent();
        weightValue = intent.getExtras().getDouble("무게");  // 무게정보
        objectName =  intent.getExtras().getString("품명"); // 물체정보
        Log.d("TAG", "step4 weightValue: "+weightValue+ " objectName: "+objectName);



        bnp = (NumberProgressBar)findViewById(R.id.step_cal_progress_bar);
        bnp.setOnProgressBarListener(this);

        // Room 라이브러리 (db)
        AppDatabase db = Room.databaseBuilder(this,AppDatabase.class,"device-db")
                .allowMainThreadQueries()
                .build();

        deviceID = db.todoDao().getAll().get(0).getTitle();


        InitialSocket();

        JSONObject sendData = new JSONObject();
        try {

            sendData.put("objectName", objectName);
            sendData.put("deviceID", deviceID);
            Log.d("TAG","objectName = "+objectName);
            socket.emit("CHECK_PRICE", sendData);

        } catch (JSONException e) {
            e.printStackTrace();
        }




       // objectInfo = new HashMap<>();

       // objectInfo.put("타입",objectName);
       // objectInfo.put("무게",weightValue);

        Log.d("TAG","타입:"+objectName+" 무게:"+weightValue);


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(1);

                        if(CalChcek==true) {
                            if (timer != null) {
                                timer.cancel(); //스케쥴task과 타이머를 취소한다.
                                timer.purge(); //task큐의 모든 task를 제거한다.
                                timer = null;
                            }

                            Toast.makeText(step4_carPrice.this, "계산완료", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(step4_carPrice.this, step5_setPrice.class);
                            intent.putExtra("품명", objectName);
                            intent.putExtra("무게", weightValue);
                            intent.putExtra("가격", objectPrice);
                            startActivity(intent);
                            overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                            finish();

                        }

                    }
                });
            }
        }, 100, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer!=null) {
            timer.cancel();
        }
    }


    public void onProgressChange(int current, int max) {
        if(current == max) {
            if (!CalChcek) {
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


    public void InitialSocket(){

        try {
            socket = IO.socket(serverURL);
        }catch (Exception e) {
            e.printStackTrace();
        }
        socket.connect(); // 소켓통신 시작
        socket.on("RECEIVE_PRICE",RECEIVE_PRICE);

    }

    private Emitter.Listener RECEIVE_PRICE = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            String data = args[0].toString();
            objectPrice = Integer.parseInt(data);
            Log.d("TAG",  "계산된 가격: "+ objectPrice);

            Handler delayHandler1 = new Handler(Looper.getMainLooper());
            delayHandler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CalChcek = true;

                }
            }, 3000); // 계산완료 3초뒤 화면전환

        }
    };








}
