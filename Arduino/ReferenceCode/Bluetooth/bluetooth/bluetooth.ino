#include <SoftwareSerial.h>

SoftwareSerial BTSerial(4, 5);   
String ReceiveData=""; // 받는문자열
String command_1 = "IsAvailable";
String command_2 = "text2";


void setup() {  
  Serial.begin(9600);
  BTSerial.begin(9600);
}

void loop() {
  if (BTSerial.available()){
    ReceiveData = BTSerial.readStringUntil('\n'); // APP으로부터 받은 데이터를 String으로 읽어들임.
  }

  if(ReceiveData.equals(command_1))  //ReceiveData == command_1
  {  
       Serial.println("[ReceiveData] "+command_1); // APP으로부터 받은 데이터를 시리얼모니터에 출력, 없어도되는 코드
       delay(15000);
       BTSerial.println("Available"); // 아두이노 -> APP 으로 데이터(ACCEPT) 전송
       //BTSerial.println("DENINED (reason:~~~)");
       ReceiveData="";  //ReceiveData 변수값 초기화
  }
  
  else if(ReceiveData.equals(command_2))  //ReceiveData == command_1
  {
       Serial.println("[ReceiveData] "+command_2); // APP으로부터 받은 데이터를 시리얼모니터에 출력, 없어도되는 코드
       BTSerial.println("Available2"); // 아두이노 -> APP 으로 데이터(ACCEPT) 전송
       //BTSerial.println("DENINED (reason:~~~)");
       ReceiveData="";  //ReceiveData 변수값 초기화
  }
  
}
