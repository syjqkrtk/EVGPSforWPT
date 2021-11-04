// MainActivity.java
// Display current GPS location
// Control all parameters and event pages (classes)

// Import necessary packages and libraries
package com.example.gpslog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Define MainActivity class
public class MainActivity extends AppCompatActivity {

    // Initialize global variables
    private Button button1;
    private Button button2;
    private TextView txtResult;
    private String Filename;
    private String sFilename;
    private Button buttonSetting;
    private Button buttonEdit;
    double defaultlati = 36.35684399;
    double defaultlongi = 127.3534751;
    public double radius = 0.1;
    public double target_long = 127.3534751;
    public double target_lat = 36.35684399;

    // Define onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Start event page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set display variables
        button1 = (Button)findViewById(R.id.button1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        button2 = (Button)findViewById(R.id.button2);
        button2.setEnabled(false);
        txtResult = (TextView)findViewById(R.id.txtResult);
        buttonSetting = (Button)findViewById(R.id.buttonSetting);
        buttonEdit = (Button)findViewById(R.id.buttonEdit);
        txtResult.setText("Press Start");

        // Load GPS function
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define START button
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check file control and GPS permission
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            0);
                }
                else{

                    // Change display
                    button1.setEnabled(false);
                    button2.setEnabled(true);
                    buttonSetting.setEnabled(false);
                    buttonEdit.setEnabled(false);
                    txtResult.setText("Finding GPS location");

                    // Define Filename to save GPS log
                    long now = System.currentTimeMillis(); // 현재시간 받아오기
                    Date date = new Date(now); // Date 객체 생성
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String nowDate = sdf.format(date);
                    SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmssSSS");
                    String nowTime = sdf2.format(date);
                    Filename = "GPSlog_" + nowDate + "_" + nowTime;

                    // Make GPS log file
                    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String SaveFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                        File dir = new File(SaveFolderPath);

                        if (!dir.exists()){
                            dir.mkdir();
                        }

                        File file = new File(SaveFolderPath, Filename+".txt");

                        try {
                            FileWriter fw = new FileWriter(file, true);
                            fw.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Save Exception", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Save ERROR", Toast.LENGTH_LONG).show();
                    }

                    // Load setting parameters from file
                    sFilename = "setting3";
                    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                        File dir = new File(LoadFolderPath);

                        if (!dir.exists()){
                            dir.mkdir();
                        }

                        File file = new File(LoadFolderPath, sFilename+".txt");
                        try {
                            String temp;
                            BufferedReader fr = new BufferedReader(new FileReader(file));
                            temp = fr.readLine();
                            target_long = Double.parseDouble(temp);
                            temp = fr.readLine();
                            target_lat = Double.parseDouble(temp);
                            temp = fr.readLine();
                            radius = Double.parseDouble(temp);
                            fr.close();
                        }
                        catch(IOException e) {
                                e.printStackTrace();
                        }
                    }

                    // Renew GPS location
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1,
                            0,
                            gpsLocationListener);
                }
            }
        });

        // Define END button
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check GPS function control permission
                if (Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            0);
                } else {
                    lm.removeUpdates(gpsLocationListener);
                    txtResult.setText("");
                    txtResult.setText("Terminated\nPress to restart");
                    button1.setEnabled(true);
                    button2.setEnabled(false);
                    buttonSetting.setEnabled(true);
                    buttonEdit.setEnabled(true);
                }
            }
        });

        // Define SETTING button
        buttonSetting.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send parameters to Setting class
                Intent intent = new Intent(MainActivity.this, Setting.class);
                intent.putExtra("latitude", target_lat);
                intent.putExtra("longitude", target_long);
                intent.putExtra("radius",radius);
                startActivityForResult(intent, 1);

            }
        });

        // Define EDIT button
        buttonEdit.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send parameters to Edit class
                Intent intent = new Intent(MainActivity.this, Edit.class);
                intent.putExtra("latitude", target_lat);
                intent.putExtra("longitude", target_long);
                intent.putExtra("radius",radius);
                startActivityForResult(intent, 2);

            }
        });

    }

    // Define parameter receiver
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                // Receive parameters from Setting or Edit class
                target_long = data.getDoubleExtra("longitude",defaultlongi);
                target_lat = data.getDoubleExtra("latitude",defaultlati);
                radius = data.getDoubleExtra("radius",0.1);

                // Save parameters into file
                sFilename = "setting3";
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                    File dir = new File(LoadFolderPath);

                    if (!dir.exists()){
                        dir.mkdir();
                    }

                    File file = new File(LoadFolderPath, sFilename+".txt");
                    try {
                        FileWriter fw = new FileWriter(file, false);
                        String strtowrite = Double.toString(target_long)+"\n";
                        strtowrite = strtowrite + Double.toString(target_lat)+"\n";
                        strtowrite = strtowrite + Double.toString(radius)+"\n";
                        fw.write(strtowrite);
                        fw.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Save Exception", Toast.LENGTH_LONG).show();

                    }
                }
                Toast.makeText(MainActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Define gpsLocationListener
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Get GPS location from android
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            // Display current GPS location
            long now = System.currentTimeMillis(); // 현재시간 받아오기
            Date date = new Date(now); // Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String nowDate = sdf.format(date);
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss:SSS");
            String nowTime = sdf2.format(date);
            String GPSdata = "   Longitude : " + longitude + "\n" +
                             "   Latitude : " + latitude + "\n\n";
            txtResult.setText(GPSdata);

            // Save current GPS location
            String GPSdata_file = longitude + "," + latitude + "\n";
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String SaveFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                File dir = new File(SaveFolderPath);

                if (!dir.exists()){
                    dir.mkdir();
                }

                File file = new File(SaveFolderPath, Filename+".txt");

                try {
                    FileWriter fw = new FileWriter(file, true);
                    fw.write(GPSdata_file);
                    fw.close();
                } catch(IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Save Exception", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Save ERROR", Toast.LENGTH_LONG).show();
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
}

