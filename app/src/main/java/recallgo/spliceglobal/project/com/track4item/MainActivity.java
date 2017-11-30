package recallgo.spliceglobal.project.com.track4item;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("REFRESH_THIS");
        PendingIntent pi = PendingIntent.getBroadcast(this,123456789,intent, 0);
        int type = AlarmManager.RTC_WAKEUP;
        long interval = 1000 * 50;
        System.out.println("current milli sec"+System.currentTimeMillis());
        am.setInexactRepeating(type, System.currentTimeMillis(),interval,pi);

    }
}
