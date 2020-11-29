var app = require('express')();
var http = require('http').createServer(app);
var io = require('socket.io')(http);
var mysql = require("mysql");


var db;
db = mysql.createConnection({
user : 'root',
password : '!tksgml',
port : 3306
});

db.query("set names utf8");
db.query("set session character_set_connection=utf8");
db.query("set session character_set_results=utf8");
db.query("set session character_set_client=utf8");

db.query("use vm_db");     // db이름
db.on('error', function(err) {
   console.log(err.code)
});


app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html');
});


var id = ""; // 기기 아이디
var location = ""; // 기기 위치
var construction = true; // 점검여부
var object_capacity = "Available"; // 기기 수용량 상태
let have_money = "20/20)20]20";  // 500원 100원 50원 10원을 구분하기 위해 특수문자를 사용하였다.

var gps_latitude = 0.0; // 기기 위도, 경도
var gps_longitude = 0.0;


let today = new Date();
let year = today.getFullYear();
let month = today.getMonth()+1;
let day = today.getDate();

let date_format = year+"-"+(("00"+month.toString()).slice(-2))+"-"+(("00"+day.toString()).slice(-2));

console.log("날짜::: "+date_format);
//let str_HM = `"${n_Array_HM[0]}/${n_Array_HM[1]})${n_Array_HM[2]}]${n_Array_HM[3]}"`; // 문자열로 변환


for(let i=22;i<24;i++)
{
  let rand = Math.floor( Math.random() * 4 + 5 );
  let rand1 = Math.floor( Math.random() * 4 + 10 );
  let rand2 = Math.floor( Math.random() * 4 + 3 );
  let rand3 = Math.floor( Math.random() * 4 + 8 );

  db.query(`INSERT INTO device_log(id,date,soju,makju,can,book) VALUES ("09O-0706041416", "2020-07-${i}", ${rand}, ${rand1},${rand2},${rand3})`,function(error,result){
    if(error){
      console.log(error);
    }
  });  

}


