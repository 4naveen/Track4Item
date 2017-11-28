package recallgo.spliceglobal.project.com.track4item;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Personal on 11/28/2017.
 */

public class AppConstant {

    public static final String ITEM_LIST_URL = "http://ec2-35-154-135-19.ap-south-1.compute.amazonaws.com:8001/api/reminders/";
    public static int list_size=0;
    public static ArrayList<Item> itemArrayList;
    public static  Location mCurrentLocation,mPreviousLocation;

}
