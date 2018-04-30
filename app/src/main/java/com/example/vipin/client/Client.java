package com.example.vipin.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.os.StrictMode;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.String;

public class Client extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = Client.class.getSimpleName();

    public static final int SERVERPORT = 6060;

    public static final String SERVER_IP = "192.168.0.6";
    ClientThread clientThread;
    Thread thread;
    TextView messageTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageTv = (TextView) findViewById(R.id.messageTv);
    }

    public void updateMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageTv.append(message + "\n");
            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.connect_server) {
            messageTv.setText("");
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            return;
        }

        if (view.getId() == R.id.send_data) {
            //clientThread.sendMessage("Hello you too\n");
            new SendMessage().execute("SPACE");
        }
    }

    Socket socket;

    class ClientThread implements Runnable {

        private BufferedReader input;

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {

                    Log.i(TAG, "Waiting for message from server...");

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    Log.i(TAG, "Message received from the server : " + message);

                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        updateMessage(getTime() + " | Server : " + message);
                        break;
                    }

                    updateMessage(getTime() + " | Server : " + message);

                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        /*void sendMessage(String message) {
            try {
                if (null != socket) {
                    PrintWriter output = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    output.println(message);
                    Log.i(TAG, "Message to send : " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/
             /*finally {
                if (output != null) {
                    output.close();
                }
            }*/

        //}

    }

    private class SendMessage extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... params) {
            String message = params[0];
            try {
                if (null != socket) {
                    PrintWriter output = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    output.println(message);
                    Log.i(TAG, "Message to send : " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
        }
    }


    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            //clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
    }
}
