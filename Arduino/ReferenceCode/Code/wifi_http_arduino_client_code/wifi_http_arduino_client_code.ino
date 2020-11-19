#include "WiFiEsp.h"

#ifndef HAVE_HWSERIAL1
#endif

#include "SoftwareSerial.h"

#define rx_pin 2
#define tx_pin 3

SoftwareSerial esp(rx_pin, tx_pin); // RX, TX 핀번호

char ssid[] = "902-2.4G-1";            // SSID 입력, (모바일 핫스팟 테스트 완료), (단, ESP8266은 5G 지원 안함) 
char pass[] = "kpu123456!";        // WIFI 비밀번호 입력

int status = WL_IDLE_STATUS;     // 와이파이 상태

bool workList = false; // 작업목록이 있을 경우, request를 그만
int price =0;
unsigned long ulPreTime = 0;

char server[] = "14.55.235.173";
int port = 52273;


// 이더넷 클라이언트 객체 초기화

WiFiEspClient client;


void setup()

{
  //String cmd = "AT+UART_DEF=9600,8,1,0,0";

  // 디버깅을 위한 시리얼 초기화
  Serial.begin(9600);

  // ESP 모듈을 위한 시리얼 초기화
  esp.begin(9600);

  // ESP 모듈 초기화
  WiFi.init(&esp);



  // 와이파이 쉴드 유무 확인

  if (WiFi.status() == WL_NO_SHIELD) {

    Serial.println("WiFi shield not present");

    // don't continue
    while (true);

  }



  // WiFi 네트워크 연결 시도

  while ( status != WL_CONNECTED) {

    Serial.print("▶ 연결시도, WPA SSID: ");
    Serial.println(ssid);

    // WPA/WPA2 네트워크 연결
    status = WiFi.begin(ssid, pass);

  }



  // 네트워크 연결성공

  Serial.println("<<< ----- 와이파이 네트워크 연결 성공! ----- >>>");


  printWifiStatus();


  Serial.println();

  Serial.println("▶ 서버와의 연결을 시작합니다...");

  
}



void loop()

{

  int line =0;
  char ch_price[7];

  unsigned long ulCurTime = millis();

  if (ulCurTime - ulPreTime >= 10000)
  {
      ulPreTime = ulCurTime;
     
      client.flush();


      // 서버와 연결이 될 경우,

      if(!workList )
      {

         if (client.connect(server, port)) {
    
            Serial.println("<<< ----- Connected! Sending HTTP request ----- >>>");
        
            // HTTP request
        
            client.println("GET /sky HTTP/1.1");
            client.println("Host: 14.55.235.173");
            client.println("Connection: close");
            client.println();
        
          }
          else
          {
             Serial.println("Connect Failed!");
          }


        
          while(client.connected())
          {
            if (client.available()) 
            {
          
              // Serial.read(); => 시리얼 통신 수신버퍼에서 첫 번째 문자를 읽어 반환한다. 수신 버퍼가 비어있으면 -1을 반환
              // readString : 전부 저장, readStringUntil : 한줄씩 저장
              String strReceive = client.readStringUntil('\n'); 
              
              
              if ( line == 8){ // 필요한 수신 값이 9번째에 있음.
                Serial.println(strReceive);
          
                strReceive.toCharArray(ch_price,strReceive.length()+1);  // atoi 사용하기 위해 char형으로 변환
                price = atoi(ch_price);  // 문자열->정수 변환

                if(price == 0)
                {
                  // 작업요청이 없으니 while에서벗어나서 다시 새로 수신해야함.
                  break;
                }
                else
                {
                  workList = true;
                  // 작업처리 함수 호출 여기서.
                }
                
              }
                line++;
      
              
          
            }
          }


          
      }
      else //작업 처리중
      {
        
      }
        
      


   
  
  
  
  }
}




void printWifiStatus()

{

  // 연결된 네트워크의 SSID를 출력

  Serial.print("■ SSID: ");

  Serial.println(WiFi.SSID());



  // 와이파이 쉴드의 IP주소를 출력

  IPAddress ip = WiFi.localIP();

  Serial.print("■ IP 주소: ");

  Serial.println(ip);



  // 수신 신호 세기 출력

  long rssi = WiFi.RSSI();

  Serial.print("■ 신호 세기 (RSSI):");

  Serial.print(rssi);

  Serial.println(" dBm");

}
