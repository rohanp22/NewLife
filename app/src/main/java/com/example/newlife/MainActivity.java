package com.example.newlife;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.SyncStateContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.warkiz.tickseekbar.TickSeekBar;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView console;
    public String MODULE_MAC;
    public final static int REQUEST_ENABLE_BT = 1;
    CircularProgressBar circularProgressBar;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    TimerService.Connected btt2 = null;
    public Handler mHandler;
    TextView setTimerText;
    LinearLayout linearLayout2;
    Button reconnect;
    static TextView countdown;
    BluetoothAdapter bta;
    TextView stop, resettextview;
    private Button start;
    TickSeekBar seekBar;
    ToggleButton toggleButton;
    Handler handler;
    Dialog dialogTransparent;
    BroadcastReceiver breciever;
    TextView disconnected;
    TimerService mMyService;
    ServiceConnection conn;
    FrameLayout frameLayout;
    RelativeLayout mainlayout;
    int reset;

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoactionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        resettextview = findViewById(R.id.reset);
        mainlayout = findViewById(R.id.activity_main);
        frameLayout = findViewById(R.id.frame);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        countdown = findViewById(R.id.countDownTimer);
        console = findViewById(R.id.console);
        reconnect = findViewById(R.id.reconnect);
        setTimerText = findViewById(R.id.settimertext);
        linearLayout2 = findViewById(R.id.linearlayout2);
        relativeLayout = findViewById(R.id.relativeLayout);
        disconnected = findViewById(R.id.disconnected);

        seekBar = (TickSeekBar) findViewById(R.id.seekbar);
        seekBar.setProgress(0);
        seekBar.setMax(4);

        seekBar.setDecimalScale(4);

        toggleButton = findViewById(R.id.toggle);

        MODULE_MAC = SharedPrefManager.getInstance(MainActivity.this).getUser().getMacid();
        console.setMovementMethod(new ScrollingMovementMethod());
        showDialog();

        registerReceiver(breciever, new IntentFilter(TimerService.ACTION_LOCATION_BROADCAST));
        countdown.setText(String.format(Locale.ENGLISH, "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(TestClass.time), TimeUnit.MILLISECONDS.toSeconds(TestClass.time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(TestClass.time))));

        bta = BluetoothAdapter.getDefaultAdapter();

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMyService = ((TimerService.MyBinder) service).getService();
                handler = mMyService.getHandler();
                Log.d("handler", (handler == null) + "");

                if (!bta.isEnabled()) {
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
                } else {
                    console.append("\n> Turning on Bluetooth");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Trying to connect");
                            reconnect();
                        }
                    }, 100);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

        };

        bind();

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btt2.write("S".getBytes());
                stopService();
            }
        });

        resettextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Reset", "reset");
                String r = reset + "";
                btt2.write(r.getBytes());
                frameLayout.setVisibility(View.VISIBLE);
                countdown.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                setTimerText.setVisibility(View.INVISIBLE);
                circularProgressBar.setVisibility(View.VISIBLE);
                btt2.write((seekBar.getProgress() + "").getBytes());
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (mmSocket.isConnected() && btt2 != null && seekBar.getProgress() != 0) {
                    //hideViews();
                    startService();
                    reset = seekBar.getProgress();
                    frameLayout.setVisibility(View.VISIBLE);
                    countdown.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.INVISIBLE);
                    setTimerText.setVisibility(View.INVISIBLE);
                    circularProgressBar.setVisibility(View.VISIBLE);
                    btt2.write((seekBar.getProgress() + "").getBytes());
                    Log.d("time", seekBar.getProgress() + "");
                    //mMyService.startCountDownTimer(seekBar.getProgress() * 60 * 1000);
                    toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked && !countdown.getText().equals("00:00")) {
                                // The toggle is enabled
                                btt2.write("P".getBytes());
                            } else if(!isChecked && !countdown.getText().equals("00:00")){
                                // The toggle is disabled
                                btt2.write("R".getBytes());
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Device not connected", Toast.LENGTH_LONG);
                }
            }
        });

        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogTransparent.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initiateBluetoothProcess();
                        btt2 = new TimerService.Connected(mmSocket, mHandler, MainActivity.this, handler);
                        btt2.start();
                    }
                }, 100);

            }
        });

        breciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String time = intent.getStringExtra("time");
                countdown.setText(time);
                float progress = intent.getFloatExtra("progress", 0);
                circularProgressBar.setProgress(progress);
                if (time.equals("00:00")) {
                    displayTimer();
                    mainlayout.setBackgroundColor(getResources().getColor(R.color.White));
                    seekBar.setVisibility(View.VISIBLE);
                    setTimerText.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_nav, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_profile:
                        startActivity(new Intent(MainActivity.this, Profile.class));
                        return true;
                    case R.id.nav_contact:
                        Toast.makeText(getApplicationContext(), "Item 2 Selected", Toast.LENGTH_LONG).show();
                        return true;
                    case R.id.nav_terms:
                        Toast.makeText(getApplicationContext(), "Item 3 Selected", Toast.LENGTH_LONG).show();
                        return true;
                    case R.id.nav_item_logout:
                        SharedPrefManager.getInstance(MainActivity.this).logout();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, Profile.class));
                return true;
            case R.id.nav_contact:
                Toast.makeText(getApplicationContext(), "Item 2 Selected", Toast.LENGTH_LONG).show();
                return true;
            case R.id.nav_terms:
                Toast.makeText(getApplicationContext(), "Item 3 Selected", Toast.LENGTH_LONG).show();
                return true;
            case R.id.nav_item_logout:
                SharedPrefManager.getInstance(MainActivity.this).logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    boolean rec = false;

    void showDialog() {
        dialogTransparent = new Dialog(this, android.R.style.Theme_Black);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.progressbg, null);
        dialogTransparent.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogTransparent.getWindow().setBackgroundDrawableResource(R.color.transparent);
        dialogTransparent.setContentView(view);
        dialogTransparent.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            if (!BluetoothAdapter.checkBluetoothAddress(MODULE_MAC)) {
                Toast.makeText(MainActivity.this, "Invalid QR CODE", Toast.LENGTH_LONG).show();
            } else {
                initiateBluetoothProcess();
                btt2 = new TimerService.Connected(mmSocket, mHandler, MainActivity.this, handler);
                btt2.start();
            }
        }
    }

    void reconnect() {
        dialogTransparent.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initiateBluetoothProcess();
                btt2 = new TimerService.Connected(mmSocket, mHandler, MainActivity.this, handler);
                btt2.start();
            }
        }, 1000);
    }

    void displayTimer() {
        if(mmSocket.isConnected() && bta.isEnabled()) {
            relativeLayout.setVisibility(View.VISIBLE);
        } else {
            //disconnected.setVisibility(View.VISIBLE);
        }
    }

    RelativeLayout relativeLayout;

    public void initiateBluetoothProcess() {
        if (bta.isEnabled()) {
            console.append("\n> Bluetooth turned on");
            BluetoothSocket tmp = null;
            mmDevice = bta.getRemoteDevice(MODULE_MAC);
            try {
                console.append("\n> Trying to connect to Newlife");
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
                mmSocket = tmp;
                mmSocket.connect();
                console.append("\n> Connected to Newlife");
                //relativeLayout.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.VISIBLE);
                relativeLayout.setVisibility(View.VISIBLE);
                reconnect.setVisibility(View.GONE);
                Log.i("[BLUETOOTH]", "Connected to: " + mmDevice.getName());
                if (countdown.getText().equals("00:00")) {
                    displayTimer();
                }
                if (TestClass.time != 0) {
                    frameLayout.setVisibility(View.VISIBLE);
                    countdown.setVisibility(View.VISIBLE);
                    circularProgressBar.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.INVISIBLE);
                    setTimerText.setVisibility(View.INVISIBLE);
                } else {
                    relativeLayout.setVisibility(View.VISIBLE);
                }

                disconnected.setVisibility(View.GONE);
                dialogTransparent.dismiss();
            } catch (IOException e) {
                try {
                    if (!rec) {
                        reconnect();
                        rec = true;
                    } else {
                        mmSocket.close();
                        console.append("\n> Device disconnected");
                        disconnected.setVisibility(View.VISIBLE);
                        frameLayout.setVisibility(View.INVISIBLE);
                        linearLayout2.setVisibility(View.INVISIBLE);
                        relativeLayout.setVisibility(View.INVISIBLE);
                        dialogTransparent.dismiss();
                    }

                } catch (IOException c) {
                    return;
                }
            }
            Log.i("[BLUETOOTH]", "Creating handler");
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    //super.handleMessage(msg);
                    if (msg.what == 2) {
                        String txt = (String) msg.obj;
//                        if (txt.equals("OPEN")) {
//                            console.append("> Door open");
//                        } else if (txt.equals("CLOSED")) {
//                            console.append("> Door closed");
//                        }
                        if(txt.equals("disconnected") && !rec){
                            displayDisconnected();
                        }
                    }
                }
            };
            Log.i("[BLUETOOTH]", "Creating and running Thread");
        }
    }

    Intent serviceIntent;

    public void bind() {
        serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.setAction("start");
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);
    }

    public void startService() {
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    void displayDisconnected(){
        disconnected.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.INVISIBLE);
        relativeLayout.setVisibility(View.INVISIBLE);
        linearLayout2.setVisibility(View.INVISIBLE);
        reconnect.setVisibility(View.VISIBLE);
    }

    void hideViews() {
        relativeLayout.setVisibility(View.GONE);
    }

    public void stopService() {
        //Intent serviceIntent = new Intent(this, TimerService.class);
        Intent stopIntent = new Intent(MainActivity.this, TimerService.class);
        stopIntent.setAction("stop");
        startService(stopIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(breciever, new IntentFilter(TimerService.ACTION_LOCATION_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(breciever);
    }

    @Override
    protected void onDestroy() {
        if (!mmSocket.isConnected()) {
            stopService();
        }
        super.onDestroy();
        unbindService(conn);
    }
}