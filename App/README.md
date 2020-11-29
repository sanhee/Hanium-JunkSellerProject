# 재활용분리기 안드로이드 APP

### (1) 사용 기술

#### 1) 클라이언트

##### ① 안드로이드
- 세계 최다 사용자를 보유하고 있는 만큼 주변에서 구하기 쉬운 안드로이드 디바이스를 클라이언트 기기로 선정 하였고, Java 언어로 프로그래밍 하였다.


##### ② 라이브러리
 - [BluetoothSPP by akexorcist](https://github.com/akexorcist/BluetoothSPPLibrary)
    -  안드로이드와 HC-06 모듈간의 블루투스 통신을 추상화한 라이브러리로 통신 코드 설계의 빠른 생산성을 위해 해당 라이브러리를 사용하였다.
           
 - [AnyChart](https://github.com/AnyChart)
    -  심플하고 완성도 있는 차트를 빠르게 구현할 수 있는 커스텀 라이브러리로, 일간/주간 통계를 구현하기 위해 사용하였다.
   
 -  [Horizontal Calendar by Mulham-Raee](https://github.com/Mulham-Raee/Horizontal-Calendar)
    -  달력을 수평으로 제공하는 라이브러리로, 일간 통계를 구현하기 위해 앞선 AnyChart 라이브러리와 혼용해서 사용하였다.

#### 2) 서버
##### ① NodeJS

- 웹 서버를 JavaScript로 프로그래밍 하여 손쉽게 구축할 수 있는 플랫폼으로, MariaDB를 제어하기 위해 사용하였다.


##### ② MariaDB

- MariaDB는 오픈 소스의 관계형 데이터베이스 관리 시스템(RDBMS)이다. MySQL과 동일한 소스 코드를 기반으로 하며, 이것은 고물 분리기의 각종 정보(기기 정보, 판매 기록, 고물 시세)를 영구적으로 보관 및 컨트롤 하기 위해 사용하였다.

#### 3) 클라이언트-서버 통신 모듈

##### ① Socket.IO

- SocketIO는 WebSocket을 기반으로 클라이언트와 서버의 양방향 통신을 가능하게 해주는 모듈로 애플리케이션에서 서버 데이터베이스를 이벤트로 CRUD 하기 위해 사용하였다.


### (2) 애플리케이션 기능

 1) 각 H/W간 블루투스 통신

- BluetoothSPP를 활용하여, 각 3가지 H/W 파트(물체인식, 무게인식, 물체분리)과 통신을 한다. 애플리케이션에서 데이터처리를 정확하게 연산 하기 위해 모든 H/W를 동시에 멀티 페어링을 하지 않고 단일 스레드를 사용하여, 필요한 파트마다 페어링 및 통신을 한다.

 2) 예외사항 에러 처리

- 애플리케이션은 H/W에서 받은 정보(수용량, 물체정보, 무게)를 수집/비교해 정상범주에서 벗어나는 값이 감지될 경우 모든 프로세스를 즉시 중지하고, 에러 메시지와 초기화면으로 리셋 한다.



3) 기기 UNIQUE ID 생성 [(참고 블로그)](https://toshi15shkim.github.io/articles/2019-10/java-unique-id)
```java
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
```


- 고물 분리기 디바이스가 복수 개 있다고 가정했을 때, 이를 구분할 아이디가 필요하다고 생각되어, 날짜와 랜덤 문자열을 통해 겹치지 않는 고유 번호를 구현하여, 애플리케이션을 처
음 실행할 경우 고유 번호를 생성해 서버 데이터베이스에 등록한다.



### 유즈케이스 다이어그램
![.](https://i.imgur.com/l5AYK0G.png)

### 시퀀스 다이어그램
![.](https://i.imgur.com/8FAEYzL.png)

### 데이터베이스, 테이블
![.](https://i.imgur.com/rUpkSof.png)

### 애플리케이션 UI 설계도

![.](https://i.imgur.com/SoVLoz8.png)
![.](https://i.imgur.com/thEFfOa.png)
