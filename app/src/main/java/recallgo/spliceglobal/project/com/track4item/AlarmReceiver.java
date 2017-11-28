package recallgo.spliceglobal.project.com.track4item;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

/**
 * Created by Personal on 11/7/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        System.out.println("Alarma Reciver Called");
        if (isMyServiceRunning(this.context, LocationItemReminderService.class)) {
            System.out.println("alredy running no need to start again");
            Intent background1 = new Intent(context, LocationItemReminderService.class);
           context.startService(background1);
          /*  Intent background2 = new Intent(context, LocationItemReminderService1.class);
            context.startService(background2);*/
        } else {
          Intent background1 = new Intent(context, LocationItemReminderService.class);
            context.startService(background1);
            /*Intent background2 = new Intent(context, LocationItemReminderService1.class);
            context.startService(background2);*/
        }
    }
    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (services != null) {
            for (int i = 0; i < services.size(); i++) {
                if ((serviceClass.getName()).equals(services.get(i).service.getClassName()) && services.get(i).pid != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
