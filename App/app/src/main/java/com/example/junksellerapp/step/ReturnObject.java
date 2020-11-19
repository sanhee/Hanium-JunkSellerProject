package com.example.junksellerapp.step;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.example.junksellerapp.MainActivity;
import com.example.junksellerapp.R;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReturnObject extends AppCompatActivity implements OnProgressBarListener {
    public Timer timer;
    private NumberProgressBar bnp;
    private boolean InsertCheck = false; // 사용 가능한지에 대한 체크문
    private String ReturnReason = "";
    @BindView(R.id.editText) TextView editText;
    @BindView(R.id.stepTitle) TextView stepTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_object);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
        WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        ButterKnife.bind(this);  // 버터나이프

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");
        stepTitle.setTypeface(typeface2);

        Intent intent = getIntent();
        ReturnReason = intent.getExtras().getString("리턴사유"); // 수용량체크 받아오기

        if(ReturnReason != null) {
            if (ReturnReason.equals("무게비정상")) {
                stepTitle.setText("해당 물품의 무게가 비정상으로 판단됨.");
            } else {
                String str = ReturnReason + " 의 보관함이 가득차, 판매 불가능";
                stepTitle.setText(str);
            }
        }
        bnp = (NumberProgressBar)findViewById(R.id.number_progress_bar);
        bnp.setOnProgressBarListener(this);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(20);

                        if(InsertCheck==true){

                            if(timer!=null){
                                timer.cancel(); //스케쥴task과 타이머를 취소한다.
                                timer.purge(); //task큐의 모든 task를 제거한다.
                                timer=null;
                            }

                            Intent intent = new Intent(ReturnObject.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0,0); // 액티비티 전환 애니메이션 제거
                            finish();

                        }


                    }
                });
            }
        }, 100,50); // 총 30초

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


}
