package com.example.newlife;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.newlife.App.CHANNEL_ID;

public class TimerService extends Service {

    Handler handler;
    CountDownTimer ct;
    public static final String ACTION_LOCATION_BROADCAST = TimerService.class.getName() + "LocationBroadcast";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Handler getHandler() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                if (msg.what == 1) {
                    String txt = (String) msg.obj;
                    if(txt.equals("disconnected")){
                        //ct.cancel();
                    }
                    if(txt.equals("O") || txt.equals("P")){
                        Log.d("Before open", TestClass.time+"");
                        TestClass.isOpen = true;
                    }else if(txt.indexOf("C") != -1 || txt.indexOf("R") != -1){
                        Log.d("Total2", TestClass.time+"");
                        TestClass.isOpen = false;
                        TimerService.this.startCountDownTimer(TestClass.time);
                    } else if(txt.equals("1")){
                        if (ct != null) {
                            ct.cancel();
                        }
                        startCountDownTimer(1*60*1000);
                        TestClass.totaltime = 60;
                    } else if(txt.equals("2")){
                        if (ct != null) {
                            ct.cancel();
                        }
                        startCountDownTimer(2*60*1000);
                        TestClass.totaltime = 2*60;
                    } else if(txt.equals("3")){
                        if (ct != null) {
                            ct.cancel();
                        }
                        startCountDownTimer(3*60*1000);
                        TestClass.totaltime = 3*60;
                    } else if(txt.equals("4")){
                        if (ct != null) {
                            ct.cancel();
                        }
                        startCountDownTimer(4*60*1000);
                        TestClass.totaltime = 4*60;
                    } else if(txt.equals("disconnected")){
                        stopForeground(true);
                        //Toast.makeText(getApplicationContext(), "Device disconnected", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        return handler;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("start")){
            Log.i("Tag", "Received Start Foreground Intent ");
            // your start service code
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        0, notificationIntent, 0);

                String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
                String channelName = "My Background Service";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                chan.setLightColor(Color.BLUE);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(chan);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                Notification notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("New life")
                        .setContentText("App is running in background")
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();

                startForeground(2, notification);
                //do heavy work on a background thread
                //stopSelf();
            } else {
                startForeground(1, new Notification());
            }
        } else if (intent.getAction().equals( "stop")) {
                Log.i("LOG_TAG", "Received Stop Foreground Intent");
                //your end servce code
                ct.cancel();
                sendBroadcastMessage("00:00", "0");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class MyBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private void addNotification() {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Newlife")
                .setContentTitle("Time is up")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);
        notificationManagerCompat.notify(5, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) TimerService.this.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void sendBroadcastMessage(String time, String progress) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra("time", time);
            Log.d("progress", TestClass.totaltime+ "");
            intent.putExtra("progress", (Float.parseFloat(progress)/TestClass.totaltime)*100);
            sendBroadcast(intent);
    }

    public void startCountDownTimer(long min) {
        TestClass.time = min;
        ct = new CountDownTimer(TestClass.time, 1000) {
            public void onTick(long millisUntilFinished) {
                //update total with the remaining time left
                Log.d("time", TestClass.time + "");

                if(!TestClass.isOpen) {
                    TestClass.time = millisUntilFinished;
                    sendBroadcastMessage("" + String.format(Locale.ENGLISH, "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))), ""+TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished));
                } else {
                    cancel();
                }
            }
            public void onFinish() {
                sendBroadcastMessage("00:00", "0");
                stopForeground(true);
                addNotification();
            }
        };
        ct.start();
    }

    static class Connected extends Thread{

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        Context context;
        private final OutputStream mmOutStream;
        public static final int RESPONSE_MESSAGE = 10;
        Handler uih;
        Handler handler;

        Connected(BluetoothSocket socket, Handler uih, Context context, Handler handler) {
            mmSocket = socket;
            InputStream tmpIn = null;
            this.context = context;
            this.handler = handler;
            OutputStream tmpOut = null;
            this.uih = uih;
            Log.i("[THREAD-CT]", "Creating thread");
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("[THREAD-CT]", "Error:" + e.getMessage());
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            try {
                mmOutStream.flush();
            } catch (IOException e) {
                return;
            }
            Log.i("[THREAD-CT]", "IO's obtained");
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d("TAG", "InputStream: " + incomingMessage);
                    Message m = new Message();
                    m.what = 1;
                    m.obj = incomingMessage;
                    handler.sendMessage(m);
                } catch (IOException e) {
                    Log.e("TAG", "write: Error reading Input Stream. " + e.getMessage());
                    Message m1 = new Message();
                    m1.what = 1;
                    m1.obj = "disconnected";
                    Message m2 = new Message();
                    m2.what = 2;
                    m2.obj = "disconnected";
                    handler.sendMessage(m1);
                    uih.sendMessage(m2);
                    break;
                }
            }
            Log.i("[THREAD-CT]", "While loop ended");
        }

        public void write(byte[] bytes) {
            try {
                Log.i("[THREAD-CT]", "Writing bytes");
                mmOutStream.write(bytes);
                Log.d("bytes", bytes + "");

            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

        private void addNotification(String msg) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText("Newlife")
                    .setContentTitle(msg)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(alarmSound);
            notificationManagerCompat.notify(5, builder.build());
        }
    }
}