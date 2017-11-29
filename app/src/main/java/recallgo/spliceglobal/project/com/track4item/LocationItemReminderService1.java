package recallgo.spliceglobal.project.com.track4item;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocationItemReminderService1 extends IntentService {
    private Location mCurrentLocation,mPreviousLocation;
    private LocationCallback mLocationCallback;
    private Boolean mRequestingLocationUpdates=false;
    private String mLastUpdateTime;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private SettingsClient mSettingsClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private LocationSettingsRequest mLocationSettingsRequest;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    boolean itemNotFound=false;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    public static final String PREF_NAME = "RecallPref";
    int PRIVATE_MODE = 0;

    ArrayList<Item> itemArrayList,locationItemArrayList;
    private String next_url;
    public LocationItemReminderService1() {
        super("MyLocationService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        AppConstant.mPreviousLocation=new Location("");
        locationItemArrayList=new ArrayList<>();
        AppConstant.itemArrayList=new ArrayList<>();
        AppConstant.shownList=new ArrayList<>();
        pref = this.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        mLastUpdateTime = "";
        createLocationCallback();
        createLocationRequest();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        itemArrayList=new ArrayList<>();

        getItems(AppConstant.ITEM_LIST_URL);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                AppConstant.mCurrentLocation = locationResult.getLastLocation();
                Double truncatedlatitude = BigDecimal.valueOf(locationResult.getLastLocation().getLatitude())
                        .setScale(4, RoundingMode.HALF_UP)
                        .doubleValue();
                Double truncatedlongitude = BigDecimal.valueOf(locationResult.getLastLocation().getLongitude())
                        .setScale(4, RoundingMode.HALF_UP)
                        .doubleValue();
                AppConstant.mCurrentLocation.setLatitude(truncatedlatitude);
                AppConstant.mCurrentLocation.setLongitude(truncatedlongitude);
                System.out.println("mcurrentlocation:"+AppConstant.mCurrentLocation+"mpreviouslocation"+AppConstant.mPreviousLocation);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
               // System.out.println("latitude:"+mCurrentLocation.getLatitude()+"longitude:"+mCurrentLocation.getLongitude());
                if (AppConstant.mCurrentLocation!=AppConstant.mPreviousLocation)
                {mRequestingLocationUpdates=true;
                    System.out.println("location changed");
                    AppConstant.mPreviousLocation=AppConstant.mCurrentLocation;
                    System.out.println("stored size"+AppConstant.list_size+"item array size"+AppConstant.itemArrayList.size());
                    if (AppConstant.itemArrayList.size()!=0){
                        AppConstant.counter++;
                        System.out.println("counter"+AppConstant.counter);
                        if (AppConstant.counter==1){
                            AppConstant.list_size=AppConstant.itemArrayList.size();
                            System.out.println("stored size"+AppConstant.list_size+"item array size"+AppConstant.itemArrayList.size());
                            for (int i = 0; i < AppConstant.itemArrayList.size(); i++) {
                                Location locationB = new Location(LocationManager.GPS_PROVIDER);
                                locationB.setLatitude(Double.parseDouble(AppConstant.itemArrayList.get(i).getLati()));
                                locationB.setLongitude(Double.parseDouble(AppConstant.itemArrayList.get(i).getLongi()));
                                System.out.println("locationB"+locationB);
                                float distance = AppConstant.mCurrentLocation.distanceTo(locationB);
                                //System.out.println("distance"+distance);
                                if (distance<(float) 200.0)
                                {
                                    //locationItemArrayList.add(itemArrayList.get(i));
                                    //locationManager.removeUpdates();.
                                    // Toast.makeText(getApplicationContext(),itemArrayList.get(i).getItem_name(), Toast.LENGTH_SHORT).show();

                                    sendNotification(AppConstant.itemArrayList.get(i).getItem_name(),i);
                                    AppConstant.shownList.add(String.valueOf(AppConstant.itemArrayList.get(i).getId()));
                                    System.out.println("send notification");
                                }
                            }
                            System.out.println("shownlist size"+AppConstant.shownList.size());
                        }
                        if (AppConstant.counter>1){
                            for (int i = 0; i < AppConstant.itemArrayList.size(); i++) {
                                Location locationB = new Location(LocationManager.GPS_PROVIDER);
                                locationB.setLatitude(Double.parseDouble(AppConstant.itemArrayList.get(i).getLati()));
                                locationB.setLongitude(Double.parseDouble(AppConstant.itemArrayList.get(i).getLongi()));
                                System.out.println("locationB"+locationB);
                                float distance = AppConstant.mCurrentLocation.distanceTo(locationB);
                                //System.out.println("distance"+distance);
                                if (distance<(float) 200.0)
                                {
                                    //locationItemArrayList.add(itemArrayList.get(i));
                                    //locationManager.removeUpdates();.
                                    // Toast.makeText(getApplicationContext(),itemArrayList.get(i).getItem_name(), Toast.LENGTH_SHORT).show();

                                   /* sendNotification(AppConstant.itemArrayList.get(i).getItem_name(),i);
                                    AppConstant.shownList.add(AppConstant.itemArrayList.get(i).getDate_created());
                                    System.out.println("send notification");*/
                                    for (int j=0;j<AppConstant.shownList.size();j++){
                                        System.out.println("shownlist item id "+AppConstant.shownList.get(j)+"item id"+String.valueOf(AppConstant.itemArrayList.get(i).getId()));
                                        if (!AppConstant.shownList.get(j).equalsIgnoreCase(String.valueOf(AppConstant.itemArrayList.get(i).getId()))){
                                            itemNotFound=true;
                                        }
                                    }
                                    if (itemNotFound){

                                        AppConstant.shownList.add(String.valueOf(AppConstant.itemArrayList.get(i).getId()));
                                        AppConstant.counter=0;
                                       // sendNotification(AppConstant.itemArrayList.get(i).getItem_name(),i);
                                        System.out.println("send notification");
                                    }

                                }
                            }

                        }
                    }
                }
            }
        };
    }
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d("", "stopLocationUpdates: updates never requested, no-op.");
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
    private void sendNotification(String messageBody,int id) {
        Intent intent = new Intent(this, MainActivity.class);
       // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Reminder")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.title_img);
            notificationBuilder.setColor(getResources().getColor(R.color.bg_screen3));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.notification_icon);
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notificationBuilder.build());
    }
    public  void getItems(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            System.out.println("response"+response);
                            JSONObject jsonObject=new JSONObject(response);
                            next_url=jsonObject.getString("next");
                            int final_count=jsonObject.getInt("count");
                            System.out.println("next in location"+next_url);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                          /*  if (itemArrayList.size()!=0){
                                itemArrayList.clear();
                            }*/
                            if (jsonArray.length()!=0){
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Item item = new Item();
                                    if (!(object.getString("lat").equalsIgnoreCase("null")&&object.getString("long").equalsIgnoreCase("null")))
                                    {
                                        item.setLati(object.getString("lat"));
                                        item.setLongi(object.getString("long"));
                                        item.setItem_name(object.getString("name"));
                                        item.setDate_created(object.getString("date_created"));
                                        item.setId(object.getInt("id"));
                                        // System.out.println("date"+dates[0].substring(0,10));
                                        AppConstant.itemArrayList.add(item);

                                    }

                                }


                            }
                            if (!next_url.equalsIgnoreCase("null")){
                                getItems(next_url);
                            }
                            else {
                                System.out.println("item array size in call"+AppConstant.itemArrayList.size());

                                startLocationUpdates();
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Log.i("response--", String.valueOf(error));
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header=new HashMap<>();
                header.put("Content-Type", "application/json; charset=utf-8");
                header.put("Authorization","Token aa5c12b3ebac6d122304d9b6c0713ae39863d938");
                // header.put("Content-Type", "application/x-www-form-urlencoded");
                return header;
            }
        } ;
        MyVolleySingleton.getInstance(this).getRequestQueue().add(stringRequest);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy called");
        stopLocationUpdates();
    }
}
