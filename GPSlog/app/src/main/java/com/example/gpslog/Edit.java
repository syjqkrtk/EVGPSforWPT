// Edit.java
// Load, edit and Save GPS log and path files

// Import necessary packages and libraries
package com.example.gpslog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Define Edit class
public class Edit extends Activity {

    // Initialize global variables
    private Button graphCancel;
    private Button logLeft;
    private Button logRight;
    private Button saveLog;
    private Button delLog;
    private Button loadLog;
    private Button prevPath;
    private Button nextPath;
    private TextView logText;
    private TextView pathText;
    private SeekBar logStart;
    private SeekBar logEnd;
    private EditText numStart;
    private EditText numEnd;
    private FrameLayout graph;
    public int filecount = 0;
    public int pathcount = 0;
    public int count = 0;
    public int count2 = 0;
    public double set_radius;
    public double set_long;
    public double set_lat;
    private int start = 1;
    private int end = 1;
    public double[] log_lat = new double[10000];
    public double[] log_long = new double[10000];
    public double[] path_lat = new double[10000];
    public double[] path_long = new double[10000];
    private int currentfile=0;
    private int currentpath=0;
    private String[] GPSfiles = new String[10];
    private String[] Pathfiles = new String[10];
    myView mview;

    // Define onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Start event page
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_popup);

        // Get parameters form MainActivity class
        Intent intent = getIntent();
        set_lat = intent.getDoubleExtra("latitude",0);
        set_long = intent.getDoubleExtra("longitude",0);
        set_radius = intent.getDoubleExtra("radius",0.1);

        // Set display variables
        graphCancel = (Button) findViewById(R.id.graphCancel);
        logLeft = (Button) findViewById(R.id.LogLeft);
        logRight = (Button) findViewById(R.id.LogRight);
        saveLog = (Button) findViewById(R.id.SaveLog);
        delLog = (Button) findViewById(R.id.DelLog);
        loadLog = (Button) findViewById(R.id.LoadLog);
        prevPath = (Button) findViewById(R.id.PrevPath);
        nextPath = (Button) findViewById(R.id.NextPath);
        logText = (TextView) findViewById(R.id.LogText);
        pathText = (TextView) findViewById(R.id.PathText);
        logStart = (SeekBar) findViewById(R.id.LogStart);
        logEnd = (SeekBar) findViewById(R.id.LogEnd);
        numStart = (EditText) findViewById(R.id.NumStart);
        numEnd = (EditText) findViewById(R.id.NumEnd);

        // Check file control and GPS permission
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Edit.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }
        else {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                // Load GPS log folder and check filenames
                String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                File dir = new File(LoadFolderPath);

                if (!dir.exists()) {
                    dir.mkdir();
                }

                String[] filenames = dir.list();
                filecount = 0;
                pathcount = 0;
                for (String st : filenames) {
                    if (st.contains("GPSlog")) {
                        GPSfiles[filecount] = st;
                        filecount++;
                    }
                    if (st.contains("InverterPath")) {
                        Pathfiles[pathcount] = st;
                        pathcount++;
                    }
                }

                // Load current GPS log file
                try {
                    File file = new File(LoadFolderPath, GPSfiles[currentfile]);
                    String temp;
                    BufferedReader fr = new BufferedReader(new FileReader(file));
                    count = 0;
                    while ((temp = fr.readLine()) != null) {
                        String[] temp2 = temp.split(",");
                        log_long[count] = Double.parseDouble(temp2[0]);
                        log_lat[count] = Double.parseDouble(temp2[1]);
                        count++;
                    }
                    if (count == 0) {
                        Toast.makeText(getApplicationContext(), "비어있는 로그파일입니다. 다음 파일을 삭제합니다.\n" + GPSfiles[currentfile], Toast.LENGTH_SHORT).show();
                        fr.close();
                        file.delete();
                        finish();
                    }
                    fr.close();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "로그 파일을 먼저 생성해주세요.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    finish();
                }
            }
        }

        // Set display variables related to path
        logStart.setMin(1);
        logStart.setMax(count);
        logEnd.setMin(1);
        logEnd.setMax(count);
        logStart.incrementProgressBy(1);
        logEnd.incrementProgressBy(1);

        // Set graph display
        graph = (FrameLayout)findViewById(R.id.graph);
        mview = new myView(graph.getContext());
        graph.addView(mview);

        // Define CANCEL button
        graphCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "로그 설정에서 나갑니다.", Toast.LENGTH_SHORT).show();

                // Terminate Edit class
                finish();
            }
        });

        // Define logLeft button
        logLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentfile > 0) {
                    // Load previous GPS log file
                    currentfile--;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                        String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                        File dir = new File(LoadFolderPath);

                        if (!dir.exists()) {
                            dir.mkdir();
                        }

                        File file = new File(LoadFolderPath, GPSfiles[currentfile]);
                        try {
                            String temp;
                            BufferedReader fr = new BufferedReader(new FileReader(file));
                            count = 0;
                            while((temp = fr.readLine()) != null) {
                                String[] temp2 = temp.split(",");
                                log_long[count] = Double.parseDouble(temp2[0]);
                                log_lat[count] = Double.parseDouble(temp2[1]);
                                count++;
                            }
                            if (count == 0) {
                                Toast.makeText(getApplicationContext(), "비어있는 로그파일입니다. 다음 파일을 삭제합니다.\n" + GPSfiles[currentfile], Toast.LENGTH_SHORT).show();
                                fr.close();
                                file.delete();
                                finish();
                            }
                            fr.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // Change display as GPS log is changed
                    logText.setText("GPSlog " + Integer.toString(currentfile + 1));
                    logStart.setMax(count);
                    logEnd.setMax(count);
                    logStart.setProgress(1);
                    logEnd.setProgress(1);
                    graph.removeAllViews();
                    graph.addView(mview);
                }
            }
        });

        // Define logRight button
        logRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentfile < filecount-1) {
                    // Load next GPS log file
                    currentfile++;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                        String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                        File dir = new File(LoadFolderPath);

                        if (!dir.exists()) {
                            dir.mkdir();
                        }

                        File file = new File(LoadFolderPath, GPSfiles[currentfile]);
                        try {
                            String temp;
                            BufferedReader fr = new BufferedReader(new FileReader(file));
                            count = 0;
                            while((temp = fr.readLine()) != null) {
                                String[] temp2 = temp.split(",");
                                log_long[count] = Double.parseDouble(temp2[0]);
                                log_lat[count] = Double.parseDouble(temp2[1]);
                                count++;
                            }
                            if (count == 0) {
                                Toast.makeText(getApplicationContext(), "비어있는 로그파일입니다. 다음 파일을 삭제합니다.\n" + GPSfiles[currentfile], Toast.LENGTH_SHORT).show();
                                fr.close();
                                file.delete();
                                finish();
                            }
                            fr.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // Change display as GPS log is changed
                    logText.setText("GPSlog " + Integer.toString(currentfile+1));
                    logStart.setMax(count);
                    logEnd.setMax(count);
                    logStart.setProgress(1);
                    logEnd.setProgress(1);
                    graph.removeAllViews();
                    graph.addView(mview);
                }
            }
        });

        // Define saveLog button
        saveLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    // Define file name of path
                    String SaveFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                    File dir = new File(SaveFolderPath);

                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    long now = System.currentTimeMillis(); // 현재시간 받아오기
                    Date date = new Date(now); // Date 객체 생성
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String nowDate = sdf.format(date);
                    SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmssSSS");
                    String nowTime = sdf2.format(date);
                    //String Filename = "InverterPath_" + nowDate + "_" + nowTime;
                    String Filename = "InverterPath_" + Integer.toString(currentpath+1);

                    if (currentpath < pathcount) {
                        Filename = Pathfiles[currentpath];
                    }

                    // Save path into file
                    File file = new File(SaveFolderPath, Filename + ".txt");

                    try {
                        FileWriter fw = new FileWriter(file, false);
                        for (int i = start; i < end; i++){
                            String GPSdata_file = log_long[i] + "\t" + log_lat[i] + "\n";
                            fw.write(GPSdata_file);
                        }
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Save Exception", Toast.LENGTH_LONG).show();
                    }
                }

                // Reset paramters
                logStart.setProgress(1);
                logEnd.setProgress(1);
                Toast.makeText(getApplicationContext(), Integer.toString(currentpath+1)+"번째 경로를 저장했습니다.", Toast.LENGTH_SHORT).show();

                // Terminate Edit class
                finish();
            }
        });

        // Define delLog button
        delLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    // Define file name to delete
                    String SaveFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                    File dir = new File(SaveFolderPath);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    try {
                        File file = new File(SaveFolderPath, Pathfiles[currentpath]);
                        file.delete();
                        Toast.makeText(getApplicationContext(), Integer.toString(currentpath + 1) + "번째 경로를 삭제했습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    catch (Exception e) {
                        Toast.makeText(getApplicationContext(), Integer.toString(currentpath + 1) + "번째 경로는 아직 생성되지 않았습니다.", Toast.LENGTH_SHORT).show();
                        graph.removeAllViews();
                        graph.addView(mview);
                        e.printStackTrace();
                    }
                }
            }
        });

        // Define loadLog button
        loadLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    // Load path file
                    String LoadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InverterGPS";
                    File dir = new File(LoadFolderPath);

                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    try {
                        File file = new File(LoadFolderPath, Pathfiles[currentpath]);
                        String temp;
                        BufferedReader fr = new BufferedReader(new FileReader(file));
                        count2 = 0;
                        while((temp = fr.readLine()) != null) {
                            String[] temp2 = temp.split("\t");
                            path_long[count2] = Double.parseDouble(temp2[0]);
                            path_lat[count2] = Double.parseDouble(temp2[1]);
                            count2++;
                        }
                        fr.close();

                        // Reset graph display
                        graph.removeAllViews();
                        graph.addView(mview);
                        Toast.makeText(getApplicationContext(), Integer.toString(currentpath + 1) + "번째 경로를 불러왔습니다.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // If the currentpath is larger than file number, display error massage
                        count2 = 0;
                        Toast.makeText(getApplicationContext(), Integer.toString(currentpath + 1) + "번째 경로를 새로 만들어 저장하세요.", Toast.LENGTH_SHORT).show();
                        graph.removeAllViews();
                        graph.addView(mview);
                        e.printStackTrace();
                    }
                }
            }
        });

        // Define prevPath button
        prevPath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentpath > 0) {
                    // Change currentpath number to previous path
                    currentpath--;
                    pathText.setText("Path " + Integer.toString(currentpath + 1));
                    graph.removeAllViews();
                    graph.addView(mview);
                }
            }
        });

        // Define nextPath button
        nextPath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentpath == pathcount-1) {
                    // Change state to making path file state
                    currentpath++;
                    Toast.makeText(getApplicationContext(), Integer.toString(currentpath + 1) + "번째 경로를 새로 만들어 저장하세요.", Toast.LENGTH_SHORT).show();
                    pathText.setText("Path " + Integer.toString(currentpath + 1));
                    graph.removeAllViews();
                    graph.addView(mview);
                }
                if (currentpath < pathcount-1) {
                    // Change currentpath number to next path
                    currentpath++;
                    pathText.setText("Path " + Integer.toString(currentpath + 1));
                    graph.removeAllViews();
                    graph.addView(mview);
                }
            }
        });

        // Define logStart button
        logStart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Match progress bar with start variable
                start = progress;
                numStart.setText(Integer.toString(start));

                //Reset graph display
                graph.removeAllViews();
                graph.addView(mview);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Define logEnd button
        logEnd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Match progress bar with end variable
                end = progress;
                numEnd.setText(Integer.toString(end));

                //Reset graph display
                graph.removeAllViews();
                graph.addView(mview);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Define numStart EditText
        numStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    // Match EditText box with progressbar
                    start = Integer.parseInt(s.toString());
                    logStart.setProgress(start);
                    numStart.setSelection(numStart.getText().length());

                    //Reset graph display
                    graph.removeAllViews();
                    graph.addView(mview);
                }
                catch (Exception e) {
                    start = 1;
                    numStart.setText(Integer.toString(start));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
        });

        // Define numEnd EditText
        numEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    // Match EditText box with progressbar
                    end = Integer.parseInt(s.toString()); // 생성된 SeekBar의 thumb(설정 부분)을 움직여 설정한 progress 값을 mSeekBarVal에 저장
                    logEnd.setProgress(end); // Layout의 배경색이 변함으로써 확인 가능
                    numEnd.setSelection(numEnd.getText().length());

                    //Reset graph display
                    graph.removeAllViews();
                    graph.addView(mview);
                }
                catch (Exception e) {
                    end = 1;
                    numEnd.setText(Integer.toString(end));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
        });
    }

    // Define myView class
    public class myView extends View {
        public myView(Context context) {
            super(context);
        }

        // Define drawing function
        public void onDraw(Canvas canvas) {
            // Set background color
            canvas.drawColor(Color.LTGRAY);

            // Set GPS log's graphical parameters
            Paint MyPaint = new Paint();
            MyPaint.setDither(true);
            MyPaint.setStrokeWidth(1f);
            MyPaint.setStyle(Paint.Style.STROKE);
            MyPaint.setStrokeJoin(Paint.Join.ROUND);
            MyPaint.setStrokeCap(Paint.Cap.ROUND);
            MyPaint.setColor(Color.BLACK);

            // Set size of display box
            double min_long = min(log_long) - 0.001;
            double max_long = max(log_long) + 0.001;
            double min_lat = min(log_lat) - 0.001;
            double max_lat = max(log_lat) + 0.001;
            double center_long = (min_long+max_long)/2;
            double center_lat = (min_lat+max_lat)/2;
            double[] xysize = {max_long-min_long,max_lat-min_lat};
            double size = max(xysize);

            // Load GPS data into graph
            if (count > 0) {
                int[] graph_long = new int[count];
                int[] graph_lat = new int[count];

                // Resize GPS log data into display box
                for (int i = 0; i < count; i++) {
                    graph_long[i] = (int) Math.round((log_long[i] - center_long) / size * 1000.0 + 500.0);
                    graph_lat[i] = (int) Math.round((log_lat[i] - center_lat) / size * 1000.0 + 500.0);
                }

                // Draw lines between points in the GPS log
                Path path = new Path();
                path.moveTo(graph_lat[0],graph_long[0]);
                for (int i = 1; i < count; i++) {
                    path.lineTo(graph_lat[i],graph_long[i]);
                }
                canvas.drawPath(path,MyPaint);

                // If path file is loaded, draw loaded path graph
                if (count2 > 0) {
                    // Set loaded path's graphical parameters
                    Paint MyPaint3 = new Paint();
                    MyPaint3.setDither(true);
                    MyPaint3.setStrokeWidth(5f);
                    MyPaint3.setStyle(Paint.Style.STROKE);
                    MyPaint3.setStrokeJoin(Paint.Join.ROUND);
                    MyPaint3.setStrokeCap(Paint.Cap.ROUND);
                    MyPaint3.setColor(Color.BLUE);

                    int[] graph_long2 = new int[count2];
                    int[] graph_lat2 = new int[count2];

                    // Resize GPS log data into display box
                    for (int i = 0; i < count2; i++) {
                        graph_long2[i] = (int) Math.round((path_long[i] - center_long) / size * 1000.0 + 500.0);
                        graph_lat2[i] = (int) Math.round((path_lat[i] - center_lat) / size * 1000.0 + 500.0);
                    }

                    // Draw lines between points in the GPS log
                    Path path3 = new Path();
                    path3.moveTo(graph_lat2[0],graph_long2[0]);
                    for (int i = 0; i < count2; i++) {
                        path3.lineTo(graph_lat2[i],graph_long2[i]);
                    }
                    canvas.drawPath(path3,MyPaint3);
                }

                // Set current path's graphical parameters
                Paint MyPaint2 = new Paint();
                MyPaint2.setDither(true);
                MyPaint2.setStrokeWidth(5f);
                MyPaint2.setStyle(Paint.Style.STROKE);
                MyPaint2.setStrokeJoin(Paint.Join.ROUND);
                MyPaint2.setStrokeCap(Paint.Cap.ROUND);
                MyPaint2.setColor(Color.RED);

                // Draw lines between points in the GPS log
                Path path2 = new Path();
                path2.moveTo(graph_lat[start-1],graph_long[start-1]);
                for (int i = start; i < end; i++) {
                    path2.lineTo(graph_lat[i],graph_long[i]);
                }
                canvas.drawPath(path2,MyPaint2);

                // Resize inverter's GPS location data
                int center_y = (int) Math.round((set_long - center_long) / size * 1000.0 + 500.0);
                int center_x = (int) Math.round((set_lat - center_lat) / size * 1000.0 + 500.0);
                int radius = (int) Math.round(set_radius / size * 10.0);

                // Set inverter's graphical parameters
                Paint MyPaint4 = new Paint();
                MyPaint4.setDither(true);
                MyPaint4.setStrokeWidth(10f);
                MyPaint4.setStyle(Paint.Style.STROKE);
                MyPaint4.setStrokeJoin(Paint.Join.ROUND);
                MyPaint4.setStrokeCap(Paint.Cap.ROUND);
                MyPaint4.setColor(Color.YELLOW);
                canvas.drawPoint(center_x,center_y,MyPaint4);
                canvas.drawCircle(center_x,center_y,radius,MyPaint4);
                //Toast.makeText(getApplicationContext(), Integer.toString(center_x) + "," + Integer.toString(center_y) + "," +Integer.toString(radius), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Define max function
    public static double max(double n[]) {
        double max = n[0];

        for (int i = 1; i < n.length; i++)
            if ((n[i] > max) && (n[i] > 0)) max = n[i];

        return max;
    }

    // Define min function
    public static double min(double n[]) {
        double min = n[0];

        for (int i = 1; i < n.length; i++)
            if ((n[i] < min) && (n[i] > 0)) min = n[i];

        return min;
    }
}
