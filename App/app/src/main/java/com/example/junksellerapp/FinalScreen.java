package com.example.junksellerapp;

import androidx.annotation.BinderThread;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junksellerapp.step.step5_setPrice;

import butterknife.BindView;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;
import ru.github.igla.ferriswheel.FerrisWheelView;

public class FinalScreen extends AppCompatActivity {

    private TextView FinalTitle;
    private FerrisWheelView ferrisWheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
        WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        setContentView(R.layout.activity_final_screen);

        ferrisWheelView = findViewById(R.id.ferrisWheelView);
        ferrisWheelView.startAnimation();

        final KonfettiView konfettiView = findViewById(R.id.viewKonfetti);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");
        FinalTitle = findViewById(R.id.FinalTitle);

        FinalTitle.setTypeface(typeface); // 시작하기 버튼 폰트 변

        konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 1350f, -50f, -50f)
                .streamFor(300, 5000L);

        Toast.makeText(this, "5초뒤 메인화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
        Handler delayHandler = new Handler();

        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                finish();
                Intent intent = new Intent(FinalScreen.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거


            }
        }, 8000); // 8초뒤 초기화면


    }

    @Override
    public void onBackPressed(){
        // 안드로이드 백버튼 막기
        return;

    }
}
