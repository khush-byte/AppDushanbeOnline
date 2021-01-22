package com.example.dushanbeonline;

import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetRequest extends AsyncTask<Void, Void, Void> {

    public MainActivity activity;
    String response;
    int i;

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        HTTPHandler handler = new HTTPHandler();
        response = handler.makeServiceCall("http://192.168.56.1:5000/");
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if(response!=null){
            Log.e("Debug", response);
            try {
                JSONObject json = new JSONObject(response);
                JSONArray array = json.getJSONArray("recordset");

                    for (int index = 0; index < array.length(); index++) {

                        JSONObject file = array.getJSONObject(index);

                        MarkerData data = new MarkerData();
                        data.eventID = Integer.parseInt(file.getString("eventID"));
                        data.event_name = file.getString("event_name");
                        data.event_text = file.getString("event_text");
                        data.user_name = file.getString("user_name");
                        data.date_start = file.getString("date_start");
                        data.date_end = file.getString("date_end");
                        data.lat = file.getString("lat");
                        data.lng = file.getString("lng");
                        activity.massive.add(data);
                    }

                    Log.e("Debug2", String.valueOf(activity.massive.size()));
                    for (i = 0; i < activity.massive.size(); i++) {
                        double lat = Double.parseDouble(activity.massive.get(i).lat);
                        double lng = Double.parseDouble(activity.massive.get(i).lng);
                        int eventID = activity.massive.get(i).eventID;
                        activity.SetMarkerGroup(activity.mapboxMap, eventID, lat, lng);
                    }
                } catch (final JSONException e) {
                Log.e("Debug2", "Incorrect json file");
            }
        }else{
            Toast toast = Toast.makeText(activity.getBaseContext(),"Отсутствует связь с сервером!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}


