package com.umls.invertergpskaist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SetParameter extends AppCompatActivity {

    double defaultlati = 36.35684399;
    double defaultlongi = 127.3534751;
    private ListAdapter3 adapter;
    private TextView GPSshow;
    private EditText historylen;
    private EditText lanesizein;
    private EditText lanesizeout;
    private EditText ServerIP;
    private EditText ServerPort;
    private Button buttonAddPad;
    private Button buttonRemovePad;
    private Button buttonSave;
    private Button buttonCancel;
    public double[] set_long;
    public double[] set_lat;
    public double[] set_long2;
    public double[] set_lat2;
    public double[] target_long;
    public double[] target_lat;
    public double[] target_long2;
    public double[] target_lat2;
    public int set_historylen;
    public double set_lanesizein;
    public double set_lanesizeout;
    public String set_serverIP;
    public int set_serverPort;
    public double longi = 0;
    public double lati = 0;
    public int padnum = 1;

    private ListView paramlist;
    public ArrayList<EditText> find = new ArrayList<>();
    public ArrayList<ListItem3> listViewItemList3 = new ArrayList<ListItem3>(); //리스트뷰
    private ArrayList<ListItem3> filteredItemList3 = listViewItemList3; //리스트뷰 임시저장소
    public ArrayList<String>find2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.param_popup);

        //데이터 가져오기
        Intent intent = getIntent();
        padnum = intent.getIntExtra("padnum",1);
        target_long = intent.getDoubleArrayExtra("longitude");
        target_lat = intent.getDoubleArrayExtra("latitude");
        target_long2 = intent.getDoubleArrayExtra("longitude2");
        target_lat2 = intent.getDoubleArrayExtra("latitude2");
        int target_historylen = intent.getIntExtra("historylen", 3);
        Double target_lanesizein = intent.getDoubleExtra("lanesizein", 2);
        Double target_lanesizeout = intent.getDoubleExtra("lanesizeout", 4);
        String target_serverIP = intent.getStringExtra("serverIP");
        int target_serverPort = intent.getIntExtra("serverPort",8011);
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        GPSshow = (TextView) findViewById(R.id.GPSshow);
        historylen = (EditText) findViewById(R.id.historylen);
        lanesizein = (EditText) findViewById(R.id.lanesizein);
        lanesizeout = (EditText) findViewById(R.id.lanesizeout);
        ServerIP = (EditText) findViewById(R.id.ServerIP);
        ServerPort = (EditText) findViewById(R.id.ServerPort);
        buttonAddPad = (Button)findViewById(R.id.buttonAddPad);
        buttonRemovePad = (Button)findViewById(R.id.buttonRemovePad);
        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonCancel = (Button) findViewById(R.id.cancelButton);
        paramlist=(ListView)findViewById(R.id.paramlist);

        historylen.setText(Integer.toString(target_historylen));
        lanesizein.setText(Double.toString(target_lanesizein));
        lanesizeout.setText(Double.toString(target_lanesizeout));
        ServerIP.setText(target_serverIP);
        ServerPort.setText(Integer.toString(target_serverPort));

        adapter = new ListAdapter3();
        adapter.setItem(target_long,target_lat,target_long2,target_lat2,padnum);
        paramlist.setAdapter(adapter);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SetParameter.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
        else {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1,
                    0,
                    gpsLocationListener);
        }


        buttonAddPad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SetParameter.this,(padnum+1)+ "번이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                adapter.addItem(defaultlongi,defaultlati,defaultlongi,defaultlati,padnum);
                adapter.notifyDataSetChanged();
                if (padnum < 4) {
                    padnum ++;
                }
            }
        });

        buttonRemovePad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SetParameter.this, (padnum)+"번이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                adapter.delItem();
                adapter.notifyDataSetChanged();
                if (padnum > 1) {
                    padnum --;
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                set_historylen = Integer.parseInt(historylen.getText().toString());
                set_lanesizein = Double.parseDouble(lanesizein.getText().toString());
                set_lanesizeout = Double.parseDouble(lanesizeout.getText().toString());
                set_serverIP = ServerIP.getText().toString();
                set_serverPort = Integer.parseInt(ServerPort.getText().toString());
                set_long = adapter.getLongi();
                set_lat = adapter.getLati();
                set_long2 = adapter.getLongi2();
                set_lat2 = adapter.getLati2();

                Intent intent = new Intent();
                intent.putExtra("padnum", padnum);
                intent.putExtra("latitude", set_lat);
                intent.putExtra("longitude", set_long);
                intent.putExtra("latitude2", set_lat2);
                intent.putExtra("longitude2", set_long2);
                intent.putExtra("historylen", set_historylen);
                intent.putExtra("lanesizein", set_lanesizein);
                intent.putExtra("lanesizeout", set_lanesizeout);
                intent.putExtra("serverIP", set_serverIP);
                intent.putExtra("serverPort", set_serverPort);
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Setting Canceled", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        paramlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // TODO : item click
            }
        }) ;
    }

    //어뎁터 시작
    public class ListAdapter3 extends BaseAdapter {

        @Override
        public int getCount() {
            return filteredItemList3.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredItemList3.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();
            final ViewHolder3 holder;

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                holder = new ViewHolder3();
                LayoutInflater inflater =
                        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.param_listview_list, parent, false);
                holder.PadView = (TextView)convertView.findViewById(R.id.PadView);
                holder.longitude = (EditText)convertView.findViewById(R.id.longitude);
                holder.latitude = (EditText)convertView.findViewById(R.id.latitude);
                holder.buttonGPS = (Button)convertView.findViewById(R.id.buttonGPS);
                holder.PadView2 = (TextView)convertView.findViewById(R.id.PadView2);
                holder.longitude2 = (EditText)convertView.findViewById(R.id.longitude2);
                holder.latitude2 = (EditText)convertView.findViewById(R.id.latitude2);
                holder.buttonGPS2 = (Button)convertView.findViewById(R.id.buttonGPS2);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder3)convertView.getTag();
            }

            holder.ref = position;

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            final TextView PadView = (TextView) convertView.findViewById(R.id.PadView);
            final EditText longitude = (EditText) convertView.findViewById(R.id.longitude);
            final EditText latitude = (EditText) convertView.findViewById(R.id.latitude);
            final Button buttonGPS = (Button) convertView.findViewById(R.id.buttonGPS);
            final TextView PadView2 = (TextView) convertView.findViewById(R.id.PadView2);
            final EditText longitude2 = (EditText) convertView.findViewById(R.id.longitude2);
            final EditText latitude2 = (EditText) convertView.findViewById(R.id.latitude2);
            final Button buttonGPS2 = (Button) convertView.findViewById(R.id.buttonGPS2);

            final ListItem3 listViewItem = filteredItemList3.get(position);

            buttonGPS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setGPS(position);
                    adapter.notifyDataSetChanged();
                }
            });

            buttonGPS2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setGPS2(position);
                    adapter.notifyDataSetChanged();
                }
            });

            PadView.setText("Start "+String.valueOf(position+1));
            PadView2.setText("End "+String.valueOf(position+1));
            holder.longitude.setText(Double.toString(listViewItem.getPadLongi()));
            holder.latitude.setText(Double.toString(listViewItem.getPadLati()));
            holder.longitude2.setText(Double.toString(listViewItem.getPadLongi2()));
            holder.latitude2.setText(Double.toString(listViewItem.getPadLati2()));

            holder.longitude.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    filteredItemList3.get(holder.ref).setPadLongi(s.toString());
                }
            });

            holder.latitude.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    filteredItemList3.get(holder.ref).setPadLati(s.toString());
                }
            });

            holder.longitude2.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    filteredItemList3.get(holder.ref).setPadLongi2(s.toString());
                }
            });

            holder.latitude2.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    filteredItemList3.get(holder.ref).setPadLati2(s.toString());
                }
            });

            return convertView;
        }

        public void addItem(double longi, double lati, double longi2, double lati2, int padnum) {
            if (listViewItemList3.size() > 3){
            } else {
                ListItem3 item = new ListItem3();
                item.setPadLongi(Double.toString(longi));
                item.setPadLati(Double.toString(lati));
                item.setPadLongi2(Double.toString(longi2));
                item.setPadLati2(Double.toString(lati2));
                item.setPadNum(padnum);

                listViewItemList3.add(item);
            }
        }

        public void setGPS(int padnum) {
            ListItem3 item = new ListItem3();
            item.setPadLongi(Double.toString(longi));
            item.setPadLati(Double.toString(lati));
            target_long[padnum] = longi;
            target_lat[padnum] = lati;
            item.setPadLongi2(Double.toString(target_long2[padnum]));
            item.setPadLati2(Double.toString(target_lat2[padnum]));

            listViewItemList3.set(padnum,item);
        }

        public void setGPS2(int padnum) {
            ListItem3 item = new ListItem3();
            item.setPadLongi(Double.toString(target_long[padnum]));
            item.setPadLati(Double.toString(target_lat[padnum]));
            item.setPadLongi2(Double.toString(longi));
            item.setPadLati2(Double.toString(lati));
            target_long2[padnum] = longi;
            target_lat2[padnum] = lati;

            listViewItemList3.set(padnum,item);
        }

        public void setItem(double[] longi, double[] lati, double[] longi2, double[] lati2, int padnum) {
            for (int i = 0; i < padnum; i++) {

                ListItem3 item = new ListItem3();
                item.setPadLongi(Double.toString(longi[i]));
                item.setPadLati(Double.toString(lati[i]));
                item.setPadLongi2(Double.toString(longi2[i]));
                item.setPadLati2(Double.toString(lati2[i]));

                listViewItemList3.add(item);
            }
        }

        public double[] getLongi() {
            double[] result = new double[4];
            for (int i = 0;i<listViewItemList3.size();i++) {
                result[i] = listViewItemList3.get(i).getPadLongi();
            }
            return result;
        }

        public double[] getLati() {
            double[] result = new double[4];
            for (int i = 0;i<listViewItemList3.size();i++) {
                result[i] = listViewItemList3.get(i).getPadLati();
            }
            return result;
        }

        public double[] getLongi2() {
            double[] result = new double[4];
            for (int i = 0;i<listViewItemList3.size();i++) {
                result[i] = listViewItemList3.get(i).getPadLongi2();
            }
            return result;
        }

        public double[] getLati2() {
            double[] result = new double[4];
            for (int i = 0;i<listViewItemList3.size();i++) {
                result[i] = listViewItemList3.get(i).getPadLati2();
            }
            return result;
        }

        public void delItem() {
            if (listViewItemList3.size() < 2) {
            } else {
                listViewItemList3.remove(listViewItemList3.size() - 1);
            }
        }
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            longi = location.getLongitude();
            lati = location.getLatitude();

            long now = System.currentTimeMillis(); // 현재시간 받아오기

            Date date = new Date(now); // Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
            String nowTime = sdf.format(date);

            String GPSdata = "Time : " + nowTime + "\n" +
                    "Longi : " + Double.toString(longi) + "\n" +
                    "Lati : " + Double.toString(lati);

            GPSshow.setText(GPSdata);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


}