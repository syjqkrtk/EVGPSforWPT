package com.umls.evgps;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.umls.evgps.modules.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MainActivity  extends AppCompatActivity {

    private EditText testEdit;

    private Button button1;
    private Button button2;
    private TextView txtResult;
    private String Filename;
    private String FileSetting;
    private Button buttonSetting;

    public String target_phone1;
    public double target_long1;
    public double target_lat1;

    public String target_phone2;
    public double target_long2;
    public double target_lat2;

    public String target_phone3;
    public double target_long3;
    public double target_lat3;

    public int numinv;
    public double range;

    // socket 통신용
    //서버주소
    public String serverIP;
    //사용할 통신 포트
    public int serverPort;

    //화면 표시용 TextView
    public TextView textSocket = null;
    // public Boolean receive_flag = false;

    public DatagramSocket socket;

    public StartUDP startUDP;
    public SendUDP sendUDP;

    public Double gps_x;
    public Double gps_y;
    public Double speed;
    public Boolean request_charging;
    public Double pad_x;
    public Double pad_y;

//    BroadcastReceiver smsReceiver;

    public String SendPhoneNum;

    public Timer bTimer;
    public String udpBdata;
    public int udpBtime;
    public Boolean udpBflag;


    public int MaxSend;
    public int timerValue;

    private String s_phone = null;

    private class RegisterThread implements Runnable{
        public RegisterThread(){

        }

        public void run(){
            boolean isChecked = false;
            while(!isChecked){
                if (checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    isChecked = true;
                }
                Log.i("olev", isChecked + "");
            }


            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            s_phone = mgr.getSimSerialNumber();
            Log.i("olev",s_phone);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new RegisterThread()).start();

        testEdit = (EditText) findViewById(R.id.testEdit);

        Button test = (Button) findViewById(R.id.testButton);
        test.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SendPushData(testEdit.getText().toString(), "TEST SONG");
            }
        });

        button1 = (Button) findViewById(R.id.button1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        button2 = (Button) findViewById(R.id.button2);
        button2.setEnabled(false);
        txtResult = (TextView) findViewById(R.id.txtResult);
        textSocket = (TextView) findViewById(R.id.textSocket);
        buttonSetting = (Button) findViewById(R.id.buttonSetting);

        numinv = 1;
        range = 5;
        target_phone1 = "01042592587";
//        serverIP = "143.248.230.195";
//        serverPort = 8011;
        target_lat1 = 36.373533066;
        target_long1 = 127.3618129;
        request_charging = true;

        MaxSend = 3;
        timerValue = 3000;

        txtResult.setText("Press Start");

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.READ_PHONE_NUMBERS ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.SEND_SMS, android.Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                    0);
        }

        // 초기 setting txt 파일에서 읽어오기
        FileSetting = "setting2";
        String line = null;
        String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EVGPS"; // 저장 경로
        File dir = new File(LoadFolderPath);
        String temp = "";

        // 폴더 생성
        if (!dir.exists()) { // 폴더 없을 경우
            dir.mkdir(); // 폴더 생성
        }

        File file = new File(LoadFolderPath, FileSetting + ".txt");

        // 초기에 setting 파일 없으면 기본값으로 생성 후, 읽어오기 위한 flag
        Boolean flagFile = true;

        while (flagFile){
            try {
                BufferedReader buf = new BufferedReader(new FileReader(file));
                while ((line = buf.readLine()) != null) {
                    temp = line;
                }
                buf.close();
                flagFile = false;

            } catch (FileNotFoundException e) {
                e.printStackTrace();

                // 초기에 setting 파일 없으면 기본값으로 생성
                FileSetting = "setting2";
//                String setting_txt = serverIP + "," + serverPort + "," + range + "," + numinv + "," + target_lat1 + "," + target_long1 + "," + target_phone1 + "," + target_lat2 + "," + target_long2 + "," + target_phone2 + "," + target_lat3 + "," + target_long3 + "," + target_phone3;
//                WriteTxt("/EVGPS", FileSetting, setting_txt, false);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] setting_data= temp.split(",");

//        serverIP = setting_data[0];
//        serverPort = Integer.parseInt(setting_data[1]);
        range = Double.parseDouble(setting_data[2]);

        numinv = Integer.parseInt(setting_data[3]);

        target_lat1 = Double.parseDouble(setting_data[4]);
        target_long1 = Double.parseDouble(setting_data[5]);
        target_phone1 = setting_data[6];

        if (numinv>=2) {
            target_lat2 = Double.parseDouble(setting_data[7]);
            target_long2 = Double.parseDouble(setting_data[8]);
            target_phone2 = setting_data[9];
        }

        if (numinv>=3) {
            target_lat3 = Double.parseDouble(setting_data[10]);
            target_long3 = Double.parseDouble(setting_data[11]);
            target_phone3 = setting_data[12];
        }


//        try {
//            IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
//            registerReceiver(smsReceiver, smsFilter);
//        } catch(IllegalArgumentException e) {
//            e.printStackTrace();
//        }

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.READ_PHONE_NUMBERS ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.SEND_SMS , android.Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                            0);
                }
                else{

                    button1.setEnabled(false);
                    button2.setEnabled(true);
                    buttonSetting.setEnabled(false);

                    txtResult.setText("Finding GPS location");


                    // socket 통신 받은 메시지 위한 handler
                    Handler handler = new Handler(){
                        @Override
                        public void handleMessage(Message message)  {


                            // socket 통신 메시지 불러오기
                            String receive_msg = message.obj.toString();
                            Log.d("UDP",receive_msg);

                            // 원하는 element 찾기
                            int ind_p = receive_msg.indexOf("@P");
                            int ind_w = receive_msg.indexOf("@W");
                            int ind_i = receive_msg.indexOf("@I");
                            int ind_a = receive_msg.indexOf("@A");

                            if (ind_p != -1) {

                                try {
                                    // GPS 위치 (x)
                                    int ind_temp1 = receive_msg.indexOf(",", ind_p + 3);
                                    gps_x = Double.parseDouble(receive_msg.substring(ind_p + 3, ind_temp1));
                                    Log.d("UDP", "gpx_x : " + gps_x.toString());


                                    // GPS 위치 (y)
                                    int ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                    gps_y = Double.parseDouble(receive_msg.substring(ind_temp1 + 1, ind_temp2));
                                    Log.d("UDP", "gpx_y : " + gps_y.toString());

                                    // 속도
                                    ind_temp1 = ind_temp2;
                                    ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                    speed = Double.parseDouble(receive_msg.substring(ind_temp1 + 1, ind_temp2));
                                    Log.d("UDP", "speed : " + speed.toString());

                                    // 충전 요청 여부
                                    ind_temp1 = ind_temp2;
                                    ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                    request_charging = "1".equals(receive_msg.substring(ind_temp1 + 1, ind_temp2));
                                    Log.d("UDP", "request_charging : " + String.valueOf(request_charging));

                                    SendUDPdata("start,@A,0,P,end");
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(), "UDP Receive : @P Message ERROR", Toast.LENGTH_LONG).show();
                                }

                            }

                            if (ind_w != -1) {

                                try {
                                    // 핸드폰 번호
                                    int ind_temp1 = receive_msg.indexOf(",", ind_w + 3);
                                    SendPhoneNum = receive_msg.substring(ind_w + 3, ind_temp1);
                                    Log.d("UDP", "SendPhoneNum : " + SendPhoneNum);

                                    int ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                    String Wdata = receive_msg.substring(ind_temp1 + 1, ind_temp2);
                                    Log.d("UDP", "Wdata : " + Wdata);

//                                    SendSMSdata(SendPhoneNum, "@W," + Wdata);
                                    SendPushData(SendPhoneNum, "@W," + Wdata);
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(), "UDP Receive : @W Message ERROR", Toast.LENGTH_LONG).show();
                                }

                            }

                            if (ind_i != -1) {
                                try {
                                    // 패드 사이즈 (x)
                                    int ind_temp1 = receive_msg.indexOf(",", ind_i + 3);
                                    pad_x = Double.parseDouble(receive_msg.substring(ind_i + 3, ind_temp1));
                                    Log.d("UDP", "pad_x : " + pad_x.toString());

                                    // 패드 사이즈 (y)
                                    int ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                    pad_y = Double.parseDouble(receive_msg.substring(ind_temp1 + 1, ind_temp2));
                                    Log.d("UDP", "pad_y : " + pad_y.toString());

                                    SendUDPdata("start,@A,0,I,end");
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(), "UDP Receive : @I Message ERROR", Toast.LENGTH_LONG).show();
                                }

                            }

                            if (ind_a != 1) {
                                try {
                                    int ind_temp1 = receive_msg.indexOf(",", ind_a + 3);
                                    String Adata = receive_msg.substring(ind_a + 3, ind_temp1);

                                    int ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                    String Aid = receive_msg.substring(ind_temp1 + 1, ind_temp2);

                                    Log.d("UDPReceive", Adata);
                                    Log.d("UDPReceive", Aid);

                                    if (Aid.equals("W") && Adata.equals("1")) {
//                                        SendSMSdata(SendPhoneNum, "@A,2,W");
                                        SendPushData(SendPhoneNum, "@A,2,W");
                                    }

                                    if (Aid.equals("B") && Adata.equals("0")) {
                                        udpBflag = false;
                                    }
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(), "UDP Receive : @A Message ERROR", Toast.LENGTH_LONG).show();
                                }
                            }

                        }
                    };

                    startUDP = new StartUDP(handler);

                    //UDP 시작
                    startUDP.start();


                    long now = System.currentTimeMillis(); // 현재시간 받아오기

                    Date date = new Date(now); // Date 객체 생성

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String nowDate = sdf.format(date);

                    SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss:SSS");
                    String nowTime = sdf2.format(date);

                    Filename = "GPSlog_" + nowDate + "_" + nowTime;

                    // txt 파일로 출력
                    String GPSdata_file = "Date;Time;provider;latitude;longitude;altitude"  + "\n";

                    WriteTxt("/GPSlog", Filename, GPSdata_file,true);

                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1,
                            0,
                            gpsLocationListener);



                    // 초기 setting 전화번호 데이터 전송
                    udpBdata = "start,@B1," + target_phone1;

                    if (numinv>=2) {
                        udpBdata = udpBdata + ",@B2," + target_phone2;
                    }

                    if (numinv>=3) {
                        udpBdata = udpBdata + ",@B3," + target_phone3;
                    }

                    udpBdata = udpBdata+ ",end";
                    SendUDPdata(udpBdata);

                    bTimer = new Timer();
                    bTimer.schedule(new Btimer(), timerValue); // 3초 간격으로 실행
                    udpBtime = 1;
                    udpBflag = true;
                }

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lm.removeUpdates(gpsLocationListener);
                txtResult.setText("Terminated\nPress to restart");
                button1.setEnabled(true);
                button2.setEnabled(false);
                buttonSetting.setEnabled(true);
                socket.close();
                textSocket.setText("Socket Closed");
            }
        });


        buttonSetting.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Setting.class);
                intent.putExtra("ip", serverIP);
                intent.putExtra("port", serverPort);
                intent.putExtra("range", range);
                intent.putExtra("numinv", numinv);

                intent.putExtra("lat1", target_lat1);
                intent.putExtra("long1", target_long1);
                intent.putExtra("phone1",target_phone1);

                if (numinv >=3){
                    intent.putExtra("lat3", target_lat3);
                    intent.putExtra("long3", target_long3);
                    intent.putExtra("phone3",target_phone3);
                }

                if (numinv>=2) {
                    intent.putExtra("lat2", target_lat2);
                    intent.putExtra("long2", target_long2);
                    intent.putExtra("phone2",target_phone2);
                }

                startActivityForResult(intent, 1);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){

                // 세팅값 받기
                numinv = data.getIntExtra("numinv",1);
                serverIP = data.getStringExtra("ip");
                serverPort = data.getIntExtra("port",8011);
                range = data.getDoubleExtra("range",0);

                target_lat1 = data.getDoubleExtra("lat1",0);
                target_long1 = data.getDoubleExtra("long1",0);
                target_phone1 = data.getStringExtra("phone1");

                if (numinv>=2) {
                    target_lat2 = data.getDoubleExtra("lat2",0);
                    target_long2 = data.getDoubleExtra("long2",0);
                    target_phone2 = data.getStringExtra("phone2");
                }

                if(numinv >=3){
                    target_lat3 = data.getDoubleExtra("lat3",0);
                    target_long3 = data.getDoubleExtra("long3",0);
                    target_phone3 = data.getStringExtra("phone3");
                }

                Toast.makeText(getApplicationContext(), "Complete Setting", Toast.LENGTH_LONG).show();



                // setting txt 파일에 저장
                FileSetting = "setting2";
                String setting_txt = serverIP + "," + serverPort + "," +range+","+ numinv+","+target_lat1+","+target_long1+","+target_phone1+","+target_lat2+","+target_long2+","+target_phone2+","+target_lat3+","+target_long3+","+target_phone3;
                WriteTxt("/EVGPS", FileSetting, setting_txt,false);

            }
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SMS 전송 함수
    public void SendSMSdata(String phoneNum, String data){
        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(data);
        sms.sendMultipartTextMessage(phoneNum, null, parts, null, null);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Push 전송 함수
    public void SendPushData(String phoneNum, String data){
        Log.i("olev", s_phone);
        try{
            JSONObject params = new JSONObject();
            params.put("r_number", phoneNum);
            params.put("s_number", s_phone);
            params.put("data", data);
            HttpRequest.request(MainActivity.this, "POST", "/message", params);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }


    // UDP 전송 함수
    public void SendUDPdata (String data){
        sendUDP = new SendUDP();
        sendUDP.sendUDPmsg(data);
        sendUDP.start();
        Log.d("SendUDP", data);
    }


    // txt 입력 위한 함수 (filename은 .txt 제외하고 입력, flag는 true면 이어쓰기, flase면 덮어쓰기)
    public void WriteTxt(String directory, String filename, String data, Boolean flag){

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            String SaveSettingFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + directory;
            File dir = new File(SaveSettingFolderPath);

            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(SaveSettingFolderPath, filename + ".txt");

            try {
                FileWriter fws = new FileWriter(file, flag);
                fws.write(data);
                fws.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Save Exception", Toast.LENGTH_LONG).show();

            }
        }

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SMS 수신시 실행하는 동작 (수신한 내용을 intent로 받음)
    private void processIntent(Intent intent){
        if(intent != null) {
            String sender = intent.getStringExtra("sender"); // 수신 전화번호

            String contents = intent.getStringExtra("contents"); // 수신 내용
//segBuffer.indexOf("end") + 3
            if (contents.contains("@W")) {
                try {
                    String smsWdata = contents.substring(contents.indexOf("@W") + 3, contents.indexOf("end") - 1);
                    Log.d("contentsMAIN", smsWdata);

                    SendUDPdata("start,@W," + sender + "," + smsWdata + ",end");


                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "SMS Receive : @W Message ERROR", Toast.LENGTH_LONG).show();
                }
            }

            if (contents.contains("@A,2,W")) {
                SendUDPdata("start,@A,3,W,end");
            }

        }

    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }


    // 첫 번째 TimerTask 를 이용한 방법
    class Btimer extends TimerTask {
        @Override
        public void run() {
            if (udpBflag && udpBtime < MaxSend){
                SendUDPdata(udpBdata);
                udpBtime = udpBtime +1;
                bTimer = new Timer();
                bTimer.schedule(new Btimer(), timerValue); // 3초 간격으로 실행

                }
        }
    }


    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            long now = System.currentTimeMillis(); // 현재시간 받아오기

            Date date = new Date(now); // Date 객체 생성

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String nowDate = sdf.format(date);

            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss:SSS");
            String nowTime = sdf2.format(date);


            // 어플 화면에 출력
            String GPSdata = nowTime + "," + latitude + "," + longitude ;


            txtResult.setText(GPSdata);

            GPSdata = GPSdata + "," + speed + "," + gps_x + "," + gps_y;


            if(request_charging) {
                // 문자로 전송
                try {
                    double distance1 = 6371 * 2 * asin(Math.pow(sin((target_lat1 - latitude) / 2), 2) + cos(latitude) * cos(target_lat1) * Math.pow(sin((target_long1 - longitude) / 2), 2));
                    if (distance1 <= range) {
//                        SendSMSdata(target_phone1,GPSdata);
                        SendPushData(target_phone1, GPSdata);
                    }

                    if (numinv >= 2) {
                        double distance2 = 6371 * 2 * asin(Math.pow(sin((target_lat2 - latitude) / 2), 2) + cos(latitude) * cos(target_lat2) * Math.pow(sin((target_long2 - longitude) / 2), 2));
                        if (distance2 <= range) {
//                            SendSMSdata(target_phone2,GPSdata);
                            SendPushData(target_phone2,GPSdata);
                        }
                    }

                    if (numinv >= 3) {
                        double distance3 = 6371 * 2 * asin(Math.pow(sin((target_lat3 - latitude) / 2), 2) + cos(latitude) * cos(target_lat3) * Math.pow(sin((target_long3 - longitude) / 2), 2));
                        if (distance3 <= range) {
//                            SendSMSdata(target_phone3,GPSdata);
                            SendPushData(target_phone3,GPSdata);
                        }
                    }


                    // UDP socket
                    SendUDPdata("start,@P,1,end");

                } catch (Exception e) {

                    Toast.makeText(getApplicationContext(), "SMS ERROR\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                    SendUDPdata("start,@P,0,end");
                }
            }


            // txt 파일에 출력
            String GPSdata_file =  nowDate + ";" +
                    nowTime + ";" +
                    latitude + ";" +
                    longitude;

            WriteTxt("/GPSlog", Filename, GPSdata_file,true);

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };



    public class StartUDP extends Thread {

        private Handler handler;
        private String sendData = "";
        private String receive_msg;
        private Boolean loop = true;

        StartUDP(Handler handler ){
            this.handler = handler;
        }

        public void run() {

            try {
                receive_msg = "";
                sendData = "Connection request";

                //UDP 통신용 소켓 생성
                socket = new DatagramSocket();
                textSocket.setText("Socket Opened");

                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(serverIP);

                //보낼 데이터 생성
                byte[] buf = sendData.getBytes();

                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, serverPort);

                //패킷 전송!
                socket.send(packet);
                textSocket.setText("Send : " + sendData);

                loop = true;

                while (loop) {

                    byte[] buffer = new byte[2048];
                    packet = new DatagramPacket(buffer, buffer.length, serverAddr, serverPort);

                    // 데이터 수신
                    socket.receive(packet);

                    // 데이터 수신되었다면 문자열로 변환
                    receive_msg = new String(packet.getData());


                    if (receive_msg.contains("start") && receive_msg.contains("end")) {

                        //txtView에 표시
                        textSocket.setText("Receive : \n" + receive_msg);

                        Message msg = new Message();

                        msg.obj = receive_msg;
                        handler.sendMessage(msg);

                    } else {
                        textSocket.setText("Receive ERROR : \n" + receive_msg);
                    }

                }

            } catch (Exception e) {
                loop = false;
            }

        }

    };

    public class SendUDP extends Thread {

        private String sendData = "";

        public void sendUDPmsg(String str) {
            sendData = str;
        }

        public void run() {

            try {
                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(serverIP);

                //보낼 데이터 생성
                byte[] buf = sendData.getBytes();

                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, serverPort);

                //패킷 전송!
                socket.send(packet);
                textSocket.setText("Send : " + sendData);

            } catch (Exception e) {
                Log.d("UDP","UDP Send Exception");
            }

        }


    };


}


