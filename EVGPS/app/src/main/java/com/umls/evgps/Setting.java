package com.umls.evgps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Setting extends Activity {

    private EditText lat1;
    private EditText long1;
    private EditText phone1;
    private EditText lat2;
    private EditText long2;
    private EditText phone2;
    private EditText lat3;
    private EditText long3;
    private EditText phone3;
    private EditText serverIP;
    private EditText serverPort;
    private EditText range;
    private Button buttonAdd;
    private Button buttonDelete;
    private Button buttonSave;
    private Button cancelButton;
    private LinearLayout inv2;
    private LinearLayout inv3;
    public int numinv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_popup);

        lat1 = (EditText) findViewById(R.id.lat1);
        long1 = (EditText) findViewById(R.id.long1);
        phone1 = (EditText) findViewById(R.id.phone1);
        lat2 = (EditText) findViewById(R.id.lat2);
        long2 = (EditText) findViewById(R.id.long2);
        phone2 = (EditText) findViewById(R.id.phone2);
        lat3 = (EditText) findViewById(R.id.lat3);
        long3 = (EditText) findViewById(R.id.long3);
        phone3 = (EditText) findViewById(R.id.phone3);
        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        buttonSave = (Button) findViewById(R.id.buttonSave);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        serverIP = (EditText) findViewById(R.id.serverIP);
        serverPort = (EditText) findViewById(R.id.serverPort);
        range = (EditText) findViewById(R.id.range);
        inv2 = (LinearLayout) findViewById(R.id.inv2);
        inv3 = (LinearLayout) findViewById(R.id.inv3);

        inv2.setVisibility(View.GONE);
        inv3.setVisibility(View.GONE);
        buttonDelete.setEnabled(false);

        //데이터 가져오기
        Intent intent = getIntent();
        numinv = intent.getIntExtra("numinv",1);

        serverIP.setText(intent.getStringExtra("ip"));
        serverPort.setText(Integer.toString(intent.getIntExtra("port",8011)));
        range.setText(Double.toString(intent.getDoubleExtra("range",0)));

        lat1.setText(Double.toString(intent.getDoubleExtra("lat1",0)));
        long1.setText(Double.toString(intent.getDoubleExtra("long1",0)));
        phone1.setText(intent.getStringExtra("phone1"));

        if (numinv>=2){
            buttonDelete.setEnabled(true);
            inv2.setVisibility(View.VISIBLE);
            lat2.setText(Double.toString(intent.getDoubleExtra("lat2",0)));
            long2.setText(Double.toString(intent.getDoubleExtra("long2",0)));
            phone2.setText(intent.getStringExtra("phone2"));
        }

        if (numinv>=3){
            buttonAdd.setEnabled(false);
            inv3.setVisibility(View.VISIBLE);
            lat3.setText(Double.toString(intent.getDoubleExtra("lat3",0)));
            long3.setText(Double.toString(intent.getDoubleExtra("long3",0)));
            phone3.setText(intent.getStringExtra("phone3"));
        }

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (numinv==2) {
                    numinv=3;
                    inv3.setVisibility(View.VISIBLE);
                    buttonAdd.setEnabled(false);
                }

                if (numinv==1) {
                    numinv=2;
                    inv2.setVisibility(View.VISIBLE);
                    buttonDelete.setEnabled(true);
                }

            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (numinv==2) {
                    numinv=1;
                    inv2.setVisibility(View.GONE);
                    buttonDelete.setEnabled(false);
                }

                if (numinv==3) {
                    numinv=2;
                    inv3.setVisibility(View.GONE);
                    buttonAdd.setEnabled(true);

                }
            }
        });


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.putExtra("numinv", numinv);
                intent.putExtra("ip", serverIP.getText().toString());
                intent.putExtra("port",Integer.parseInt(serverPort.getText().toString()));
                intent.putExtra("range",Double.parseDouble(range.getText().toString()));

                intent.putExtra("lat1", Double.parseDouble(lat1.getText().toString()));
                intent.putExtra("long1", Double.parseDouble(long1.getText().toString()));
                intent.putExtra("phone1", phone1.getText().toString());

                if (numinv>=2){
                    intent.putExtra("lat2", Double.parseDouble(lat2.getText().toString()));
                    intent.putExtra("long2", Double.parseDouble(long2.getText().toString()));
                    intent.putExtra("phone2", phone2.getText().toString());
                }

                if (numinv>=3){
                    intent.putExtra("lat3", Double.parseDouble(lat3.getText().toString()));
                    intent.putExtra("long3", Double.parseDouble(long3.getText().toString()));
                    intent.putExtra("phone3", phone3.getText().toString());
                }

                setResult(RESULT_OK, intent);

                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Setting Canceled", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

    }



}
