package com.example.iot_led;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sm;

    private TextView ip;
    private TextView port;
    private Button start;
    private InetAddress address;
    private DatagramSocket UDPSocket;
    private int PORT;
    private Button selectJ1;
    private Button selectJ2;
    private TextView resultat;

    private SendMessage send_message;

    private float previous_x = 0;

    private int current_player = 0; // 1 = J1; 2 = J2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        resultat = findViewById(R.id.resultat);
        start = findViewById(R.id.start);

        selectJ1 = findViewById(R.id.selectJ1);
        selectJ2 = findViewById(R.id.selectJ2);

        selectJ1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectJ1.setBackgroundColor(Color.BLUE);
                selectJ2.setBackgroundColor(Color.BLACK);
                current_player = 1;
                startParty();
            }
        });

        selectJ2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectJ1.setBackgroundColor(Color.BLACK);
                selectJ2.setBackgroundColor(Color.RED);
                current_player = 2;
                startParty();
            }
        });

        PORT = Integer.parseInt(port.getText().toString());

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetParty();

                if(current_player != 0){
                    // Abandon

                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
        sm = null;
    }

    private void initSensor(){
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    private void initNetworking(){
        try { // Choix du port local laissé à la discrétion de la plateforme
            UDPSocket = new DatagramSocket();
            address = InetAddress.getByName(ip.getText().toString());

            send_message = new SendMessage(UDPSocket, address, PORT);

            send_message.start();

            Listen listener = new Listen() {
                @Override
                public void listen(String data) {
                    result(data);
                }
            };
            (new ReceiverTask(UDPSocket, listener)).execute();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void result(String result){
        // Affiche et reset le jeu en cas de win / loose
        if(result.equals("(1)")){
            resultat.setText("Victoire");
        } else if (result.equals("(0)")){
            resultat.setText("Perdu");
        } else {
            resultat.setText("Erreur");
        }

        resultat.setVisibility(View.VISIBLE);

        start.setText("Restart");
    }

    private void leaveParty(){

    }

    private void resetParty(){
        resultat.setVisibility(View.INVISIBLE);
        selectJ2.setBackgroundColor(Color.RED);
        selectJ1.setBackgroundColor(Color.BLUE);

        selectJ2.setClickable(true);
        selectJ1.setClickable(true);

        current_player = 0;

        start.setText("Start");
    }

    private void startParty(){
        start.setText("Abandon");

        selectJ2.setClickable(false);
        selectJ1.setClickable(false);
    }

    private void sendJForward(){
        if(current_player == 1) {
            send_message.send("(1)");
        } else {
            send_message.send("(2)");

        }
        Log.i("SEND", "Forward");
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNetworking();
        initSensor();
        resetParty();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Math.abs(sensorEvent.values[0] - previous_x) > 15 && current_player != 0){
            sendJForward();
            Log.i("SENSOR_PREV", String.valueOf(previous_x));
            Log.i("SENSOR_CUR", String.valueOf(sensorEvent.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

