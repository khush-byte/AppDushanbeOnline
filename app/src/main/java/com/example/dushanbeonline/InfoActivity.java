package com.example.dushanbeonline;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InfoActivity extends AppCompatActivity {
    ImageView btn_back;
    String event_name, event_text, user_name;
    Integer eventId;
    TextView info_title, info_text, start_date_info, user_name_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        btn_back = findViewById(R.id.btn_back_info);
        info_text = findViewById(R.id.info_text);
        info_title = findViewById(R.id. info_title);
        start_date_info = findViewById(R.id.start_date_info);
        user_name_info = findViewById(R.id.user_name_info);

        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            eventId = extras.getInt("eventID");
            event_name = extras.getString("event_name");
            event_text = extras.getString("event_text");
            long date_start = extras.getLong("start_date");
            long end_start = extras.getLong("end_date");
            user_name = extras.getString("user_name");

            info_title.setText(event_name);
            info_text.setText(event_text);
            user_name_info.setText(user_name);

            String date_a = getDate(date_start, "dd/MM/yyyy");
            String date_b = getDate(end_start, "dd/MM/yyyy");
            String date_info = "Создано: "+date_a+"\nАктивно до: "+date_b;
            start_date_info.setText(date_info);
        }

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}