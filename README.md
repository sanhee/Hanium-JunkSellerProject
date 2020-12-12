# [졸업작품/한이음] 영상 처리 기술을 이용한 재활용 분리기
 -  <b>과학기술정보통신부</b>에서 주관하는 2020 한이음 공모전 <b>입선</b>
 -  <b>한이음</b> 2020 학술대회 <b>우수상</b> 수상
 -  <b>한국정보처리학회</b> 2020 추계학술대회 <b>동상</b> 수상
 
 | | | | 
|------|---|---|
|<p align="left"><img src="https://i.imgur.com/XrpEvlF.png" width="200" height="300"></p> <p align="center">| <img src="https://i.imgur.com/WTUSxeQ.jpg" width="200" height="300"></p>| <img src="https://i.imgur.com/KQ1JQfK.jpg" width="200" height="300">|

### 작품 영상 링크(Youtube)
> - https://youtu.be/Oav8CXkXcVU

### 맡은 역할
> 1. [안드로이드 APP](https://github.com/sanhee/graduationProject/tree/main/App) 
> 2. [NodeJS Server](https://github.com/sanhee/graduationProject/tree/main/Server)
> 3. [동전분리 알고리즘](https://github.com/sanhee/graduationProject/tree/main/Algorithm/CoinSeperate)

### 프로젝트 소개

>- 1인 가구의 증가로 인해 한 가정 당 발생하는 소주병, 맥주병, 책, 캔 등이 증가하게 되면서, 재활용을 직접 다 해서 버려야 하는 노동과 시간이 많이 소요된다. 
>- 또한, 공동주택(아파트, 연립주택 등)이 아닌 단독 주택(다가구 주택)에 임차인으로 처음 입주하는 사람들은 재활용을 하는 장소나 종류를 모르는 경우가 많다.
>- 이러한 문제점을 해결하기 위해서 판매가 가능한 재활용을 편리하게 판매하는 서비스를 제공하는 재활용 분리기를 제안한다. 
>- 재활용 분리기는 <b><u>첫째</u></b>, 무게/카메라/모터 센서 등을 통해 재활용 분리기를 제어하고 라즈베리 파이와 서버 간 통신을 통해 알맞은 금액을 산출하여 현금으로 반환해 준다. 
>- <b><u>둘째</u></b>, 사용자가 판매하려는 재활용을 특정 물건으로 딥러닝 시키고, 해당 물건의 평균 추정 무게를 구축하여, 재활용의 인식 오류를 줄이고자 한다.

### 개발배경 및 필요성
>- 전북도민일보에 따르면 아직도 일부 슈퍼마켓이나 소규모 마트에서는 공병 회수가 제한적이거나 거부하는 사례가 있다고 한다. 
>- 하지만 무인기로 바로 하나의 병이라도 현금으로 즉시 바꿀 수 있기 때문에, 사람들이 길거리에 쓰레기를 버리는 횟수가 적어질 것이고, 
>- 재활용품의 효율적 관리를 통해 재활용이 가능해진다.

### 프로젝트 주요기능

>1. 실시간 객체 감지 및 분석 기능
>   - 라즈베리 파이와 카메라 모듈을 이용하여 실시간으로 객체를 판단하고 분석하여 4개의 레이블로 나누고, 분별해낼 수 있다.
>2. 머신러닝을 이용한 객체 판단 및 분류
>   - 머신러닝을 통해 객체를 잡아낸 것을 종류별로 분류시켜 picamera를 통해 나타낸다.
>3. 물체 분류 시스템
>   - picamera를 통해 인식한 물체가 어떤 것인지를 확인하고, 그에 맞는 공간으로 모터를 활용하여 물체 분류를 시켜준다.
>4. 무게 측정 기능
>   - p물체의 인식이 끝나면 액츄레이터를 이용하여, 해당 물체를 뒤로 옮기고, 그 공간에서 물체의 무게를 측정하여 라즈베리 파이에게 넘겨준다.
>5. 서버를 통한 제어

>   - 라즈베리 파이가 처리한 영상 정보와 무게 정보를 서버가 전달받아 자체 데이터베이스의 재활용가능자원 가격과 비교하여 적합한 가격을 탐색한 후 동전 반환장치에 값을 넘겨준다.
>   - 또한 오류 상황에 대한 정보를 실시간으로 주고받아 문제에 대한 예외 처리를 진행한다.
>
>6. 데이터베이스 관리
>   - 재활용 분리기의 위치정보 / 장비 고장 현황 / 보유 현금 현황 / 재활용 수용량 현황 / 보유 현금 현황을 주기적으로 주고받아 해당하는 이벤트가 발생할 경우DB를 기록한다.
>7. 전용 어플리케이션
>   - GPS를 이용하여 가장 가까운 재활용 분리기 위치와 판매 가능 여부를 미리 파악  할 수 있으며, 재활용 분리기의 문제 상황에 대한 문의를 할 수 있다.
>8. 동전 배출

>   - 서버를 통해 배출해야 하는 금액을 받으면 아두이노를 통해서 각 동전의 배출 개수를 계산하고 사용자에게 금액에 맞게 동전을 주기 위해 서보모터를 사용하여 사용자에게 배출한다.
>   -동전의 남은 개수를 서버에 알려주고 동전이 없으면 동전 배출을 정지하고, 서버에 동전이 없다는 것을 알린다.

### 작품의 기대효과 및 활용분야

>- 기존 재활용 분리에서는 사람이 직접 분리해서 가져가 다시 분리함에 맞는 재활용품들을 넣는 수고로움이 있다. 이를 해결하기 위해 본 프로젝트는 재활용품을 손쉽게 처리하는 재활용 분리기를 개발하고 해당 제품을 각 지역별 공공기관에 비치하여 시간과 장소에 구애받지 않고 소비자가 원하는 때에 재활용을 판매할 수 있는 접근 환경을 구축한다. 이 작품의 활용분야는 분리수거의 목적을 통해 알 수 있는데, 
>  우리가 사용하는 것들이 매립·소각의 방법으로 사라지기 전에 자원으로 활용하기 위함이 분리수거의 목적이다. 이는 길거리의 쓰레기 무단 투기 문제를 해결해주면서, 여기서 나온 재활용품들을 재사용할 수 있게 해준다. 결론적으로 다른 재활용 자원들의 재사용률을 높여주고, 환경부하가 적은 생활공간의 구축과 도시환경을 개선시킬 수 있다.
