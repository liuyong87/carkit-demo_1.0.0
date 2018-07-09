package com.feasycom.feasyblue.Controler;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;


public class ActivityCollector {
    public static List<Activity> activities=new ArrayList<>();
    public static void addActivity(Activity activity){
        activities.add(activity);
    }

    public static Boolean removeActivity(Activity  activity){
        return activities.remove(activity);
    }

    public static void finishAllActivity(){
        for(Activity activity:activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
        activities.clear();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    public static int getCount(){
        return activities.size();
    }
}
