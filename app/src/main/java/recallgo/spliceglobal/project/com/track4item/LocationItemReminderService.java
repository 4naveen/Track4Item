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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Personal on 11/10/2017.
 */

public class LocationItemReminderService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener{
    ArrayList<Item> itemArrayList,locationItemArrayList;
    int final_count,counter;
    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;
    public Location mCurrentLocation,mPreviousLocation;
    String next_url;
    SharedPreferences pref;
    int PRIVATE_MODE = 0;
    SharedPreferences.Editor editor;
    public static final String PREF_NAME = "RecallPref";
    public LocationItemReminderService() {
        super("");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        itemArrayList=new ArrayList<>();
        locationItemArrayList=new ArrayList<>();
        pref = this.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        mPreviousLocation=new Location("");
        editor = pref.edit();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getItems("http://ec2-35-154-135-19.ap-south-1.compute.amazonaws.com:8001/api/reminders");
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Today Reminder")
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
        notificationManager.notify(0, notificationBuilder.build());
    }

    public  void getItems(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println("resonse"+response);
                            JSONObject jsonObject=new JSONObject(response);
                            next_url=jsonObject.getString("next");
                            final_count=jsonObject.getInt("count");
                            System.out.println("next in location"+next_url);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
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
                                        // System.out.println("date"+dates[0].substring(0,10));
                                        itemArrayList.add(item);
                                    }
                                }
                            }
                            if (!next_url.equalsIgnoreCase("null")){
                                getItems(next_url);
                            }
                            else {
                                System.out.println("item array size"+itemArrayList.size());
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
    public void onConnected(@Nullable Bundle bundle) {
        UpdateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

        Double truncatedlatitude = BigDecimal.valueOf(location.getLatitude())
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
        Double truncatedlongitude = BigDecimal.valueOf(location.getLongitude())
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();

        Location locationA = new Location(LocationManager.GPS_PROVIDER);
        locationA.setLatitude(truncatedlatitude);
        locationA.setLongitude(truncatedlongitude);
        System.out.println("locationA:"+locationA+"mpreviouslocation"+mPreviousLocation);

        if (locationA!=mPreviousLocation)
        {
            System.out.println("location changed");
            mPreviousLocation=locationA;
                if (itemArrayList.size()!=0){
                    for (int i = 0; i < itemArrayList.size(); i++) {
                        Location locationB = new Location(LocationManager.GPS_PROVIDER);
                        locationB.setLatitude(Double.parseDouble(itemArrayList.get(i).getLati()));
                        locationB.setLongitude(Double.parseDouble(itemArrayList.get(i).getLongi()));
                        System.out.println("locationB"+locationB);
                        float distance = locationA.distanceTo(locationB);
                        if (distance<200.0);
                        {
                            locationItemArrayList.add(itemArrayList.get(i));
                            //locationManager.removeUpdates();
                           // Toast.makeText(getApplicationContext(),itemArrayList.get(i).getItem_name(), Toast.LENGTH_SHORT).show();
                            sendNotification(itemArrayList.get(i).getItem_name());
                        }
                    }
            }
        }
        System.out.println("mcurrentlocation"+mCurrentLocation+"mpreviouslocation:"+mPreviousLocation);
    }
    private void UpdateLocation() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,mLocationRequest, this);
    }
}
