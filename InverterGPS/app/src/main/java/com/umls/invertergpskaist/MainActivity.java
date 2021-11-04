package com.umls.invertergpskaist;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity {

    double[] destinylati = new double[10];
    double[] destinylongi = new double[10];
    double[] destinylati2 = new double[10];
    double[] destinylongi2 = new double[10];
    int padnum = 1;
    int historylen = 3;
    double lanesizein = 2;
    double lanesizeout = 4;
    double stdist = 0;
    double eddist = 0;

    private double[] longis = new double[1000];
    private double[] latis = new double[1000];
    private int count;
    int historycount = 0;
    double[] longhistory = new double[historylen+1];
    double[] lathistory = new double[historylen+1];

    BroadcastReceiver smsReceiver;
    private Button buttonSetting;
    private Button buttonParameter;
    private Button LeftPad;
    private Button RightPad;
    private TextView CurrentPad;

    private ListView mainlist;
    public ArrayList<EditText> find = new ArrayList<>();
    public ArrayList<ListItem2> listViewItemList2 = new ArrayList<ListItem2>(); //리스트뷰
    private ArrayList<ListItem2> filteredItemList2 = listViewItemList2; //리스트뷰 임시저장소
    public ArrayList<String> find2 = new ArrayList<>();

    private ListAdapter2 adapter;
    private String time;
    private Double latitude;
    private Double longitude;
    private String rFilename;
    private String wFilename;
    public String[] car_PhoneNum = new String[10];
    public int[][] ONOFF = new int[10][10];
    public Double[][] Distance = new Double[10][10];
    public Double[][] Speed = new Double[10][10];
    public Double[][] Arrival = new Double[10][10];
    public Double[][] Arrival2 = new Double[10][10];
    private int currentpad = 0;
    private int setcount = 1;


    //////////////////////////////////// 소켓통신//////////////////////////////////////////////////////
    //서버주소
    public String serverIP = "143.248.230.195";
    //사용할 통신 포트
    public int serverPort = 8011;

    public DatagramSocket socket;

    public StartUDP startUDP;
    public SendUDP sendUDP;

    public double[] start_lat = new double[10];
    public double[] start_long = new double[10];
    public double[] end_lat = new double[10];
    public double[] end_long = new double[10];
    public double[] pad_width = new double[10];

    public double speed;
    public double gps_x;
    public double gps_y;

    public String SendPhoneNum;

    //////////////////////////////////// 소켓통신//////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        car_PhoneNum[0] = "01012345678";
        destinylati[0] = 36.35684399;
        destinylongi[0] = 127.3534751;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSetting = (Button)findViewById(R.id.buttonSetting);
        buttonParameter = (Button)findViewById(R.id.buttonParameter);
        LeftPad = (Button)findViewById(R.id.LeftPad);
        RightPad = (Button)findViewById(R.id.RightPad);
        CurrentPad=(TextView)findViewById(R.id.CurrentPad);
        mainlist=(ListView)findViewById(R.id.mainlist);


        boolean isChecked = false;
        while(!isChecked){
            if (checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                isChecked = true;
            }
            Log.i("olev", isChecked + "");
        }

        String s_phone = "";
        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        s_phone = mgr.getSimSerialNumber();
        Log.i("olev",s_phone);

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.READ_PHONE_NUMBERS ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.RECEIVE_SMS ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.READ_SMS ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.SEND_SMS ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.SEND_SMS,android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_SMS,android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }
        else {
            try {
                IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                registerReceiver(smsReceiver, smsFilter);
            } catch(IllegalArgumentException e) {

                e.printStackTrace();
            }

        }

        buttonSetting.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, (setcount)+ "개가 저장되있습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Setting.class);
                intent.putExtra("phoneNum",car_PhoneNum);
                intent.putExtra("num",setcount);
                startActivityForResult(intent, 1);

            }
        });

        buttonParameter.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetParameter.class);
                intent.putExtra("padnum", padnum);
                intent.putExtra("longitude", destinylongi);
                intent.putExtra("latitude", destinylati);
                intent.putExtra("longitude2", destinylongi2);
                intent.putExtra("latitude2", destinylati2);
                intent.putExtra("historylen", historylen);
                intent.putExtra("lanesizein", lanesizein);
                intent.putExtra("lanesizeout", lanesizeout);
                intent.putExtra("serverIP", serverIP);
                intent.putExtra("serverPort", serverPort);
                startActivityForResult(intent, 2);

            }
        });

        LeftPad.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentpad > 0) {
                    currentpad--;
                    CurrentPad.setText("Pad " + Integer.toString(currentpad+1));
                    adapter.setItem(ONOFF[currentpad], Distance[currentpad], Speed[currentpad], Arrival[currentpad], Arrival2[currentpad]);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        RightPad.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentpad < padnum-1) {
                    currentpad++;
                    CurrentPad.setText("Pad " + Integer.toString(currentpad+1));
                    adapter.setItem(ONOFF[currentpad], Distance[currentpad], Speed[currentpad], Arrival[currentpad], Arrival2[currentpad]);
                    adapter.notifyDataSetChanged();
                }

            }
        });

        //////////////////////////////////// 소켓통신//////////////////////////////////////////////////////
        //serverIP = "143.248.230.195";
        //serverPort = 8011;

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
                int ind_n = receive_msg.indexOf("@N");
                int ind_a = receive_msg.indexOf("@A");


                if (ind_p != -1) {
                    SendUDPdata("start,@A,0,P,end");
                    // 아직 쓸 일 없을듯
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

                        SendSMSdata(SendPhoneNum, "@W,"+Wdata);
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(), "UDP Receive : @W Message ERROR", Toast.LENGTH_LONG).show();
                    }

                }

                if (ind_n != -1) {

                    try {
                        int temp_index = 0;
                        while (true) {
                            temp_index = temp_index + 1;
                            int ind_start = receive_msg.indexOf("@N" + temp_index);

                            if (ind_start == -1) {
                                break;
                            } else {
                                //Log.d("UDP", "<<<<<< NumPad : " + temp_index +" >>>>>>");

                                // 패드 시작점 위도
                                int ind_temp1 = receive_msg.indexOf(",", ind_start + 4);
                                start_lat[temp_index-1] = Double.parseDouble(receive_msg.substring(ind_start + 4, ind_temp1));
                                //Log.d("UDP", "start_lat : " + String.valueOf(start_lat[temp_index]));

                                // 패드 시작점 경도
                                int ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                start_long[temp_index-1] = Double.parseDouble(receive_msg.substring(ind_temp1 + 1, ind_temp2));
                                //Log.d("UDP", "start_long : " + String.valueOf(start_long[temp_index]));

                                // 패드 끝점 위도
                                ind_temp1 = ind_temp2;
                                ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                end_lat[temp_index-1] = Double.parseDouble(receive_msg.substring(ind_temp1 + 1, ind_temp2));
                                //Log.d("UDP", "end_lat : " + String.valueOf(end_lat[temp_index]));

                                // 패드 끝점 경도
                                ind_temp1 = ind_temp2;
                                ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                end_long[temp_index-1] = Double.parseDouble(receive_msg.substring(ind_temp1 + 1, ind_temp2));
                                // Log.d("UDP", "end_long : " + String.valueOf(end_long[temp_index]));

                                // 패드 폭
                                ind_temp1 = ind_temp2;
                                ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                                pad_width[temp_index-1] = Double.parseDouble(receive_msg.substring(ind_temp1 + 1, ind_temp2));
                                //Log.d("UDP", "width : " + String.valueOf(pad_width[temp_index]));
                            }

                        }
                        SendUDPdata("start,@A,0,N,end");
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(), "UDP Receive : @N Message ERROR", Toast.LENGTH_LONG).show();
                    }
                }

                if (ind_a != 1) {
                    try {
                        int ind_temp1 = receive_msg.indexOf(",", ind_a + 3);
                        String Adata = receive_msg.substring(ind_a + 3, ind_temp1);

                        int ind_temp2 = receive_msg.indexOf(",", ind_temp1 + 1);
                        String Aid = receive_msg.substring(ind_temp1 + 1, ind_temp2);

                        if (Aid.equals("W") && Adata.equals("1")) {
                            SendSMSdata(SendPhoneNum, "@A,2,W");
                        }
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(), "UDP Receive : @A Message ERROR", Toast.LENGTH_LONG).show();
                    }
                }

            }
        };

        startUDP = new StartUDP(handler);

        //보내기 시작
        startUDP.start();


        //////////////////////////////////// 소켓통신 //////////////////////////////////////////////////////


            rFilename = "InverterPath_1";


        // txt 파일로 입력
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
            File dir = new File(LoadFolderPath);

            if (!dir.exists()){
                dir.mkdir();
            }

            File file = new File(LoadFolderPath, rFilename+".txt");
            try {
                count = 0;
                String temp;
                BufferedReader fr = new BufferedReader(new FileReader(file));
                while((temp = fr.readLine()) != null){
                    String[] temp1 = temp.split("\t");
                    double x1 = Double.parseDouble(temp1[0]);
                    double y1 = Double.parseDouble(temp1[1]);
                    longis[count++] = x1;
                    latis[count] = y1;
                }
                fr.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        rFilename = "setting";

        // txt 파일로 입력
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
            File dir = new File(LoadFolderPath);

            if (!dir.exists()){
                dir.mkdir();
            }

            File file = new File(LoadFolderPath, rFilename+".txt");
            try {
                setcount = 0;
                String temp;
                BufferedReader fr = new BufferedReader(new FileReader(file));
                temp = fr.readLine();
                setcount = Integer.parseInt(temp);
                for(int i = 0; i<setcount; i++){
                    temp = fr.readLine();
                    car_PhoneNum[i] = temp;
                }
                temp = fr.readLine();
                padnum = Integer.parseInt(temp);
                for(int i = 0; i<padnum; i++){
                    temp = fr.readLine();
                    String[] temp1 = temp.split(" ");
                    destinylongi[i] = Double.parseDouble(temp1[0]);
                    destinylati[i] = Double.parseDouble(temp1[1]);
                    temp = fr.readLine();
                    String[] temp2 = temp.split(" ");
                    destinylongi2[i] = Double.parseDouble(temp2[0]);
                    destinylati2[i] = Double.parseDouble(temp2[1]);
                }
                temp = fr.readLine();
                historylen = Integer.parseInt(temp);
                temp = fr.readLine();
                lanesizein = Double.parseDouble(temp);
                temp = fr.readLine();
                lanesizeout = Double.parseDouble(temp);
                temp = fr.readLine();
                serverIP = temp;
                temp = fr.readLine();
                serverPort = Integer.parseInt(temp);
                fr.close();
            } catch(IOException e) {
                e.printStackTrace();
            }


            // 저장된 핸드폰 번호 데이터 SBC에 전송
            if (car_PhoneNum[0] != null) {
                String settingPhoneNumb = "start,";
                int t = 1;
                for (t = 1; t < 11; t++) {
                    if (car_PhoneNum[t-1] == null) {
                        break;
                    } else {
                        settingPhoneNumb = settingPhoneNumb + "@B" + t + "," + car_PhoneNum[t-1] + ",";
                    }
                }

                settingPhoneNumb = settingPhoneNumb + "end";

                SendUDPdata(settingPhoneNumb);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                //데이터 받기
                car_PhoneNum = data.getStringArrayExtra("phoneNum");
                setcount = data.getIntExtra("num",1);

                wFilename = "setting";

                // txt 파일로 입력
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                    File dir = new File(LoadFolderPath);

                    if (!dir.exists()){
                        dir.mkdir();
                    }

                    File file = new File(LoadFolderPath, wFilename+".txt");
                    try {
                        FileWriter fw = new FileWriter(file, false);
                        String strtowrite = Integer.toString(setcount)+"\n";
                        for (int i=0;i<setcount;i++){
                            strtowrite = strtowrite + car_PhoneNum[i] + "\n";
                        }
                        strtowrite = strtowrite + Integer.toString(padnum)+"\n";
                        for (int i=0;i<padnum;i++){
                            strtowrite = strtowrite + destinylongi[i] + " " + destinylati[i] + "\n";
                            strtowrite = strtowrite + destinylongi2[i] + " " + destinylati2[i] + "\n";
                        }
                        strtowrite = strtowrite + Integer.toString(historylen)+"\n";
                        strtowrite = strtowrite + Double.toString(lanesizein)+"\n";
                        strtowrite = strtowrite + Double.toString(lanesizeout)+"\n";
                        strtowrite = strtowrite + serverIP+"\n";
                        strtowrite = strtowrite + Integer.toString(serverPort)+"\n";
                        fw.write(strtowrite);
                        fw.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Save Exception", Toast.LENGTH_LONG).show();

                    }
                }

                // 저장된 핸드폰 번호 데이터 SBC에 전송
                if (car_PhoneNum[0] != null) {
                    String settingPhoneNumb = "start,";
                    int t = 1;
                    for (t = 1; t < 11; t++) {
                        if (car_PhoneNum[t-1] == null) {
                            break;
                        } else {
                            settingPhoneNumb = settingPhoneNumb + "@B" + t + "," + car_PhoneNum[t-1] + ",";
                        }
                    }

                    settingPhoneNumb = settingPhoneNumb + "end";

                    SendUDPdata(settingPhoneNumb);
                }

                Toast.makeText(MainActivity.this, (setcount)+ "개가 저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode==2) {
            if(resultCode==RESULT_OK){
                //데이터 받기
                padnum = data.getIntExtra("padnum",1);
                destinylongi = data.getDoubleArrayExtra("longitude");
                destinylati = data.getDoubleArrayExtra("latitude");
                destinylongi2 = data.getDoubleArrayExtra("longitude2");
                destinylati2 = data.getDoubleArrayExtra("latitude2");
                historylen = data.getIntExtra("historylen",3);
                lanesizein = data.getDoubleExtra("lanesizein",2);
                lanesizeout = data.getDoubleExtra("lanesizeout",4);
                serverIP = data.getStringExtra("serverIP");
                serverPort = data.getIntExtra("serverPort",8011);

                wFilename = "setting";

                // txt 파일로 입력
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                    File dir = new File(LoadFolderPath);

                    if (!dir.exists()){
                        dir.mkdir();
                    }

                    File file = new File(LoadFolderPath, wFilename+".txt");
                    try {
                        FileWriter fw = new FileWriter(file, false);
                        String strtowrite = Integer.toString(setcount)+"\n";
                        for (int i=0;i<setcount;i++){
                            strtowrite = strtowrite + car_PhoneNum[i] + "\n";
                        }
                        strtowrite = strtowrite + Integer.toString(padnum)+"\n";
                        for (int i=0;i<padnum;i++){
                            strtowrite = strtowrite + destinylongi[i] + " " + destinylati[i] + "\n";
                            strtowrite = strtowrite + destinylongi2[i] + " " + destinylati2[i] + "\n";
                        }
                        strtowrite = strtowrite + Integer.toString(historylen)+"\n";
                        strtowrite = strtowrite + Double.toString(lanesizein)+"\n";
                        strtowrite = strtowrite + Double.toString(lanesizeout)+"\n";
                        strtowrite = strtowrite + serverIP+"\n";
                        strtowrite = strtowrite + Integer.toString(serverPort)+"\n";
                        fw.write(strtowrite);
                        fw.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Save Exception", Toast.LENGTH_LONG).show();

                    }
                }

                Toast.makeText(MainActivity.this, (setcount)+ "개가 저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processIntent(Intent intent){

        if(intent != null){

            Log.d("SMSmain", "called");

            String sender = intent.getStringExtra("sender"); // 수신 전화번호
            Log.d("SMSmain", sender);

            String contents = intent.getStringExtra("contents"); // 수신 내용
            Log.d("SMSmain", contents);

            if (contents.contains("@W")) {
                String smsWdata = contents.substring(contents.indexOf("@W") + 3, contents.indexOf("end") - 1); //contents.substring(contents.indexOf("@W")+3,contents.length());
                Log.d("contentsMAIN", smsWdata);
                SendUDPdata("start,@W,"+sender+","+smsWdata+",end");
            }

            if (contents.contains("@A,2,W")) {
                SendUDPdata("start,@A,3,W,end");
            }


            if (Arrays.asList(car_PhoneNum).contains(sender)) {
                int position = Arrays.asList(car_PhoneNum).indexOf(sender);

                // 수신 내용에서 추출
                int index = contents.indexOf(",");
                time = contents.substring(0, index - 1); // 변환 완료값
                contents = contents.substring(index + 1, contents.length());

                index = contents.indexOf(",");
                latitude = Double.parseDouble(contents.substring(0, index - 1)); // 변환 완료값
                contents = contents.substring(index + 1, contents.length());

                index = contents.indexOf(",");
                longitude = Double.parseDouble(contents.substring(0, index - 1)); // 변환 완료값
                contents = contents.substring(index + 1, contents.length());

                ///////////////////////////// 차량 SBC 에서 받은거 /////////////////////////////////////////////////////
                // 속도
                index = contents.indexOf(",");
                try {
                    speed = Double.parseDouble(contents.substring(0, index - 1)); // 변환 완료값
                }
                catch (Exception e) {
                    speed = 0;
                }
                contents = contents.substring(index + 1, contents.length());
                //Log.d("speed", String.valueOf(speed));

                // 차량 GPS 위치 (x, y) 좌표 (패드 중앙 기준)
                index = contents.indexOf(",");
                try {
                    gps_x = Double.parseDouble(contents.substring(0, index - 1)); // 변환 완료값
                    gps_y = Double.parseDouble(contents.substring(index + 1, contents.length())); // 변환 완료값
                }
                catch (Exception e) {
                    gps_x = 0;
                    gps_y = 0;
                }//Log.d("gps_x", String.valueOf(gps_x));
                //Log.d("gps_y", String.valueOf(gps_y));
                ///////////////////////////// 차량 SBC 에서 받은거 /////////////////////////////////////////////////////

                if (historylen > 0) {
                    for (int i = historylen; i > 0; i--) {
                        longhistory[i] = longhistory[i - 1];
                        lathistory[i] = lathistory[i - 1];
                    }
                }
                longhistory[0] = longitude;
                lathistory[0] = longitude;
                historycount++;

                if (historycount > 1 && count > 0) {
                    long now = System.currentTimeMillis(); // 현재시간 받아오기

                    Date date = new Date(now); // Date 객체 생성

                    SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss:SSS");
                    String nowTime = sdf2.format(date);
                    double hour, minute, second, milsec;

                    index = nowTime.indexOf(":");
                    try {
                        hour = Double.parseDouble(nowTime.substring(0, index - 1)); // 변환 완료값
                    }
                    catch (Exception e) {
                        hour = 0;
                    }
                    nowTime = nowTime.substring(index + 1, nowTime.length());

                    index = nowTime.indexOf(":");
                    try {
                        minute = Double.parseDouble(nowTime.substring(0, index - 1)); // 변환 완료값
                    }
                    catch (Exception e) {
                        minute = 0;
                    }
                    nowTime = nowTime.substring(index + 1, nowTime.length());

                    index = nowTime.indexOf(":");
                    try {
                        second = Double.parseDouble(nowTime.substring(0, index - 1)); // 변환 완료값
                    }
                    catch (Exception e) {
                        second = 0;
                    }
                    nowTime = nowTime.substring(index + 1, nowTime.length());

                    index = nowTime.indexOf(":");
                    try {
                        milsec = Double.parseDouble(nowTime.substring(0, index - 1)); // 변환 완료값
                    }
                    catch (Exception e) {
                        milsec = 0;
                    }
                    nowTime = nowTime.substring(index + 1, nowTime.length());
                    double numtime = 3600*hour+60*minute+second+milsec/1000;

                    String time2 = time;
                    index = time2.indexOf(":");
                    try {
                        hour = Double.parseDouble(time2.substring(0, index - 1)); // 변환 완료값
                    }
                    catch (Exception e) {
                        hour = 0;
                    }
                    time2 = time2.substring(index + 1, time2.length());

                    index = time2.indexOf(":");
                    try {
                        minute = Double.parseDouble(time2.substring(0, index - 1)); // 변환 완료값
                    }
                    catch (Exception e) {
                        minute = 0;
                    }
                    time2 = time2.substring(index + 1, time2.length());

                    index = time2.indexOf(":");
                    try {
                        second = Double.parseDouble(time2.substring(0, index - 1)); // 변환 완료값
                    }
                    catch (Exception e) {
                        second = 0;
                    }
                    time2 = time2.substring(index + 1, time2.length());

                    index = time2.indexOf(":");
                    try {
                        milsec = Double.parseDouble(time2.substring(0, index - 1)); // 변환 완료값
                    }
                    catch (Exception e) {
                        milsec = 0;
                    }
                    time2 = time2.substring(index + 1, time2.length());
                    double numtime2 = 3600*hour+60*minute+second+milsec/1000;

                    double deltime = numtime-numtime2;
                    if (deltime < -80000) {
                        deltime += 3600*24;
                    }

                    for (int i = 0; i < padnum; i++) {
                        ONOFF[i][position] = get_lane_distance(longitude, latitude, ONOFF[i][position], i);
                        Distance[i][position] = get_distance(longitude, latitude, i);
                        if (speed == 0) {
                            Speed[i][position] = get_speed();
                        }
                        else {
                            Speed[i][position] = speed;
                        }
                        /*
                        Arrival[i][position] = get_predicted_time(longitude, latitude, i, Speed[i][position]);
                        if (Arrival[i][position]==0 || Arrival[i][position]==-1) {
                            Arrival2[i][position] = Arrival[i][position];
                        }
                        else {
                            Arrival[i][position] = Arrival[i][position] - deltime;
                            Arrival2[i][position] = Arrival[i][position] + gps_distance(destinylongi[i], destinylati[i], destinylongi2[i], destinylati2[i])/Speed[i][position];
                            Toast.makeText(MainActivity.this,Double.toString(Arrival[i][position])+","+Double.toString(Arrival2[i][position])+","+Double.toString(gps_distance(destinylongi[i], destinylati[i], destinylongi2[i], destinylati2[i])/Speed[i][position]), Toast.LENGTH_SHORT).show();
                        }
                        */
                        Arrival[i][position] = get_predicted_time(longitude, latitude, i, Speed[i][position]);
                        Arrival2[i][position] = get_predicted_time2(longitude, latitude, i, Speed[i][position]);
                        adapter = new ListAdapter2();
                    }
                    adapter.setItem(ONOFF[currentpad], Distance[currentpad], Speed[currentpad], Arrival[currentpad], Arrival2[currentpad]);
                    mainlist.setAdapter(adapter);
                    //Toast.makeText(MainActivity.this,UDPstring, Toast.LENGTH_SHORT).show();


                    String UDPstring = "start";
                    for (int j = 0; j < setcount; j++){
                        UDPstring += ",@V"+Integer.toString(j+1)+","+car_PhoneNum[j];
                        for (int i = 0; i < padnum; i ++) {
                            if (Arrival[i][j] == null){
                                UDPstring += "," + 0 + "," + 0;
                            }
                            else{
                                UDPstring += "," + Arrival[i][j] * 1000 + "," + Arrival2[i][j] * 1000;
                            }
                        }
                        if (Arrival[0][j] == null) {
                            UDPstring += "," + 0;
                        }
                        else{
                            UDPstring += "," + ONOFF[0][j];
                        }
                        UDPstring += ","+time;
                        UDPstring += ","+latitude;
                        UDPstring += ","+longitude;
                    }
                    UDPstring += ",end";
                    SendUDPdata(UDPstring);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    int get_lane_distance(double longi, double lati, int onoff, int pad) {
        /*
        double min_dist_null[] = new double[count-1];
        for(int i = 0; i < count-1; i++) {
            min_dist_null[i] = log_lanedistance(longi,lati,longis[i],latis[i],longis[i+1],latis[i+1]);
        }

        Arrays.sort(min_dist_null);
        if (onoff==0) {
            if (min_dist_null[0] <= lanesizein) {
                return 1;
            } else {
                return 0;
            }
        }
        else {
            if (min_dist_null[0] <= lanesizeout) {
                return 1;
            } else {
                return 0;
            }
        }
        */
        double startdistance = gps_distance(longi, lati, destinylongi[pad], destinylati[pad]);
        double enddistance = gps_distance(longi, lati, destinylongi2[pad], destinylati2[pad]);
        double virtualdistance1 = gps_distance(longi, lati, 2 * destinylongi[pad] - destinylongi2[pad], 2 * destinylati[pad] - destinylati2[pad]);
        double virtualdistance2 = gps_distance(longi, lati, 2 * destinylongi2[pad] - destinylongi[pad], 2 * destinylati2[pad] - destinylati[pad]);
        stdist = startdistance;
        eddist = enddistance;
        if (((startdistance <= virtualdistance2) && (enddistance <= virtualdistance1) && (startdistance <= 200) && (enddistance <= 200)) || (startdistance <= lanesizein) || (enddistance <= lanesizeout)) {
            return 1;
        } else {
            return 0;
        }
    }


    double get_distance(double longi, double lati, int pad) {
        /*
        double min_dist_null[] = new double[count-1];
        for(int i = 0; i < count-1; i++) {
            min_dist_null[i] = log_distance(longi,lati,longis[i],latis[i],longis[i+1],latis[i+1],pad);
        }

        Arrays.sort(min_dist_null);
        double startdistance = gps_distance(longi,lati,destinylongi[pad], destinylati[pad]);
        double enddistance = gps_distance(longi,lati,destinylongi2[pad], destinylati2[pad]);
        if (startdistance < enddistance) {
            if (min_dist_null[0] == 100000){
                return gps_distance(longi,lati,destinylongi[pad], destinylati[pad]);
            }
            else {
                return min_dist_null[0];
            }
        }
        else {
            return -1;
        }
        */
        double virtualdistance = gps_distance(longi,lati,2*destinylongi[pad]-destinylongi2[pad], 2*destinylati[pad]-destinylati2[pad]);
        //double startdistance = gps_distance(longi,lati,destinylongi[pad], destinylati[pad]);
        double enddistance = gps_distance(longi,lati,destinylongi2[pad], destinylati2[pad]);
        if (virtualdistance < enddistance) {
            return gps_distance(longi, lati, destinylongi[pad], destinylati[pad]);
        }
        else {
            return -1;
        }
    }

    double get_distance2(double longi, double lati, int pad) {
        double virtualdistance = gps_distance(longi,lati,2*destinylongi2[pad]-destinylongi[pad], 2*destinylati2[pad]-destinylati[pad]);
        double startdistance = gps_distance(longi,lati,destinylongi[pad], destinylati[pad]);
        //double enddistance = gps_distance(longi,lati,destinylongi2[pad], destinylati2[pad]);
        if (virtualdistance > startdistance) {
            return gps_distance(longi, lati, destinylongi2[pad], destinylati2[pad]);
        }
        else {
            return -1;
        }
    }

    double log_lanedistance(double longi, double lati, double x1, double y1, double x2, double y2) {
        /*
        double new_x;
        double new_y;
        double d;

        if (y1 != y2) {
            double a = (x2-x1) / (y2-y1);
            double b = x1 - a * y1;

            new_y = (a*longi+lati-a*b)/(Math.pow(a,2)+1);
            new_x = a*new_y+b;
            //String text = Double.toString(x1)+' '+Double.toString(y1)+' '+Double.toString(x2)+' '+Double.toString(y2)+' '+Double.toString(longi)+' '+Double.toString(lati);
            //txtResult2.setText(text);
        }
        else {
            new_y = y1;
            new_x = longi;
        }

        if ((new_x-x1)*(new_x-x2) > 0) {
            //d = gps_distance(new_x,new_y,longi,lati);
            d = 100000;
        }
        else {
            d = gps_distance(new_x,new_y,longi,lati);
        }

        return d;
        */
        return 0;
    }

    double log_distance(double longi, double lati, double x1, double y1, double x2, double y2, int pad) {
        double new_x, new_x2;
        double new_y, new_y2;
        double d;

        double a = (x2-x1) / (y2-y1);
        double b = x1 - a * y1;

        if (y1 != y2) {
            new_y = (a*longi+lati-a*b)/(Math.pow(a,2)+1);
            new_x = a*new_y+b;
            //String text = Double.toString(x1)+' '+Double.toString(y1)+' '+Double.toString(x2)+' '+Double.toString(y2)+' '+Double.toString(longi)+' '+Double.toString(lati);
            //txtResult2.setText(text);
        }
        else {
            new_y = y1;
            new_x = longi;
        }

        if ((new_x-x1)*(new_x-x2) > 0) {
            d = 100000;
        }
        else {
            new_y2 = (a*destinylongi[pad]+destinylati[pad]-a*b)/(Math.pow(a,2)+1);
            new_x2 = a*new_y2+b;
            d = gps_distance(new_x,new_y,new_x2,new_y2);
        }
        return d;
    }

    double gps_distance(double longi1, double lati1, double longi2, double lati2) {
        lati1 = lati1 * Math.PI / 180;
        lati2 = lati2 * Math.PI / 180;
        longi1 = longi1 * Math.PI / 180;
        longi2 = longi2 * Math.PI / 180;

        double lati_diff = (lati1-lati2);
        double longi_diff = (longi1-longi2);

        double a = Math.pow(Math.sin(lati_diff/2),2)+Math.pow(Math.sin(longi_diff/2),2)*Math.cos(lati1)*Math.cos(lati2);
        double c = 2*6378.137*1000*Math.atan2(Math.pow(a,0.5),Math.pow(1-a,0.5));
        //String text = Double.toString(a)+' '+Double.toString(c);
        //txtResult2.setText(text);

        return c;
    }

    double get_speed() {
        double distance = 0;
        double velocity;

        for (int i = 0; i< min(historylen,historycount-1); i++) {
            distance += gps_distance(longhistory[i],lathistory[i],longhistory[i+1],lathistory[i+1]);
        }

        velocity = distance / min(historylen,historycount-1);

        return velocity;
    }

    double get_predicted_time(double longi, double lati, int pad, double velocity) {
        double predicted_distance = get_distance(longi, lati, pad);
        double predicted_time = predicted_distance / velocity;

        if (velocity == 0) {
            return -1;
        }
        else {
            if (predicted_distance == -1) {
                return 0;
            }
            else {
                return predicted_time;
            }
        }
    }

    double get_predicted_time2(double longi, double lati, int pad, double velocity) {
        double predicted_distance = get_distance2(longi, lati, pad);
        double predicted_time = predicted_distance / velocity;

        if (velocity == 0) {
            return -1;
        }
        else {
            if (predicted_distance == -1) {
                return 0;
            }
            else {
                return predicted_time;
            }
        }
    }



    //////////////////////////////////// 소켓통신 //////////////////////////////////////////////////////
    // SMS 전송 함수
    public void SendSMSdata(String phoneNum, String data){
        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(data);
        sms.sendMultipartTextMessage(phoneNum, null, parts, null, null);
    }

    // UDP 전송 함수
    public void SendUDPdata (String data){
        sendUDP = new SendUDP();
        sendUDP.sendUDPmsg(data);
        sendUDP.start();
    }

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

                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(serverIP);

                //보낼 데이터 생성
                byte[] buf = sendData.getBytes();

                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, serverPort);

                //패킷 전송!
                socket.send(packet);
                Log.d("UDP", "port number : " + socket.getPort());

                loop = true;

                while (loop) {

                    byte[] buffer = new byte[2048];
                    packet = new DatagramPacket(buffer, buffer.length, serverAddr, serverPort);

                    // 데이터 수신
                    socket.receive(packet);

                    // 데이터 수신되었다면 문자열로 변환
                    receive_msg = new String(packet.getData());


                    if (receive_msg.contains("start") && receive_msg.contains("end")) {


                        Message msg = new Message();

                        msg.obj = receive_msg;
                        handler.sendMessage(msg);

                    } else {
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

            } catch (Exception e) {
                Log.d("UDP","UDP Send Exception");
            }

        }


    };
    //////////////////////////////////// 소켓통신 //////////////////////////////////////////////////////


    //어뎁터 시작
    public class ListAdapter2 extends BaseAdapter {

        @Override
        public int getCount() {
            return filteredItemList2.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredItemList2.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();
            final ViewHolder2 holder;

            if (convertView == null) {
                holder = new ViewHolder2();
                LayoutInflater inflater =
                        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.main_listview_list, parent, false);
                holder.TextView0 = (TextView)convertView.findViewById(R.id.textView0);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder2)convertView.getTag();
            }

            holder.ref = position;

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            final TextView textView0 = (TextView) convertView.findViewById(R.id.textView0);
            final TextView textView = (TextView) convertView.findViewById(R.id.textView);
            final TextView textView2 = (TextView) convertView.findViewById(R.id.textView2);
            final TextView textView3 = (TextView) convertView.findViewById(R.id.textView3);
            final TextView textView4 = (TextView) convertView.findViewById(R.id.textView4);
            final TextView textView5 = (TextView) convertView.findViewById(R.id.textView5);

            // Data Set(filteredItemList)에서 position에 위치한 데이터 참조 획득
            final ListItem2 listViewItem = filteredItemList2.get(position);

            textView0.setText("Car"+(position+1));
            textView.setText(listViewItem.getON());
            try {
                if (listViewItem.getDistance()==-1) {
                    textView2.setText("PASS");
                }
                else {
                    textView2.setText(String.format("%.2f",listViewItem.getDistance()));
                }
                textView3.setText(String.format("%.2f",listViewItem.getSpeed()));
                textView4.setText(String.format("%.2f",stdist));
                textView5.setText(String.format("%.2f",eddist));
                /*
                if (listViewItem.getArrival()==-1) {
                    textView4.setText("Inf");
                }
                else {
                    if (listViewItem.getArrival()<=0) {
                        textView4.setText("PASS");
                    }
                    else {
                        textView4.setText(String.format("%.2f",listViewItem.getArrival()));
                    }
                }
                if (listViewItem.getArrival2()==-1) {
                    textView5.setText("Inf");
                }
                else {
                    if (listViewItem.getArrival2()<=0) {
                        textView5.setText("PASS");
                    }
                    else {
                        textView5.setText(String.format("%.2f",listViewItem.getArrival2()));
                    }
                }
                */
            }
            catch (Exception e) {

            }

            return convertView;
        }

        public void setItem(int[] ONOFF, Double[] Distance, Double[] Speed, Double[] Arrival, Double[] Arrival2) {
            //Toast.makeText(MainActivity.this,(Arrival[0])+ " 결과", Toast.LENGTH_SHORT).show();
            listViewItemList2.clear();
            for (int i = 0;i<setcount;i++) {
                ListItem2 item = new ListItem2();
                item.setON(ONOFF[i]);
                item.setDistance(Distance[i]);
                item.setSpeed(Speed[i]);
                item.setArrival(Arrival[i]);
                item.setArrival2(Arrival2[i]);

                listViewItemList2.add(item);
            }
        }
    }
}
