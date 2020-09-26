package com.example.newlife;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Profile extends AppCompatActivity {

    String TAG = "GenerateQRCode";
    ImageView qrImage;
    String inputValue;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    TextView logout;
    TextView contactUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        logout = findViewById(R.id.profileLogout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefManager.getInstance(Profile.this).logout();
            }
        });

        contactUs = findViewById(R.id.profileSupport);

        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, ContactUs.class));
            }
        });

        qrImage = findViewById(R.id.qrimage);

        String json = "{\n" +
                "   \"Mac\":\""+SharedPrefManager.getInstance(Profile.this).getUser().getMacid()+"\",\n" +
                "   \"bid\":\""+SharedPrefManager.getInstance(Profile.this).getUser().getBid()+"\"\n" +
                "}";

        inputValue = json;

        if (inputValue.length() > 0) {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    inputValue, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.v(TAG, e.toString());
            }
        }

    }

    public void gotoHome(View view) {
        onBackPressed();
    }
}