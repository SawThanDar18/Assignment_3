package com.padcmyanmar.padc9.assignment_3;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Button btnStart, btnStop, btnReset, addEvent_btn, captureVideo_btn, viewContact_btn, search_btn;
    TextView name_tv, ph_tv;
    EditText search_et;
    VideoView videoView;
    Chronometer cmTimer;

    Uri videoUri;

    Boolean resume = false;
    long elapsedTime;

    private static final int VIDEO_CAPTURE = 101;
    private static final int VIEW_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cmTimer = findViewById(R.id.cmTimer);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnReset = findViewById(R.id.btnReset);

        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer arg0) {
                if (!resume) {
                    elapsedTime = SystemClock.elapsedRealtime();
                } else {
                    elapsedTime = elapsedTime + 1000;
                }
            }
        });

        addEvent_btn = findViewById(R.id.addEvent_btn);
        addEvent_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "hi", Toast.LENGTH_LONG).show();
                Calendar calendarEvent = Calendar.getInstance();
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", calendarEvent.getTimeInMillis());
                intent.putExtra("endTime", calendarEvent.getTimeInMillis() + 60 * 60 * 1000);
                intent.putExtra("title", "Sample Event");
                intent.putExtra("allDay", true);
                intent.putExtra("rule", "FREQ=YEARLY");
                startActivity(intent);
            }
        });

        captureVideo_btn = findViewById(R.id.captureVideo_btn);
        captureVideo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takeVideoIntent, VIDEO_CAPTURE);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No camera on device", Toast.LENGTH_LONG).show();
                }
            }
        });

        viewContact_btn = findViewById(R.id.viewContact_btn);
        viewContact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, VIEW_CONTACT);
            }
        });

        ph_tv = findViewById(R.id.ph_tv);
        ph_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ph_tv = findViewById(R.id.ph_tv);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + ph_tv.getText().toString()));
                startActivity(intent);
            }
        });

        search_et = findViewById(R.id.search_et);

        search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH );
                intent.putExtra(SearchManager.QUERY, search_et.getText().toString());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_CAPTURE) {

            if (resultCode == RESULT_OK) {
                videoUri = data.getData();
                playbackRecordedVideo();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Failed to record video", Toast.LENGTH_LONG).show();
            }
        }

        else if (requestCode == VIEW_CONTACT && resultCode == RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor cursor = managedQuery(contactData, null, null, null, null);
                    cursor.moveToFirst();

                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    name_tv = findViewById(R.id.name_tv);
                    name_tv.setText(name);

                    ph_tv = findViewById(R.id.ph_tv);
                    ph_tv.setText(number);
                }
    }

    //timer
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnStart:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);

                if (!resume) {
                    cmTimer.setBase(SystemClock.elapsedRealtime());
                    cmTimer.start();
                } else {
                    cmTimer.start();
                }
                break;

            case R.id.btnStop:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                cmTimer.stop();
                resume = true;
                btnStart.setText("RESUME");
                break;

            case R.id.btnReset:
                cmTimer.stop();
                cmTimer.setText("00:00");
                resume = false;
                btnStart.setEnabled(true);
                btnStart.setText("START");
                btnStop.setEnabled(false);
                break;
        }
    }

    public void playbackRecordedVideo() {
        videoView = findViewById(R.id.videoView);
        videoView.setVideoURI(videoUri);
        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.start();
    }
}