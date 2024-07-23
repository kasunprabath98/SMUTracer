package com.smu.tracing;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// supporting class for MainActivity Class
public class GetAllAppsTask extends AsyncTask<Void, Void, List<ApplicationInfo>> {

    private MainActivity activity;
    private List<ApplicationInfo> apps;
    private PackageManager packageManager;

    public GetAllAppsTask(MainActivity activity, List<ApplicationInfo> apps, PackageManager pm) {
        this.activity = activity;
        this.apps = apps;
        this.packageManager = pm;
    }

    @Override
    protected List<ApplicationInfo> doInBackground(Void... params) {
        apps = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
        List<ApplicationInfo> listInstalledApps = new ArrayList<ApplicationInfo>();

        for (ApplicationInfo app : apps) {
            // apps with launcher intent
            if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                // updated system apps
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                // system apps
                listInstalledApps.add(app);     // revised by joshua 21-10-2021 to include system apps
            } else {
                // user installed apps
                listInstalledApps.add(app);
            }
        }
        return listInstalledApps;
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<>();
        for (ApplicationInfo applicationInfo : list) {
            try {
                if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                    applist.add(applicationInfo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return applist;
    }

    @Override
    protected void onPostExecute(List<ApplicationInfo> list) {
        super.onPostExecute(list);
        activity.callBackDataFromAsynctask(list);
    }
}
