package com.smu.tracing;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

// this is the main activity class
// it is responsible for processing and displaying the information on the first page (main menu) of the application
// this class is also supported by GetAllAppsTask class which helps to retrieve the applications information in the phone
// this class is also supported by CustomAdapter class which helps with the display of MainActivity class
public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private TextView headerText;
    private TextView footerText;
    private int traceLevel;
    private int traceCallee;
    private long fileSize;
    private String methodName;
    private String callDepth;
    private String baseAddr;
    private String offset;
    private String instrOffset;
    private String length;

    private PackageManager packageManager;
    private ArrayAdapter<ApplicationInfo> adapter;
    private ArrayList<ApplicationInfo> applicationInfos;
    private ProgressDialog progressDialog;

    public String lastTraceApp = "";

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1000;
    Date currentDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();
        applicationInfos = new ArrayList<>();

        // find View by id
        listView = (ListView) findViewById(R.id.list_view);
        // toolbar = (Toolbar) findViewById(R.id.toolbar);

        // setSupportActionBar(toolbar);

        // Retrieve trace level based on user selected checkbox created in TraceType
        // class
        traceLevel = getIntent().getIntExtra("traceLevel", 0);
        fileSize = getIntent().getLongExtra("fileSize", 0);
        methodName = getIntent().getStringExtra("methodName");
        traceCallee = getIntent().getIntExtra("traceCallee", 0);
        callDepth = getIntent().getStringExtra("callDepth");
        baseAddr = getIntent().getStringExtra("baseAddr");
        offset = getIntent().getStringExtra("offset");
        instrOffset = getIntent().getStringExtra("instrOffset");
        length = getIntent().getStringExtra("length");

        // show progress dialog
        progressDialog = ProgressDialog.show(this, "Loading All Apps", "Loading application info...");

        // set list view adapter
        LayoutInflater inflater = getLayoutInflater();

        View footer = inflater.inflate(R.layout.layout_lv_header, listView, false);
        footerText = (TextView) footer.findViewById(R.id.text_header);
        listView.addFooterView(footer, null, false);

        View header = inflater.inflate(R.layout.layout_lv_header, listView, false);
        headerText = (TextView) header.findViewById(R.id.text_header);
        listView.addHeaderView(header, null, false);

        // initializing and set adapter for listview
        adapter = new ApplicationAdapter(this, R.layout.item_listview, applicationInfos, traceLevel, fileSize,
                methodName, traceCallee, callDepth, baseAddr, offset, instrOffset, length);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                // Toast.makeText(MainActivity.this, "This method is run every 1 second",
                // Toast.LENGTH_SHORT).show();
                setFooterStatusInformation();
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // stop handler when activity not visible super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        // searchView.setQuery("Select your app to trace below", false);
        searchView.setOnQueryTextListener(onQueryTextListener()); // text changed listener
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    private SearchView.OnQueryTextListener onQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // filter adapter and update ListView
                adapter.getFilter().filter(s);

                return false;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        // invoke async task
        new GetAllAppsTask(this, applicationInfos, packageManager).execute();
    }

    private void setFooterStatusInformation() {
        String fileName = "trace/trace.cfg";
        File file = new File(getApplicationContext().getFilesDir(), fileName);
        Scanner sc = null;
        String uid = "";
        String packageName = "";
        String appName = "";
        try {
            sc = new Scanner(file);
            if (sc != null) {
                String temp = sc.nextLine();
                temp = sc.nextLine();
                uid = temp.substring(0, Math.min(temp.length(), 5));
                packageName = getApplicationContext().getPackageManager().getNameForUid(Integer.parseInt(uid));
                final PackageManager pm = getApplicationContext().getPackageManager();
                ApplicationInfo ai;
                try {
                    ai = pm.getApplicationInfo(packageName, 0);
                } catch (Exception e) {
                    ai = null;
                }
                appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "unknown");
            }
            sc.close();
        } catch (Exception e) {
        }

        File dir = new File(getApplicationContext().getFilesDir(), "trace");
        if (!dir.exists()) {
            dir.mkdir();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, 1, 1, 1, 1, 1);
        Date maxDate = calendar.getTime();
        String maxTraceFile = "";
        try {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().toLowerCase().contains(".mmaps")) {
                    File traceFile = new File(getApplicationContext().getFilesDir(), "trace/" + files[i].getName());
                    Date lastModified = new Date(traceFile.lastModified());

                    if (lastModified.after(maxDate)) {
                        maxDate = lastModified;
                        maxTraceFile = files[i].getName();
                    }
                }
            }
        } catch (Exception e) {
        }

        if (maxTraceFile != "") {
            try {
                String[] traceFileArray = maxTraceFile.split("\\.");
                if (traceFileArray[2].toLowerCase().contains("ph") && traceFileArray[2].toLowerCase().contains("_")) {
                    maxTraceFile = traceFileArray[1];
                } else {
                    maxTraceFile = traceFileArray[1] + "." + traceFileArray[2];
                    lastTraceApp = maxTraceFile;
                }
            } catch (Exception e) {

            }
        }

        String monitoring = "";
        monitoring = monitoring + "Currently selected app : " + appName;
        monitoring = monitoring + "\n";
        monitoring = monitoring + "Last traced app: " + maxTraceFile;
        monitoring = monitoring + "\n";
        monitoring = monitoring + "Last traced time: " + maxDate;
        monitoring = monitoring + "\n";
        currentDate = new Date(System.currentTimeMillis());
        monitoring = monitoring + "The above information was last updated at: " + currentDate;
        footerText.setText(monitoring);
    }

    public void callBackDataFromAsynctask(List<ApplicationInfo> list) {
        applicationInfos.clear();
        for (int i = 0; i < list.size(); i++) {
            applicationInfos.add(list.get(i));
        }
        headerText.setText("All Apps (" + applicationInfos.size() + ")");
        adapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }

    public void updateUILayout(String content) {
        headerText.setText(content);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }
}
