package com.mokiductions.mando;

import android.os.StrictMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {

    private BufferedReader in;
    private PrintWriter out;

    private Mando mando;
    private String svIp;

    public Connection(Mando mando, String svIp) {
        this.mando = mando;
        this.svIp = svIp;
    }

    public void sendData(String data) {
        out.println(data);
    }

    @Override
    public void run() {
        String serverAddress = svIp;
        Socket socket = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        System.out.println("llega coco");
        try {
            socket = new Socket(serverAddress, 9090);
            System.out.println("llega nop");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                if (line.startsWith("CONNECT")) {
                    System.out.println("llega");
                    out.println("MANDO_ANDROID");
                } else if (line.startsWith("CONN_OK")) {
                    // Conexión aceptada por el servidor
                    System.out.println("llega con cojones");
                    mando.setConnState("Conectado al servidor " + serverAddress);
                    mando.disableBtnConn();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
