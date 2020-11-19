package com.example.junksellerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junksellerapp.database.AppDatabase;
import com.example.junksellerapp.database.Todo;
import com.example.junksellerapp.step.RefillCoin;
import com.example.junksellerapp.step.step2_monitorObject;
import com.nihaskalam.progressbuttonlibrary.CircularProgressButton;
import com.sdsmdg.harjot.rotatingtext.RotatingTextWrapper;
import com.sdsmdg.harjot.rotatingtext.models.Rotatable;


import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btnHelp) ImageButton btnHelp;
    @BindView(R.id.btnAdmin) ImageButton btnAdmin;
    @BindView(R.id.editText) TextView editText;
    @BindView(R.id.mainTitle) TextView mainTitle;
    @BindView(R.id.mainTitle2) TextView mainTitle2;

    @BindView(R.id.btnStart) CircularProgressButton btnStart;

    // UUID ( 추후 랜덤으로 만든후 내부 DB에 저장하는 방식을 해야힘 )
    private String deviceID = "";
    private Boolean ID_CHECK = true; // 중복체크를 위한 불린 변수

    // GPS 관련
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private double latitude,longitude;
    private List<Address> addresses;


    // 소켓통신
    public String serverURL = "http://103.124.101.163:3000/";
    Socket socket;

    boolean socket_Valid = false;
    boolean Bluetooth_Valid = false;

    public boolean adminMode = false;
    public boolean adminCheck = false;

    public boolean cnt = false;

    public String HW_state = "Ban";  // 모두 꽉 찼을 때
    //public String HW_state = "Available";  // 모두 비어있어서 이용가능 할 때

    public String HW_Capacity = "0000";  // H/W 수용량, [0000: can,soju,makju,book]

    public int btnState = 0;  // HW_state 값을 따라감.

    // 블루투스 통신 관련
    private app.akexorcist.bluetotohspp.library.BluetoothSPP BT; // 블루투스
    private app.akexorcist.bluetotohspp.library.BluetoothSPP BT2; // 블루투스
    private String MAC_ADDRESS = "00:18:E4:35:56:7F";
   // private String MAC_ADDRESS = "98:D3:21:F4:73:D3";
    private String RequestCommand = "IsAvailable";

    // 블루투스 통신 관련
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  // 전체화면
        WindowManager.LayoutParams.FLAG_FULLSCREEN); // 전체화면
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);  // 버터나이프

        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/koverwatch.ttf");
        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "font/nanumbarungothicbold.ttf");


        /*
        RotatingTextWrapper rotatingTextWrapper = (RotatingTextWrapper) findViewById(R.id.custom_switcher);
        rotatingTextWrapper.setSize(55);
        rotatingTextWrapper.setTypeface(typeface);

        Rotatable rotatable = new Rotatable(Color.parseColor("#FFA036"), 1000, "책", "소주병", "맥주병","캔");
        rotatable.setSize(65);
        rotatable.setAnimationDuration(500);
        rotatable.setTypeface(typeface2);
        rotatable.setInterpolator(new BounceInterpolator());

        rotatingTextWrapper.setContent("잠들어있는 ", rotatable);
        */
        mainTitle.setTypeface(typeface); // 시작하기 버튼 폰트 변
        mainTitle2.setTypeface(typeface); // 시작하기 버튼 폰트 변
        Animation anim = new AlphaAnimation(0.0f,1.0f);
        anim.setDuration(100);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        mainTitle2.startAnimation(anim);


        btnStart.setTypeface(typeface2); // 시작하기 버튼 폰트 변

        BT = new app.akexorcist.bluetotohspp.library.BluetoothSPP(this); //Initializing;
        BT2 = new app.akexorcist.bluetotohspp.library.BluetoothSPP(this); //Initializing;

        // GPS

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        gpsTracker = new GpsTracker(MainActivity.this);

        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();
        getCurrentAddress(latitude,longitude); // 위도,경도를 주소로 변환 시키는 함수

        // Room 라이브러리 (db)
        AppDatabase db = Room.databaseBuilder(this,AppDatabase.class,"device-db")
                .allowMainThreadQueries()
                .build();

        //db.todoDao().delete();


        InitialSocket(); // 소켓통신 연결, 이벤트리스너 등 함수화
        UUID_CREATE_CHECK(db); // 기기 UUID 생성 및 서버 DB와 통신하여 중복체크
        UUID_REGISTER(db); // 기기 UUID가 중복되지 않은경우 서버DB 와 기기 내부DB에 ID정보 등록

        btConnect(); // 블루투스 연결
        btConnect2(); // 블루투스 연결

        btReceive(); // 블루투스 데이터 수신

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                    //if(socket_Valid &&  Bluetooth_Valid) // 소켓통신/블루투스 통신 연결이 됬을 때 버튼이 표시되게함.
                    if (socket_Valid) // 소켓통신 연결이 됬을 때 버튼이 표시되게함.
                    {
                        btnStart.setVisibility(View.VISIBLE);
                        btnAdmin.setVisibility(View.VISIBLE);
                    } else if(!socket_Valid){
                        btnStart.setVisibility(View.INVISIBLE);
                        btnAdmin.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "서버/블루투스 연결이 되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
            }
        }, 5000);// 5초 정도 딜레이를 준 후 시작


    }

    @Override
    public void onResume(){
        super.onResume();
        //사용자에게 보여질 데이터 등 가져오기

    }

    @OnLongClick(R.id.btnAdmin)
    public void onLongClick()
    {

        Intent intent = new Intent(MainActivity.this, daily_statistics.class);
        startActivity(intent);
        overridePendingTransition(0,0); // 액티비티 전환 애니메이션 제거

        /*
        if(adminCheck == false) {
            adminMode = true;
            adminCheck = true;
            Toast.makeText(this, "어드민 모드 활성화", Toast.LENGTH_SHORT).show();

        }
        else{ // 관리자 모드 인경우
            adminMode = false;
            adminCheck = false;
            Toast.makeText(this, "어드민 모드 비활성화", Toast.LENGTH_SHORT).show();

        }
        */

    }

    @OnClick({R.id.btnHelp,R.id.btnAdmin,R.id.btnStart})
    public void onClick(View view)
    {

        if (view.getId() == R.id.btnHelp)
        {
            Intent intent = new Intent(MainActivity.this, HelpScreen.class);
            startActivity(intent);
            overridePendingTransition(0,0); // 액티비티 전환 애니메이션 제거
        }
        else if(view.getId() == R.id.btnStart)
        {
            Handler delayHandler1 = new Handler();
            delayHandler1.postDelayed(new Runnable() {
                @Override
                public void run() {

                    // 블루투스 발신, 장비 사용가능 한지에 대한 리퀘스트
                    BT.send(RequestCommand, false); // CR(Carriage Return):\r , LF(Line Feed):\n

                }
            }, 1000); //블루투스 연결이되고 5초후 IsObject를 보냄. (* 아두이노가 라즈베리에서 정보를 받아오는 시간이 필요하기 때문임. )

            btReceive(); // 블루투스 데이터 수신

            if (btnState==0) {
                //진행도를 로딩형식으로 설정
                btnStart.setIndeterminateProgressMode(true);
                // 로딩의 시작을 알림
                btnStart.setProgress(1);

                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if ( HW_state.equals("Available") ) { // 하드웨어가 물건을 받아들일 수 있을 때.
                            btnStart.setProgress(100);
                            btnState = 100;

                            delayHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    Intent intent = new Intent(MainActivity.this, step2_monitorObject.class);
                                    intent.putExtra("수용량체크", HW_Capacity);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                                    finish();

                                }
                            }, 1000); // 1초뒤 액티비티 자동전환

                        } else if(HW_state.equals("Ban"))  { // 하드웨어의 물건 수용량이 한계치 일 때
                            btnStart.setProgress(-1);
                            btnState = -1;
                        }

                    }
                }, 5000); //버튼 클릭후, 2초가 됬을 완료 상태가 됨.

            }

            else if (btnState== -1) // 사용불가 버튼을 클릭할 경우
            {

                Toast.makeText(MainActivity.this, "모든 품목의 수용량이 한계치입니다, 관리자에게 문의해주세요.", Toast.LENGTH_SHORT).show();

            }

        }
        else if (view.getId() == R.id.btnAdmin)
        {

            Intent intent = new Intent(MainActivity.this, RefillCoin.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
            finish();

            /*
            if(adminMode) {
                if (cnt == false)
                {
                    HW_state = "Unavailable";
                    cnt = true;
                    Toast.makeText(this, "사용불가 활성화", Toast.LENGTH_SHORT).show();

                }

                if(btnState== -1) // 사용불가 일때, 리셋을 위해 톱니바퀴 한번더 누를경우 액티비티 재실행 -- 사용가능활성화
                {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent);
                    overridePendingTransition(0, 0); // 액티비티 전환 애니메이션 제거
                    Toast.makeText(this, "사용가능 활성화", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else
            {
                Toast.makeText(this, "관리자 기능입니다.", Toast.LENGTH_SHORT).show();
            }


             */

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

                //BT.connect(MAC_ADDRESS); // 기기 연결시도
                //BT.connect(MAC_ADDRESS); // 기기 연결시도
                BT.connect("98:D3:71:FD:9C:2D"); // 기기 연결시도



                BT.setBluetoothConnectionListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
                    public void onDeviceConnected(String name, String address) {
                        //Toast.makeText(getApplicationContext(), "블루투스 연결완료 " + name + "\n" + address, Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.BLUE);
                        editText.setText( "블루투스1 연결완료 \n" + address);
                        Bluetooth_Valid = true;
                    }

                    public void onDeviceDisconnected() { //연결해제
                        //Toast.makeText(getApplicationContext(), "블루투스 연결해제 ", Toast.LENGTH_SHORT).show();
                        editText.setText("블루투스 연결해제");
                        Bluetooth_Valid = false;
                    }

                    public void onDeviceConnectionFailed() { //연결실패
                        //Toast.makeText(getApplicationContext(), "블루투스 연결실패,\n 재시도중..", Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.RED);
                        editText.setText("블루투스 연결실패,\n 재시도중..");
                        Bluetooth_Valid = false;
                        BT.connect(MAC_ADDRESS);

                    }
                });

            }
        }

    }

    public void btConnect2(){
        if (!BT2.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!BT2.isBluetoothEnabled()) { // 블루투스 OFF
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // 권한요청
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        else { // 블루투스 ON
            if (!BT2.isServiceAvailable()) { // 블루투스 서비스 OFF
                BT2.setupService();
                BT2.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리

                //BT.connect(MAC_ADDRESS); // 기기 연결시도
                //BT.connect(MAC_ADDRESS); // 기기 연결시도
                BT2.connect("98:D3:91:FD:B9:84"); // 기기 연결시도



                BT2.setBluetoothConnectionListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
                    public void onDeviceConnected(String name, String address) {
                        //Toast.makeText(getApplicationContext(), "블루투스 연결완료 " + name + "\n" + address, Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.BLUE);
                        editText.setText( "블루투스2 연결완료 \n" + address);
                        Bluetooth_Valid = true;
                    }

                    public void onDeviceDisconnected() { //연결해제
                        //Toast.makeText(getApplicationContext(), "블루투스 연결해제 ", Toast.LENGTH_SHORT).show();
                        editText.setText("블루투스 연결해제");
                        Bluetooth_Valid = false;
                    }

                    public void onDeviceConnectionFailed() { //연결실패
                        //Toast.makeText(getApplicationContext(), "블루투스 연결실패,\n 재시도중..", Toast.LENGTH_SHORT).show();
                        editText.setTextColor(Color.RED);
                        editText.setText("블루투스 연결실패,\n 재시도중..");
                        Bluetooth_Valid = false;
                        BT.connect(MAC_ADDRESS);

                    }
                });

            }
        }

    }

    public void btReceive(){

        BT.setOnDataReceivedListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {

                Log.d("TAG","[BT1] " +message);

                if(message.equals("All_Full") ){
                    Log.d("TAG", "RECEiVE DATA " + message);
                    HW_state = "Ban";
                    HW_Capacity = "1111";
                }
                else // 완전히 비어있거나 일부가 차있을 경우
                {
                    // 0000~1111 : can,soju,makju,book   0: low , 1:high(보관함 가득참)

                    if (message.equals("Available")) {
                        HW_state = "Available"; // 모든 보관함 사용 가능
                        HW_Capacity = "0000";
                    }
                    else // 보관함 일부가 포화상태인 경우
                    {
                        HW_state = "Available"; // 0101 등의 형태의 문자열 저장
                        HW_Capacity = message;
                    }

                }

            }

        });


        BT2.setOnDataReceivedListener(new app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {

                Log.d("TAG","[BT2] " +message);

                if(message.equals("All_Full") ){
                    Log.d("TAG", "RECEiVE DATA " + message);
                    HW_state = "Ban";
                    HW_Capacity = "1111";
                }
                else // 완전히 비어있거나 일부가 차있을 경우
                {
                    // 0000~1111 : can,soju,makju,book   0: low , 1:high(보관함 가득참)

                    if (message.equals("Available")) {
                        HW_state = "Available"; // 모든 보관함 사용 가능
                        HW_Capacity = "0000";
                    }
                    else // 보관함 일부가 포화상태인 경우
                    {
                        HW_state = "Available"; // 0101 등의 형태의 문자열 저장
                        HW_Capacity = message;
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
        else if(requestCode == GPS_ENABLE_REQUEST_CODE)
        {
            //사용자가 GPS 활성 시켰는지 검사
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {

                    Log.d("TAG", "onActivityResult : GPS 활성화 되있음");
                    checkRunTimePermission();
                    return;
                }
            }

        }
    }



    /**
     * Socket Server 연결 Listener
     */
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            socket_Valid = true;

            // 서버로 전송할 데이터 생성 및 채널 입장 이벤트 보냄.
            JSONObject sendData = new JSONObject();
            try {

                sendData.put("id", deviceID);
                sendData.put("location", addresses.get(0).getAddressLine(0) );
                sendData.put("gps_latitude", latitude);
                sendData.put("gps_longitude", longitude);

                //sendData.put("construction", false);
                //sendData.put("object_capacity", good);
                //sendData.put("have_money", "20)20)20)20"); // 초깃값은 서버에서 자동지정하기 때문에 필요가 없다.
                Log.d("TAG", "onConnect: "+sendData);
                socket.emit("clientInfo", sendData);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener UUID_Verify = new Emitter.Listener() { // 중복검사
        @Override
        public void call(Object... args) {

            String data = args[0].toString();

            if(data.equals("true"))
                ID_CHECK = true;
            else
                ID_CHECK = false;

        }
    };


    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());


        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void UUID_REGISTER(AppDatabase db) {

        if(db.todoDao().getAll().toString().equals("[]")) { // 고유번호가 부여되지 않은 경우
                db.todoDao().insert(new Todo(deviceID)); // 고유번호 등록
                //db.todoDao().delete();
                Log.d("TAG", "[알림] 고유번호(" + deviceID + ")를 등록하였습니다.");

        }
    }

    public void UUID_CREATE_CHECK(AppDatabase db){

        if(db.todoDao().getAll().toString().equals("[]")) { // 고유번호가 부여되지 않은 경우

            JSONObject sendData = new JSONObject();
            try {
                deviceID = getUniqueId();
                sendData.put("deviceID", deviceID);
                socket.emit("UUID_CHECK", sendData);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(ID_CHECK == true) { // 겹칠경우 재탐색

                Log.d("TAG", "[경고] 고유번호(" + deviceID + ")가 겹쳐 재등록 시도,,,");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UUID_CREATE_CHECK(db);
                    }
                }, 10000);// 10초 정도 딜레이를 준 후 시작
            }

        }
        else { // 고유번호가 존재하는 경우
            deviceID = db.todoDao().getAll().get(0).getTitle();
            Log.d("TAG", "[알림] 해당 기기 ID : " + deviceID);


        }

    }

    public void InitialSocket(){

        try {
            socket = IO.socket(serverURL);
        }catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG", "InitialSocket: "+e);
        }
        socket.connect(); // 소켓통신 시작
        Log.d("TAG", "InitialSocket 시작: "+socket.connect());
        socket.on("connect", onConnect);  // EVENT_CONNECT = connect, 접속했을때
        socket.on("UUID_Verify",UUID_Verify);
    }

    // 고유번호 생성
    public static String getUniqueId() {
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //yyyymmddhh24missSSS_랜덤문자6개
        uniqueId = RandomStringUtils.randomAlphanumeric(3)+"-"+uniqueId;

        return uniqueId;
    }

    public void onDestroy() {
        super.onDestroy();
        BT.stopService(); //블루투스 중지
        socket.close();
    }






}
