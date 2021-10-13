package com.example.iot_led;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SendMessage extends Thread {

    private DatagramSocket UDPSocket;
    private InetAddress IP;
    private int PORT;

    private List<String> pile = new ArrayList<>();

    private Semaphore sem = new Semaphore(0);


    public SendMessage(DatagramSocket socket, InetAddress address, int port){
        IP = address;
        PORT = port;
        UDPSocket = socket;
    }

    public void send(String data){
        pile.add(data);
        sem.release();
    }

    @Override
    public void run() {
        while(true){
            try {
                sem.acquire();

                byte[] data = pile.get(0).getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(data, data.length, IP, PORT);
                UDPSocket.send(packet);

                pile.remove(0);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