io.on('connection', (socket) => {

     console.log('[알림] 클라이언트 소켓통신 연결시작');

    
    socket.on('clientInfo',function(data){

      console.log(data);

      id=data.id;
      location=data.location;
      gps_latitude = data.gps_latitude;
      gps_longitude = data.gps_latitude;


      db.query(`SELECT * FROM device_state WHERE id="${id}"`,function(error,result){
        //console.log(result);
 
         if(result.length == 0) // 새로운 기기 정보일 경우, INSERT
         {
            db.query(`INSERT INTO device_state(id,location,construction,object_capacity,have_money,gps_latitude,gps_longitude) VALUES ("${id}", "${location}", ${construction}, "${object_capacity}","${have_money}",${gps_latitude},${gps_longitude})`,function(error,result){
              if(error){
                console.log(error);
              }
            });   

         }
         else if (result[0].location != location) // 기존에 이미 있는 기기 정보면서, 위치만 달라진 경우, UPDATE
         {
            db.query(`UPDATE device_state SET location="${location}", construction=${construction}, object_capacity="${object_capacity}", gps_latitude =${gps_latitude}, gps_longitude=${gps_longitude}  WHERE id="${id}"`,function(error,result){
              if(error){
                console.log(error);
              }
            });   

         }
       }); 
      
    })

    socket.on('UUID_CHECK',function(data){ // 중복아이디 확인을 위한 소켓통신
      db.query(`SELECT * FROM device_state WHERE id=${data.deviceID}`,function(error,result){

        
        var check_result = false;
        if (result == null) return 0;
        
        if(result.length == 1) { // 결과가 존재하면 중복ID가 있는 것 이므로
             check_result = true;
            }
        else{
           check_result = false;
        }
           
           socket.emit('UUID_Verify',check_result);
        if(error){
          console.log(error);
        }
      });     
    })


    socket.on('CHECK_PRICE',function(data){
      console.log(data);
      db.query(`SELECT price FROM marketprice WHERE type="${data.objectName}"`,function(error,result){
        console.log(result);
        
        if(error){
          console.log(error);
        }

        var price = Number(result[result.length-1].price);
        socket.emit('RECEIVE_PRICE',price);

        console.log(`가격 ${price}`);

      });   

      
      db.query(`SELECT have_money FROM device_state WHERE id="${data.deviceID}"`,function(error,result){
        // console.log(result[0].price.toString());
   
         console.log(`id="${data.deviceID}"의 SELECT result = ${result[result.length-1].have_money}`);
         if(error){
           console.log(error);
         }
         have_money = result[result.length-1].have_money;
        
         
         let idx = have_money.indexOf("/");  // 문자열 형태의 기기 잔여금을 개별적으로 분리하기 위한 변수
         let idx2 = have_money.indexOf(")");
         let idx3 = have_money.indexOf("]");

    
         var n_Array_HM = {
             a : have_money.substring(0,idx),      // 남은 500원 개수
             b : have_money.substring(idx+1,idx2),
             c : have_money.substring(idx2+1,idx3),
             d : have_money.substring(idx3+1),
         }; // Have_Money 배열선언

  
         socket.emit('RECEIVE_HAVE_MONEY',n_Array_HM);

      
          console.log("n_Array_HM.a = "+n_Array_HM.a);
          console.log("n_Array_HM.b = "+n_Array_HM.b);
          console.log("n_Array_HM.c = "+n_Array_HM.c);
          console.log("n_Array_HM.d = "+n_Array_HM.d);
         
       });   


    })

    socket.on('SEND_HM_LOG',function(data){ // 동전수 갱신과 로그 기록 이벤트

      console.log("SEND_HAVE_MONEY data.objectName = "+ data.objectName);
      db.query(`SELECT * FROM device_state WHERE id="${data.deviceID}"`,function(error,result){
        //console.log(result);
 
         if(result.length == 0) // 새로운 기기 정보일 경우, 에러
         {
             if(error){
                console.log(error);
              }
         }

         let str_HM = `"${data.a}/${data.b})${data.c}]${data.d}"`; // 남은 돈 문자열로 변환
         console.log(`str_HM = ${str_HM}`);

            db.query(`UPDATE device_state SET have_money=${str_HM}  WHERE id="${data.deviceID}"`,function(error,result){
              if(error){
                console.log(error);
              }
            });   

         
       }); 



       db.query(`SELECT * FROM device_log WHERE id="${data.deviceID}" AND date="${date_format}"`,function(error,result){
        
        if(result.length == 0) // 새로운 기기 정보일 경우, INSERT
        {
            db.query(`INSERT INTO device_log(id,date,${data.objectName}) VALUES ("${data.deviceID}","${date_format}", 1)`,function(error,result){
              if(error){
                console.log(error);
              }
            });   
        }
        else
        {
          let num= 0;
          if(data.objectName == "soju")
            num = (result[0].soju)+1;
          else if(data.objectName == "makju")
            num = (result[0].makju)+1;
          else if(data.objectName == "book")
            num = (result[0].book)+1;
          else if(data.objectName == "can")
           num = (result[0].can)+1;

          console.log("num = "+num);
         
          db.query(`UPDATE device_log SET ${data.objectName}=${num} WHERE id="${data.deviceID}" AND date="${date_format}"`,function(error,result){
            if(error){
              console.log(error);
            }
          });   

        }

    
       }); 
      })


      socket.on('ADD500',function(data){ // 동전수 갱신과 로그 기록 이벤트
      

        db.query(`SELECT * FROM device_state WHERE id="${data.deviceID}"`,function(error,result){
          //console.log(result);
   
           if(result.length == 0) // 업데이트인데 정보가 없다는 건 말이안됨.
           {
               if(error){
                  console.log(error);
                }
           }
     
           else  // 기존에 이미 있는 기기 정보면서, UPDATE
           {
                have_money = result[result.length-1].have_money;
            
            
                let idx = have_money.indexOf("/");  // 문자열 형태의 기기 잔여금을 개별적으로 분리하기 위한 변수
                let idx2 = have_money.indexOf(")");
                let idx3 = have_money.indexOf("]");
      
          
                var n_Array_HM = {
                    a : parseInt(have_money.substring(0,idx)),      // 남은 500원 개수
                    b : parseInt(have_money.substring(idx+1,idx2)),
                    c : parseInt(have_money.substring(idx2+1,idx3)),
                    d : parseInt(have_money.substring(idx3+1)),
                }; // Have_Money 배열선언
                console.log("ori n_Array_HM.a = "+  n_Array_HM.a)
                n_Array_HM.a= n_Array_HM.a+25;
                console.log("new n_Array_HM.a = "+  n_Array_HM.a)

                let str_HM = `"${n_Array_HM.a}/${n_Array_HM.b})${n_Array_HM.c}]${n_Array_HM.d}"`; // 문자열로 변환
                console.log("new str_HM= "+  str_HM)

              db.query(`UPDATE device_state SET have_money=${str_HM} WHERE id="${data.deviceID}"`,function(error,result){
                if(error){
                  console.log(error);
                }
              });   
  
           }
         }); 

      
      })

      socket.on('ADD100',function(data){ // 동전수 갱신과 로그 기록 이벤트
    
        db.query(`SELECT * FROM device_state WHERE id="${data.deviceID}"`,function(error,result){
          //console.log(result);
   
           if(result.length == 0) // 업데이트인데 정보가 없다는 건 말이안됨.
           {
               if(error){
                  console.log(error);
                }
           }
     
           else  // 기존에 이미 있는 기기 정보면서, UPDATE
           {
                have_money = result[result.length-1].have_money;
            
            
                let idx = have_money.indexOf("/");  // 문자열 형태의 기기 잔여금을 개별적으로 분리하기 위한 변수
                let idx2 = have_money.indexOf(")");
                let idx3 = have_money.indexOf("]");
      
          
                var n_Array_HM = {
                    a : parseInt(have_money.substring(0,idx)),      // 남은 500원 개수
                    b : parseInt(have_money.substring(idx+1,idx2)),
                    c : parseInt(have_money.substring(idx2+1,idx3)),
                    d : parseInt(have_money.substring(idx3+1)),
                }; // Have_Money 배열선언
                console.log("ori n_Array_HM.a = "+  n_Array_HM.a)
                n_Array_HM.b= n_Array_HM.b+25;
                console.log("new n_Array_HM.a = "+  n_Array_HM.a)

                let str_HM = `"${n_Array_HM.a}/${n_Array_HM.b})${n_Array_HM.c}]${n_Array_HM.d}"`; // 문자열로 변환
                console.log("new str_HM= "+  str_HM)

              db.query(`UPDATE device_state SET have_money=${str_HM} WHERE id="${data.deviceID}"`,function(error,result){
                if(error){
                  console.log(error);
                }
              });   
  
           }
         }); 



      })
      socket.on('ADD50',function(data){ // 동전수 갱신과 로그 기록 이벤트
        db.query(`SELECT * FROM device_state WHERE id="${data.deviceID}"`,function(error,result){
          //console.log(result);
   
           if(result.length == 0) // 업데이트인데 정보가 없다는 건 말이안됨.
           {
               if(error){
                  console.log(error);
                }
           }
     
           else  // 기존에 이미 있는 기기 정보면서, UPDATE
           {
                have_money = result[result.length-1].have_money;
            
            
                let idx = have_money.indexOf("/");  // 문자열 형태의 기기 잔여금을 개별적으로 분리하기 위한 변수
                let idx2 = have_money.indexOf(")");
                let idx3 = have_money.indexOf("]");
      
          
                var n_Array_HM = {
                    a : parseInt(have_money.substring(0,idx)),      // 남은 500원 개수
                    b : parseInt(have_money.substring(idx+1,idx2)),
                    c : parseInt(have_money.substring(idx2+1,idx3)),
                    d : parseInt(have_money.substring(idx3+1)),
                }; // Have_Money 배열선언
                console.log("ori n_Array_HM.a = "+  n_Array_HM.a)
                n_Array_HM.c= n_Array_HM.c+25;
                console.log("new n_Array_HM.a = "+  n_Array_HM.a)

                let str_HM = `"${n_Array_HM.a}/${n_Array_HM.b})${n_Array_HM.c}]${n_Array_HM.d}"`; // 문자열로 변환
                console.log("new str_HM= "+  str_HM)

              db.query(`UPDATE device_state SET have_money=${str_HM} WHERE id="${data.deviceID}"`,function(error,result){
                if(error){
                  console.log(error);
                }
              });   
  
           }
         }); 

      })
      socket.on('ADD10',function(data){ // 동전수 갱신과 로그 기록 이벤트
    
        db.query(`SELECT * FROM device_state WHERE id="${data.deviceID}"`,function(error,result){
          //console.log(result);
   
           if(result.length == 0) // 업데이트인데 정보가 없다는 건 말이안됨.
           {
               if(error){
                  console.log(error);
                }
           }
     
           else  // 기존에 이미 있는 기기 정보면서, UPDATE
           {
                have_money = result[result.length-1].have_money;
            
            
                let idx = have_money.indexOf("/");  // 문자열 형태의 기기 잔여금을 개별적으로 분리하기 위한 변수
                let idx2 = have_money.indexOf(")");
                let idx3 = have_money.indexOf("]");
      
          
                var n_Array_HM = {
                    a : parseInt(have_money.substring(0,idx)),      // 남은 500원 개수
                    b : parseInt(have_money.substring(idx+1,idx2)),
                    c : parseInt(have_money.substring(idx2+1,idx3)),
                    d : parseInt(have_money.substring(idx3+1)),
                }; // Have_Money 배열선언
                console.log("ori n_Array_HM.a = "+  n_Array_HM.a)
                n_Array_HM.d= n_Array_HM.d+25;
                console.log("new n_Array_HM.a = "+  n_Array_HM.a)

                let str_HM = `"${n_Array_HM.a}/${n_Array_HM.b})${n_Array_HM.c}]${n_Array_HM.d}"`; // 문자열로 변환
                console.log("new str_HM= "+  str_HM)

              db.query(`UPDATE device_state SET have_money=${str_HM} WHERE id="${data.deviceID}"`,function(error,result){
                if(error){
                  console.log(error);
                }
              });   
  
           }
         }); 
      })


      socket.on('checkWeight',function(data){ // 동전수 갱신과 로그 기록 이벤트
        console.log(data);
        db.query(`SELECT * FROM marketprice WHERE type="${data.ObjectName}"`,function(error,result){
          console.log(result);
           if(result.length != 0) // 새로운 기기 정보일 경우, INSERT
           {
               
              if(data.WeightValue > result[0].weight) // 무게가 기준무게보다 클경우
              {
                 socket.emit('ValidWeight',"false");
              }
              else if(data.WeightValue <= result[0].weight) // 무게가 기준보다 적을경우
              {
                 socket.emit('ValidWeight',"true");
              }
           }
           
         }); 

      })

      socket.on('Request_Today_Statistic',function(data){ // 동전수 갱신과 로그 기록 이벤트
        console.log(data);
        console.log(date_format);
        db.query(`SELECT * FROM device_log WHERE id="${data.deviceID}" AND date="${date_format}"`,function(error,result){
  
           if(result.length != 0) // 새로운 기기 정보일 경우, INSERT
           {
            console.log("데이터 있음");
                var n_Array_TodayLog = {
                
                  soju : result[0].soju,      // 남은 500원 개수
                  makju : result[0].makju,
                  can : result[0].can,
                  book : result[0].book,
                }; // Have_Money 배열선언
                socket.emit('Send_Today_Statistic', n_Array_TodayLog);
                console.log("n_Array_TodayLog "+n_Array_TodayLog);
           }
           else{
              console.log("데이터 없음");

           }
           
         }); 

      })


      socket.on('Request_Daily_Statistic',function(data){ // 동전수 갱신과 로그 기록 이벤트

        db.query(`SELECT * FROM device_log WHERE id="${data.deviceID}" AND date="${data.selectedDate}"`,function(error,result){
           if(result.length != 0) // 새로운 기기 정보일 경우, INSERT
           {

            console.log("데이터 있음 "+ result);
                var n_Array_TodayLog = {
                
                  soju : result[0].soju,      // 남은 500원 개수
                  makju : result[0].makju,
                  can : result[0].can,
                  book : result[0].book,
                }; // Have_Money 배열선언
                socket.emit('Send_Daily_Statistic', n_Array_TodayLog);
                console.log("n_Array_TodayLog "+n_Array_TodayLog);
           }
           else{
              console.log("데이터 없음");

           }
           
         }); 

      })


  }); // io.on('connection', (socket) => {





  http.listen(3000, () => {
  console.log('Server Running at http://127.0.0.1:3000');
});