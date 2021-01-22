package com.example.dushanbeonline;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InsertRequest extends AsyncTask<Void, Void, Void> {

    public EventActivity activity;
    String event_name, event_text, user_name, date_start, date_end, lat, lng;
    int eventID;
    String response, code;

    @Override
    protected void onPreExecute() {
        eventID = activity.eventId;
        event_name = activity.event_name_field.getText().toString();
        event_text = activity.event_text_field.getText().toString();
        user_name = activity.user_name_field.getText().toString();
        date_start = String.valueOf(activity.start_date.getTimeInMillis());
        date_end = String.valueOf(activity.end_date.getTimeInMillis());

        SharedPreferences prefs = activity.getSharedPreferences("data", 0);
        lat = prefs.getString("lat", "");
        lng = prefs.getString("lng", "");
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        HTTPHandler handler = new HTTPHandler();
        String my_query = "http://192.168.56.1:5000/insert?eventID="+eventID+"&event_name='"+event_name+"'&event_text='"+event_text+"'&user_name='"+user_name+"'&date_start='"+date_start+"'&date_end='"+date_end+"'&lat='"+lat+"'&lng='"+lng+"'";
        response = handler.makeServiceCall(my_query);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if(!response.equals("")){
            Log.e("Debug", response);
            try {
                JSONObject json = new JSONObject(response);
                code = json.getString("rowsAffected");
                Log.e("Debug2", code);

                if(code.equals("[1]")){
                    Toast toast = Toast.makeText(activity.getApplicationContext(),"Событие добавлено!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }else{
                    Toast toast = Toast.makeText(activity.getApplicationContext(),"Нет связи с сервером!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

            } catch (final JSONException e) {
                Log.e("Debug2", "Incorrect json file");
            }
        }
    }
}


