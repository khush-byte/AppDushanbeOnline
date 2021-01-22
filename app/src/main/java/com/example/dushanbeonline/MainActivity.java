package com.example.dushanbeonline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class MainActivity extends AppCompatActivity {

    private boolean isEndNotified;
    private ProgressBar progressBar;
    private MapView mapView;
    private OfflineManager offlineManager;
    public double user_lat = 0.0;
    public double user_lng = 0.0;
    public boolean isUserMarkerSet = false;
    public boolean isSetButtonPressed = false;
    private Marker new_marker;
    private MainActivity activity;
    private ConstraintLayout info_bord;
    private Button set_event_btn, exit_btn;
    private int eventId;
    private String event_name, event_text, user_name, start_date, end_date;
    private long event_date;
    public MapboxMap mapboxMap;
    private IconFactory iconFactory;
    public ArrayList<MarkerData> massive;

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        info_bord = findViewById(R.id.info_bord);
        mapView = findViewById(R.id.mapView);
        set_event_btn = findViewById(R.id.set_event_btn);
        exit_btn = findViewById(R.id.exit_btn);
        activity = this;
        massive = new ArrayList<>();
        iconFactory = IconFactory.getInstance(MainActivity.this);

        info_bord.setVisibility(View.GONE);
        final Bundle extras = getIntent().getExtras();



        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                activity.mapboxMap = mapboxMap;
                mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/khush2000/ckjtl7mbf0g9u19qo3yq2vswp"), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        // Set up the OfflineManager
                        offlineManager = OfflineManager.getInstance(MainActivity.this);

                        // Create a bounding box for the offline region
                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(new LatLng(38.709031671526894, 68.89564445189016)) // Northeast
                                .include(new LatLng(38.45110563225788, 68.65007835293264)) // Southwest
                                .build();

                        activity.mapboxMap.setLatLngBoundsForCameraTarget(latLngBounds);

                        // Define the offline region
                        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                                style.getUri(),
                                latLngBounds,
                                11,
                                18,
                                MainActivity.this.getResources().getDisplayMetrics().density);

                        // Set the metadata
                        byte[] metadata;
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(JSON_FIELD_REGION_NAME, "Yosemite National Park");
                            String json = jsonObject.toString();
                            metadata = json.getBytes(JSON_CHARSET);
                        } catch (Exception exception) {
                            Log.e("Debug1", exception.getMessage());
                            metadata = null;
                        }

                        // Create the region asynchronously
                        if (metadata != null) {
                            offlineManager.createOfflineRegion(
                                    definition,
                                    metadata,
                                    new OfflineManager.CreateOfflineRegionCallback() {
                                        @Override
                                        public void onCreate(OfflineRegion offlineRegion) {
                                            offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                                            // Display the download progress bar
                                            progressBar = findViewById(R.id.progress_bar);
                                            startProgress();

                                            // Monitor the download progress using setObserver
                                            offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                                                @Override
                                                public void onStatusChanged(OfflineRegionStatus status) {

                                                    // Calculate the download percentage and update the progress bar
                                                    double percentage = status.getRequiredResourceCount() >= 0
                                                            ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                                            0.0;

                                                    if (status.isComplete()) {
                                                        // Download complete
                                                        endProgress(getString(R.string.simple_offline_end_progress_success));
                                                    } else if (status.isRequiredResourceCountPrecise()) {
                                                        // Switch to determinate state
                                                        setPercentage((int) Math.round(percentage));
                                                    }
                                                }

                                                @Override
                                                public void onError(OfflineRegionError error) {
                                                    // If an error occurs, print to logcat
                                                    Log.e("Debug2", error.getReason());
                                                    Log.e("Debug3", error.getMessage());
                                                }

                                                @Override
                                                public void mapboxTileCountLimitExceeded(long limit) {
                                                    // Notify if offline region exceeds maximum tile count
                                                    Log.e("Debug4", String.valueOf(limit));
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String error) {
                                            Log.e("Debug5", error);
                                        }
                                    });
                        }

                        //drawPolygon(mapboxMap);
                        setMarker(activity.mapboxMap);

                        Icon icon;
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                            icon = iconFactory.fromBitmap(getBitmapFromVectorDrawable(activity.getBaseContext(),R.drawable.ic_target_marker));
                        } else {
                            icon = iconFactory.fromResource(R.drawable.target_marker);
                        }

                        new_marker = activity.mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(0.0, 0.0))
                                .title("Выбранное вами место")
                                .setIcon(icon));

                            mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(@NonNull Marker marker) {
                                    Log.e("Debug", String.valueOf(marker.getId()));

                                    if(marker.getId()==0){
                                        mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
                                            @Nullable
                                            @Override
                                            public View getInfoWindow(@NonNull Marker marker) {
                                                return null;
                                            }
                                        });
                                    } else {
                                        mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
                                            @SuppressLint("SetTextI18n")
                                            @Nullable
                                            @Override
                                            public View getInfoWindow(@NonNull Marker marker) {

                                                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                                                ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.marker_view_layout, null, false);
                                                TextView title = layout.findViewById(R.id.marker_view_title);
                                                TextView text = layout.findViewById(R.id.marker_view_text);
                                                TextView date = layout.findViewById(R.id.marker_view_date);
                                                TextView name = layout.findViewById(R.id.marker_view_name);
                                                Button marker_btn = layout.findViewById(R.id.marker_btn);

                                                int marker_id = (int) marker.getId()-1;

                                                eventId = massive.get(marker_id).eventID;
                                                event_name = massive.get(marker_id).event_name;
                                                event_text = massive.get(marker_id).event_text;
                                                user_name = massive.get(marker_id).user_name;
                                                start_date = massive.get(marker_id).date_start;
                                                end_date = massive.get(marker_id).date_end;

                                                title.setText(event_name);
                                                text.setText(event_text);
                                                name.setText("Организатор: " + massive.get(marker_id).user_name);
                                                long event_date = Long.parseLong(massive.get(marker_id).date_end);

                                                @SuppressLint("SimpleDateFormat") String date_end = new SimpleDateFormat("dd.MM.yyyy").format(event_date);
                                                date_end = "Актуально до: "+ date_end;
                                                date.setText(date_end);

                                                marker_btn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
                                                        intent.putExtra("eventID", eventId);
                                                        intent.putExtra("event_name", event_name);
                                                        intent.putExtra("event_text", event_text);
                                                        intent.putExtra("start_date", start_date);
                                                        intent.putExtra("end_date", end_date);
                                                        intent.putExtra("user_name", user_name);
                                                        startActivity(intent);
                                                    }
                                                });
                                                return layout;
                                            }
                                        });
                                    }
                                    return false;
                                }
                            });

                            GetRequest request = new GetRequest();
                            request.activity = activity;
                            request.execute();
                    }
                });
            }
        });

        set_event_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info_bord.setVisibility(View.VISIBLE);
                isSetButtonPressed = true;

                if(isUserMarkerSet){
                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("data", 0).edit();
                    editor.putString("lat", String.valueOf(user_lat));
                    editor.putString("lng", String.valueOf(user_lng));
                    editor.apply();

                    startActivity(new Intent(getApplicationContext(), EventActivity.class));
                    activity.finish();
                }
            }
        });

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSetButtonPressed){
                    new_marker.setPosition(new LatLng(0,0));
                    isSetButtonPressed = false;
                    isUserMarkerSet = false;
                    info_bord.setVisibility(View.GONE);
                    set_event_btn.setText("Создать\nсобытие");
                    exit_btn.setText("Выход");
                    new_marker.hideInfoWindow();

                } else {
                    activity.finish();
                    //System.exit(0);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (offlineManager != null) {
            offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
                @Override
                public void onList(OfflineRegion[] offlineRegions) {
                    if (offlineRegions.length > 0) {
                        // delete the last item in the offlineRegions list which will be yosemite offline map
                        offlineRegions[(offlineRegions.length - 1)].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                            @Override
                            public void onDelete() {
                                /*Toast.makeText(
                                        MainActivity.this,
                                        getString(R.string.basic_offline_deleted_toast),
                                        Toast.LENGTH_LONG
                                ).show();*/
                            }

                            @Override
                            public void onError(String error) {
                                Log.e("Debug6", error);
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e("Debug7", error);
                }
            });
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    // Progress bar methods
    private void startProgress() {

        // Start and show the progress bar
        isEndNotified = false;
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        // Show a toast
        //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        activity.finish();
    }

    //Draw the polygon
    private void drawPolygon(MapboxMap mapboxMap) {
        List<LatLng> polygon = new ArrayList<>();
        polygon.add(new LatLng(38.54912373494045, 68.87827779912179));
        polygon.add(new LatLng(38.550720250620984, 68.85112705660936));
        polygon.add(new LatLng(38.5416196364115, 68.84847322463446));
        polygon.add(new LatLng(38.54050193772015, 68.83989930594632));
        polygon.add(new LatLng(38.53012247758839, 68.84010344686747));
        polygon.add(new LatLng(38.51670680338175, 68.78437297539458));
        polygon.add(new LatLng(38.51926236269746, 68.78376055263112));
        polygon.add(new LatLng(38.51830403858609, 68.7747783521007));
        polygon.add(new LatLng(38.500557880758016, 68.7780538077972));
        polygon.add(new LatLng(38.495607813334374, 68.77775561228341));
        polygon.add(new LatLng(38.49470792893792, 68.7721975034448));
        polygon.add(new LatLng(38.10811904393063, 68.85595503217355));
        polygon.add(new LatLng(38.43211005954965, 69.12353664924751));
        polygon.add(new LatLng(38.54912373494045, 68.87827779912179));
        mapboxMap.addPolygon(new PolygonOptions()
                .addAll(polygon)
                .fillColor(Color.parseColor("#B004242C")));
    }

    //Set marker by click
    private void setMarker(MapboxMap mapboxMap){
        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {
                if(isSetButtonPressed) {
                    user_lat = point.getLatitude();
                    user_lng = point.getLongitude();
                    new_marker.setPosition(new LatLng(user_lat, user_lng));
                    new_marker.setSnippet("Координаты\nширота: "+user_lat+"\nдолгота: "+user_lng);
                    isUserMarkerSet = true;
                    set_event_btn.setText("Продолжить");
                    exit_btn.setText("Отмена");
                }

                //Toast.makeText(MainActivity.this, String.format("User clicked at: \n%s\n%s", user_lat, user_lng), Toast.LENGTH_LONG).show();
                /*if(!isUserMarkerSet){
                    isUserMarkerSet = true;
                }*/
                return true;
            }
        });
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void SetMarkerGroup(MapboxMap mapboxMap, int eventId, double lat, double lng){
        Icon icon_event;
        switch (eventId) {
            case 0:
                icon_event = setMarkerIcon(R.drawable.ic_event_marker2, R.drawable.event_marker2);
                break;
            case 1:
                icon_event = setMarkerIcon(R.drawable.ic_event_marker3, R.drawable.event_marker3);
                break;
            case 2:
                icon_event = setMarkerIcon(R.drawable.ic_event_marker4, R.drawable.event_marker4);
                break;
            case 3:
                icon_event = setMarkerIcon(R.drawable.ic_event_marker5, R.drawable.event_marker5);
                break;
            case 4:
                icon_event = setMarkerIcon(R.drawable.ic_event_marker, R.drawable.event_marker);
                break;
            default:
                icon_event = setMarkerIcon(R.drawable.ic_event_marker, R.drawable.event_marker);
                break;
        }

        Marker event_marker = mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .setIcon(icon_event));
    }

    private Icon setMarkerIcon(int drawID_svg, int drawID_png){
        Icon icon_new;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            icon_new = iconFactory.fromBitmap(getBitmapFromVectorDrawable(activity.getBaseContext(), drawID_svg));
        } else {
            icon_new = iconFactory.fromResource(drawID_png);
        }
        return icon_new;
    }
}