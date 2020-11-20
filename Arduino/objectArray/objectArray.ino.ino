#include <SoftwareSerial.h>
#include <Servo.h> 
#define BT_RXD 2
#define BT_TXD 3

Servo myservo0;  // 서보 변수 선언
Servo myservo1;  // 서보 변수 선언
Servo myservo2;  // 서보 변수 선언
Servo myservo3;  // 서보 변수 선언
Servo myservo4;  // 서보 변수 선언
Servo myservo5;  // 서보 변수 선언
String ReceiveData="";
const int servoPin5 = 13; // 서보 핀
const int servoPin4 = 12; // 서보 핀
const int servoPin3 = 11; // 서보 핀
const int servoPin2 = 10; // 서보 핀
const int servoPin1 = 9; // 서보 핀
const int servoPin0 = 8; // 서보 핀

SoftwareSerial bluetooth(BT_RXD, BT_TXD);



void setup() {
  // 
  pinMode(A0, OUTPUT);
  pinMode(4, INPUT);   //초음파 1,2,3,4
  pinMode(A1, OUTPUT);
  pinMode(5, INPUT);
  pinMode(A2, OUTPUT);
  pinMode(6, INPUT);
  pinMode(A3, OUTPUT);
  pinMode(7, INPUT);
   
  Serial.begin(9600);
  bluetooth.begin(9600);
  
  myservo0.attach(servoPin0); //서보로 11핀 사용하겠다고 설정
  delay(20); 
  myservo1.attach(servoPin1); //서보로 10핀 사용하겠다고 설정 
  delay(20);
  myservo2.attach(servoPin2); //서보로 11핀 사용하겠다고 설정 
  delay(20);
  myservo3.attach(servoPin3); //서보로 10핀 사용하겠다고 설정 
  delay(20);
  myservo4.attach(servoPin4); //서보로 11핀 사용하겠다고 설정 
  delay(20);
  myservo5.attach(servoPin5); //서보로 10핀 사용하겠다고 설정
                myservo0.write(90);
                delay(2);
                myservo2.write(90);
                delay(2);
                 myservo4.write(90);    
                delay(2);
} 

void loop() {
  float duration0, distance0;
  float duration1, distance1;
  float duration2, distance2;
  float duration3, distance3;

                myservo0.write(90);
                delay(2);
                myservo1.write(90);
                delay(2);
                 myservo4.write(90);    
                delay(2);
          
        if ( bluetooth.available()){
              ReceiveData = bluetooth.readStringUntil('\n'); // APP으로부터 받은 데이터를 String으로 읽어들임.( 나중에 )
            
            //////////////////////////////여기 부분에서 초음파 센서 활용해서 사용가능한지 확인하고 블루투스에세 전달해줘야함
        }
        
        if(ReceiveData.equals("IsAvailable"))// 초음파를 보낸다. 다 보내면 echo가 HIGH 상태로 대기하게 된다.
        {
          Serial.println(ReceiveData);
          digitalWrite(A0, HIGH);
          delay(10);
          digitalWrite(A0, LOW);
         
          // echoPin 이 HIGH를 유지한 시간을 저장 한다.
          duration0 = pulseIn(4, HIGH); 
          // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
          distance0 = ((float)(340 * duration0) / 10000) / 2; 
          Serial.println(distance0);
          delay(10);
          digitalWrite(A1, HIGH);
          delay(10);
          digitalWrite(A1, LOW);
         
          // echoPin 이 HIGH를 유지한 시간을 저장 한다.
          duration1 = pulseIn(5, HIGH); 
          // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
          distance1 = ((float)(340 * duration1) / 10000) / 2;
          delay(10);
          digitalWrite(A2, HIGH);
          delay(10);
          digitalWrite(A2, LOW);
         
          // echoPin 이 HIGH를 유지한 시간을 저장 한다.
          duration2 = pulseIn(6, HIGH); 
          delay(10);
          // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
          distance2 = ((float)(340 * duration2) / 10000) / 2; 
          delay(10);   
            digitalWrite(A3, HIGH);
          delay(10);
          digitalWrite(A3, LOW);
         
          // echoPin 이 HIGH를 유지한 시간을 저장 한다.
          duration3 = pulseIn(7, HIGH); 
          // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
          distance3 = ((float)(340 * duration3) / 10000) / 2;  
        
              if(distance0 < 8 ||distance1 < 8||distance2 < 8||distance3 < 8){
                  bluetooth.println("Full"); //물체 저장 불가
                  ReceiveData="";  // 읽어온 값 삭제
                }
                else
                  bluetooth.println("Available"); //물체 저장 불가
                  ReceiveData="";  // 읽어온 값 삭제
              
         }

       if(ReceiveData.equals("soju")){ //ReceiveData == josu
            delay(20);
            myservo0.write(50);         //왼쪽  
            delay(2);
            myservo1.write(130);    
            delay(2);
            myservo2.write(130);        //오른쪽
            delay(2);
            myservo3.write(50);    
            delay(2);
            
           delay(1000); 
          }
         else if(ReceiveData.equals("makju")){  //ReceiveData == makju
              delay(100);
              myservo0.write(160);
              delay(20);
              myservo1.write(20);   // 오른쪽
              delay(20);
              
              myservo4.write(50);
              delay(20);
              myservo5.write(130);  // 왼쪽 
              delay(20);
               ReceiveData="";  //ReceiveData 변수값 초기화
             delay(1000); 
         }
         else if(ReceiveData.equals("can")){  //ReceiveData == can
              delay(20);
              myservo0.write(50);   //왼쪽
              delay(2);
              myservo1.write(130);    
              delay(2);
              myservo2.write(50);   //왼쪽
              delay(2);
              myservo3.write(130);    
              delay(2);
              
               ReceiveData="";  //ReceiveData 변수값 초기화
              delay(1000); 
            }
       else if(ReceiveData.equals("book")){  //ReceiveData == book
           delay(20);
           myservo0.write(130); //오른쪽
            delay(2);
            myservo1.write(50);    
            delay(2);
            myservo4.write(140);  // 오른쪽
            delay(2);
            myservo5.write(40);    
            delay(2);
             ReceiveData="";  //ReceiveData 변수값 초기화
           delay(1000);
          }       

         else {
            
                myservo0.write(90);
                delay(2);
                myservo2.write(90);
                delay(2);
                 myservo4.write(90);    
                delay(2);
         }
      
}
