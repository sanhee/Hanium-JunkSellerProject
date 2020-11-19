package com.example.junksellerapp.step;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junksellerapp.MainActivity;
import com.example.junksellerapp.R;
import com.nihaskalam.progressbuttonlibrary.CircularProgressButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class step5_setPrice extends AppCompatActivity {


    CircularProgressButton bt;

    @BindView(R.id.objectType1) TextView objectType1;
    @BindView(R.id.objectWeight1) TextView objectWeight1;
    @BindView(R.id.objectPrice1) TextView objectPrice1;

    @BindView(R.id.objectName) TextView objectName;
    @BindView(R.id.objectWeight2) TextView objectWeight2;
    @BindView(R.id.objectPrice2) TextView objectPrice2;

    private String objectTypeOri = "";
    private String objectType = "";
    private double objectWeight = 0;
    private int objectPrice = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_price);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
        WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        ButterKnife.bind(this);  // 버터나이프

        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");
        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/kostar.ttf");
        objectType1.setTypeface(typeface2);
        objectWeight1.setTypeface(typeface2);
        objectPrice1.setTypeface(typeface2);
        objectName.setTypeface(typeface2);
        objectWeight2.setTypeface(typeface2);
        objectPrice2.setTypeface(typeface2);

        Intent intent = getIntent();

        objectTypeOri = intent.getExtras().getString("품명");

        Log.d("TAG", "step5 objectTypeOri: "+objectTypeOri);

        objectWeight = intent.getExtras().getDouble("무게");
        objectPrice = intent.getExtras().getInt("가격");
        Log.d("TAG", "step5 objectType: "+objectType+ " objectWeight: "+objectWeight + " objectPrice " + objectPrice);



        if (objectTypeOri.equals("soju")){
            objectType = "소주병";
        }
        else if (objectTypeOri.equals("makju")){
            objectType = "맥주병";
        }
        else if (objectTypeOri.equals("can")){
            objectType = "캔";
        }
        else if (objectTypeOri.equals("book")){
            objectType = "책";
        }

        objectName.setText("\t"+objectType); // 물체정보
        objectWeight2.setText("\t"+Double.toString(objectWeight)+" g");  // 무게정보
        objectPrice2.setText("\t"+Integer.toString(objectPrice)+" 원");  // 가격정보

        bt=(CircularProgressButton) findViewById(R.id.btnExchange);
        bt.setTypeface(typeface);


        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //진행도를 로딩형식으로 설정
                bt.setIndeterminateProgressMode(true);
                // 로딩의 시작을 알림
                bt.setProgress(1);

                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        bt.setProgress(100);


                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(step5_setPrice.this, step6_DropCoin.class);
                                intent.putExtra("품명",objectTypeOri);
                                intent.putExtra("가격",objectPrice);
                                startActivity(intent);
                                overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                                finish();
                            }
                        }, 1000); // 1초뒤 액티비티 자동전환



                    }
                }, 2000); //버튼 클릭후, 2초가 됬을 완료 상태가 됨.




            }

        });


    }



    @Override
    public void onBackPressed(){
        // 안드로이드 백버튼 막기
        return;

    }

}
