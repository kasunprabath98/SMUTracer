package com.smu.tracing;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dalvik.system.DexFile;

import android.content.Context;

// this class is responsible for retrieving the names of the applications in the phone
// + the names, uid, etc... of the classes and libs in each of these applications
// and then pass this information to GetClassLib.java for display (in the form of checkboxes)
// note: that the application may hang, or the runtime can be quite long when there are many classes and libs to be extracted from the application folder
public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
    private List<ApplicationInfo> appsList;
    private List<ApplicationInfo> originalList;
    private MainActivity activity;
    private PackageManager packageManager;
    private AppsFilter filter;
    private CustomAdapter adapter;
    public int traceLevel;
    public int traceCallee;
    public long fileSize;
    public String methodName;
    public String callDepth;
    public String baseAddr;
    public String offset;
    public String instrOffset;
    public String length;

    public ApplicationAdapter(MainActivity activity, int textViewResourceId, List<ApplicationInfo> appsList, int level, long size, String methodName, int traceCallee,
                              String callDepth, String addr, String offset, String instrOffset, String length) {
        super(activity, textViewResourceId, appsList);
        this.activity = activity;
        this.appsList = appsList;
        this.originalList = appsList;
        this.traceLevel = level;
        this.fileSize = size;
        this.methodName = methodName;
        this.traceCallee = traceCallee;
        this.callDepth = callDepth;
        this.baseAddr = addr;
        this.offset = offset;
        this.instrOffset = instrOffset;
        this.length = length;
        packageManager = activity.getPackageManager();
    }

    @Override
    public int getCount() {
        return appsList.size();
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return appsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return appsList.indexOf(getItem(position));
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new AppsFilter();
        }
        return filter;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File traceConfig = new File(getContext().getFilesDir(), "trace/trace.cfg");
        String currentId = "";
        if (traceConfig != null) {
            Scanner sc = null;
            try {
                sc = new Scanner(traceConfig);
                if (sc != null) {
                    String temp = sc.nextLine();
                    temp = sc.nextLine();
                    currentId = temp.substring(0, Math.min(temp.length(), 5));
                }
                sc.close();
            } catch (Exception e) {
            }
        }

        String applicationId = "";
        try {
            applicationId = String.valueOf(packageManager.getApplicationInfo(getItem(position).packageName, PackageManager.GET_META_DATA).uid);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // If holder not exist then locate all view from UI file.
        if (convertView == null) {
            // inflate UI from XML file
            convertView = inflater.inflate(R.layout.item_listview, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder

            if (applicationId.equals(currentId)) {
                convertView.setBackgroundColor(Color.parseColor("#f2dededb"));
            }
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        String path = Environment.getDataDirectory().toString() + "/app";
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("getView", "files == null: " + (files == null));
        List<String> libsList = new ArrayList<String>();
        String apkPath = "";

        File file = new File(getContext().getFilesDir(), "lib." + getItem(position).packageName + ".txt");
        if (files == null) {
            Scanner sc = null;
            try {
                sc = new Scanner(file);
                if (sc != null) {
                    while (sc.hasNextLine()) {
                        String temp = sc.nextLine();
                        //Log.d("libs", temp);
                        libsList.add(temp);
                    }
                }
                sc.close();
            } catch (Exception e) {
            }
        } else {
            String pkgPath = "";
            String libPath = "";
           try{
               for (int i = 0; i < files.length; i++) {
                   if (Build.VERSION.RELEASE.equals("10")) {
                       pkgPath = files[i].getName();
                   } else if (Build.VERSION.RELEASE.equals("11") || Build.VERSION.RELEASE.equals("12") || Build.VERSION.RELEASE.equals("14")) {
                       File directory_inner = new File(path + "/" + files[i].getName());   // directory_inner = /data/app/<ID>
                       pkgPath = directory_inner.listFiles()[0].getName();
                       Log.d("DEBUG_APP: ", "pkgPath: "+pkgPath);
                   } else {
                       pkgPath = path;
                   }
                   if (pkgPath.toLowerCase().contains(getItem(position).packageName.toLowerCase())) {
                       if (Build.VERSION.RELEASE.equals("10")) {
                           apkPath = path + "/" + files[i].getName() + "/base.apk";
                           libPath = path + "/" + files[i].getName() + "/lib/arm64";
                       }
                       if (Build.VERSION.RELEASE.equals("11") || Build.VERSION.RELEASE.equals("12") || Build.VERSION.RELEASE.equals("14")) {
                           apkPath = path + "/" + files[i].getName() + "/" + pkgPath + "/base.apk";
                           libPath = path + "/" + files[i].getName() + "/" + pkgPath + "/lib/arm64";
                       }
                       //String libPath = path + "/" + files[i].getName() + "/lib/arm64";  //temporary commented until able to generalize across Android 10 and Android 11
                       directory = new File(libPath);
                       File[] libs = directory.listFiles();

                       String libPath2 = path + "/" + files[i].getName() + "/lib/arm";
                       directory = new File(libPath2);
                       File[] libs2 = directory.listFiles();
                       String sBody = "";
                       if (libs != null) {
                           try {
                               for (int j = 0; j < libs.length; j++) {
                                   //Log.d(getItem(position).packageName, libs[j].getName());
                                   libsList.add(libs[j].getName());
                                   sBody = sBody + libs[j].getName();
                                   sBody = sBody + "\n";
                               }
                               FileWriter writer = new FileWriter(file);
                               writer.append(sBody);
                               writer.flush();
                               writer.close();
                           } catch (NullPointerException | IOException n) {
                           }
                       } else {
                           Scanner sc = null;
                           try {
                               sc = new Scanner(file);
                               if (sc != null) {
                                   while (sc.hasNextLine()) {
                                       String temp = sc.nextLine();
                                       //Log.d("libs", temp);
                                       libsList.add(temp);
                                   }
                               }
                               sc.close();
                           } catch (Exception e) {
                           }
                       }

                       if (libs != null) {
                           try {
                               for (int j = 0; j < libs2.length; j++) {
                                   //Log.d(getItem(position).packageName, libs2[j].getName());
                                   libsList.add(libs2[j].getName());
                                   sBody = sBody + libs2[j].getName();
                                   sBody = sBody + "\n";
                               }
                               FileWriter writer = new FileWriter(file);
                               writer.append(sBody);
                               writer.flush();
                               writer.close();
                           } catch (NullPointerException | IOException n) {
                           }
                       } else {
                           Scanner sc = null;
                           try {
                               sc = new Scanner(file);
                               if (sc != null) {
                                   while (sc.hasNextLine()) {
                                       String temp = sc.nextLine();
                                       //Log.d("libs", temp);
                                       libsList.add(temp);
                                   }
                               }
                               sc.close();
                           } catch (Exception e) {
                           }
                       }
                   }
               }
           }catch (Exception e) {
               Log.d("Tracer", "getView: " + e.getMessage());
           }
        }

        //setting data to views
        holder.appName.setText(getItem(position).loadLabel(packageManager)); //get app name
        String[] packageName = getItem(position).packageName.split("\\."); //get app package
        String temp = "";
        for (String p : packageName) {
            temp = temp + "." + p;
            if (temp.length() >= 40) {
                break;
            }
        }
        temp = temp.substring(1);
        holder.appPackage.setText(temp); //get app package
        holder.appUid.setText(applicationId);
        holder.icon.setImageDrawable(getItem(position).loadIcon(packageManager)); //get app icon

        String level = Integer.toString(traceLevel);
        String size = Long.toString(fileSize);
        String callee = Integer.toString(traceCallee);
        String depth = this.callDepth;

        ArrayList<String> classesList = new ArrayList<String>();
        convertView.setOnClickListener(onClickListener(position, applicationId, libsList, classesList, level, size, callee, depth, apkPath,
                activity.lastTraceApp, "libs", "notSet", "notSet", "notSet",
                new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>()));
        return convertView;
    }

    private View.OnClickListener onClickListener(final int position, final String applicationId,
                                                 final List<String> libsList, final List<String> classesList, final String level, final String size, final String callee,
                                                 final String depth, final String apkPath, final String lastTraceApp, final String type,
                                                 final String libsStatus, final String classesStatus, final String registersStatus,
                                                 final ArrayList<String> libsAdapter, final ArrayList<String> classesAdapter, final ArrayList<String> filterAdapter,
                                                 final ArrayList<String> frameworkAdapter, final ArrayList<String> registersAdapter, final ArrayList<String> vregsAdapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationInfo app = appsList.get(position);
                try {
                    Intent intent = new Intent(v.getContext(), com.smu.tracing.GetClassLib.class);
                    if (null != intent) {
                        intent.putStringArrayListExtra("libsList", (ArrayList<String>) libsList);
                        //intent.putStringArrayListExtra("classesList", (ArrayList<String>) classesList);
                        intent.putExtra("traceLevel", level);
                        intent.putExtra("fileSize", size);
                        intent.putExtra("methodName", methodName);
                        intent.putExtra("traceCallee", callee);
                        intent.putExtra("callDepth", depth);
                        intent.putExtra("baseAddr", baseAddr);
                        intent.putExtra("offset", offset);
                        intent.putExtra("instrOffset", instrOffset);
                        intent.putExtra("length", length);
                        intent.putExtra("packageName", app.packageName);
                        intent.putExtra("applicationId", applicationId);
                        intent.putExtra("apkPath", apkPath);
                        intent.putExtra("lastTraceApp", lastTraceApp);
                        intent.putExtra("type", type);
                        intent.putExtra("libsStatus", libsStatus);
                        intent.putExtra("classesStatus", classesStatus);
                        intent.putExtra("registersStatus", registersStatus);
                        intent.putExtra("libsAdapter", (ArrayList<String>) libsAdapter);
                        intent.putExtra("classesAdapter", (ArrayList<String>) classesAdapter);
                        intent.putExtra("filterAdapter", (ArrayList<String>) filterAdapter);
                        intent.putExtra("frameworkAdapter", (ArrayList<String>) frameworkAdapter);
                        intent.putExtra("registersAdapter", (ArrayList<String>) registersAdapter);
                        intent.putExtra("vregsAdapter", (ArrayList<String>) vregsAdapter);
                        activity.startActivity(intent);
                    }
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private class ViewHolder {
        private ImageView icon;
        private TextView appName;
        private TextView appPackage;
        private TextView appUid;

        public ViewHolder(View v) {
            icon = (ImageView) v.findViewById(R.id.icon);
            appName = (TextView) v.findViewById(R.id.name);
            appPackage = (TextView) v.findViewById(R.id.app_package);
            appUid = (TextView) v.findViewById(R.id.app_uid);
        }
    }

    private class AppsFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<ApplicationInfo> filterList = new ArrayList<ApplicationInfo>();
                for (int i = 0; i < originalList.size(); i++) {
                    if ((originalList.get(i).loadLabel(packageManager).toString().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

                        ApplicationInfo applicationInfo = originalList.get(i);
                        filterList.add(applicationInfo);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;

            } else {
                results.count = originalList.size();
                results.values = originalList;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            appsList = (ArrayList<ApplicationInfo>) results.values;
            notifyDataSetChanged();

            if (appsList.size() == originalList.size()) {
                activity.updateUILayout("All apps (" + appsList.size() + ")");
            } else {
                activity.updateUILayout("Filtered apps (" + appsList.size() + ")");
            }
        }
    }
}
