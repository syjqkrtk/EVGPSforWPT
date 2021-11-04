// Setting.java
// Load and Save setting parameters

// Import necessary packages and libraries
package com.example.gpslog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Define Setting class
public class Setting extends AppCompatActivity {

    // Initialize global variables
    private EditText latitude;
    private EditText longitude;
    private EditText radius;
    private Button buttonSave;
    private Button buttonCancel;
    public double set_radius;
    public double set_long;
    public double set_lat;

    // Define onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Start event page
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_popup);

        // Get parameters form MainActivity class
        Intent intent = getIntent();
        Double target_lat = intent.getDoubleExtra("latitude",0);
        Double target_long = intent.getDoubleExtra("longitude",0);
        Double target_radius = intent.getDoubleExtra("radius",1);

        // Set display variables
        latitude = (EditText) findViewById(R.id.latitude);
        longitude = (EditText) findViewById(R.id.longitude);
        radius = (EditText) findViewById(R.id.radius);
        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        latitude.setText(Double.toString(target_lat));
        longitude.setText(Double.toString(target_long));
        radius.setText(Double.toString(target_radius));

        // Define SAVE button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Parse parameters String to double
                set_radius = Double.parseDouble(radius.getText().toString());
                set_long = Double.parseDouble(longitude.getText().toString());
                set_lat = Double.parseDouble(latitude.getText().toString());

                // Send parameters to MainActivity class
                Intent intent = new Intent();
                intent.putExtra("latitude", set_lat);
                intent.putExtra("longitude", set_long);
                intent.putExtra("radius", set_radius);
                setResult(RESULT_OK, intent);

                // Terminate Setting class
                finish();
            }
        });

        // Define CANCEL button
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Setting Canceled", Toast.LENGTH_SHORT).show();

                // Terminate Setting class
                finish();
            }
        });
    }
}
