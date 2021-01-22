package com.example.dushanbeonline;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EventActivity extends AppCompatActivity {

    private EventActivity activity;
    private Spinner spinner_event_type;
    public int eventId = 0;
    public EditText event_text_field, event_name_field, user_name_field;
    private Button create_event_btn;
    private ImageView btn_back;
    private TextView event_end_date;
    private int mYear, mMonth, mDay;
    Calendar start_date, end_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        activity = this;
        spinner_event_type = findViewById(R.id.spinner_event_type);
        event_text_field = findViewById(R.id.event_text_field);
        event_name_field = findViewById(R.id.event_name_field);
        create_event_btn = findViewById(R.id.create_event_btn);
        user_name_field = findViewById(R.id.user_name_field);
        btn_back = findViewById(R.id.btn_back_event);
        event_end_date = findViewById(R.id.event_end_date);
        start_date = Calendar.getInstance();
        end_date = Calendar.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_event_type.setAdapter(adapter);
        spinner_event_type.setSelection(4);

        spinner_event_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ((TextView) parentView.getChildAt(0)).setTextColor(Color.parseColor("#00695C"));
                ((TextView) parentView.getChildAt(0)).setTextSize(15);
                int position_code = spinner_event_type.getSelectedItemPosition();

                switch (position_code) {
                    case 0:
                        eventId = 0;
                        break;
                    case 1:
                        eventId = 1;
                        break;
                    case 2:
                        eventId = 2;
                        break;
                    case 3:
                        eventId = 3;
                        break;
                    case 4:
                        eventId = 4;
                        break;
                    default:
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                eventId = 4;
            }
        });

        create_event_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(event_text_field.getText().toString()) && !TextUtils.isEmpty(event_name_field.getText().toString()) && !TextUtils.isEmpty(user_name_field.getText().toString())){

                    InsertRequest request = new InsertRequest();
                    request.activity = activity;
                    request.execute();
                } else {

                    Toast toast = Toast.makeText(EventActivity.this,"Заполните все поля!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                activity.finish();
            }
        });

        //Date activity
        event_end_date.setPaintFlags(event_end_date.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        Calendar first_date = Calendar.getInstance();
        first_date.add(Calendar.DAY_OF_MONTH, 1);
        final long date_result = first_date.getTimeInMillis();
        start_date.add(Calendar.DAY_OF_MONTH, 0);
        end_date = first_date;

        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd.MM.yyyy").format(date_result);
        event_end_date.setText(date);

        event_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(EventActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                end_date.set(year, month, day);
                                @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd.MM.yyyy").format(end_date.getTime());
                                event_end_date.setText(date);

                                mYear = end_date.get(Calendar.YEAR);
                                mMonth = end_date.get(Calendar.MONTH);
                                mDay = end_date.get(Calendar.DAY_OF_MONTH);
                            }
                        }, mYear, mMonth, mDay);

                dpd.getDatePicker().setMinDate(date_result);
                Calendar a = Calendar.getInstance();
                a.add(Calendar.DAY_OF_MONTH,5);
                dpd.getDatePicker().setMaxDate(a.getTimeInMillis());
                dpd.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        activity.finish();
    }
}