package com.smu.tracing;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import dalvik.system.DexFile;

import static java.lang.Integer.parseInt;

// Information like the names, uid, etc... of the classes and libs in an application is passed from ApplicationAdapter.java to GetClassLib.java
// The purpose of this class is to process this information, and display them in the form of checkboxes (users can interact with these boxes)
// Note: the logic of this class is that, information of the checkboxes of all 3 tabs (classes, libs and registers are all stored at the same time)
// in 3 different arrays.
// Note: each time we navigate from one tab (e.g: classes tab) to another tab (e.g: registers tab), existing information of all 3 tabs will be passed on

// Additionally, this class is also responsible for noting down the user selected classes, libs and registers in the trace.cfg file (config file)
public class GetClassLib extends AppCompatActivity {
    private List<UserModel> libs = new ArrayList<>();
    private ArrayList<String> libsList = new ArrayList<String>();
    private ArrayList<String> classesList = new ArrayList<String>();
    private ArrayList<String> filterList = new ArrayList<String>();
    private ArrayList<String> frameworkList = new ArrayList<String>();
    private CustomAdapter adapter;
    // private Toolbar toolbar;
    private TextView selectAllViewText;
    private TextView clearAllViewText;
    private TextView footerText;
    private TextView footerStopTraceText;
    private TextView headerText;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private String nav_list[];

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        // Make sure you close all streams.
        fin.close();
        return ret;
    }

    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String type, String traceLevel,
            String fileSize, String methodName, String traceCallee, String callDepth,
            String baseAddr, String offset, String instrOffset, String length, String applicationId,
            ArrayList<String> classes, ArrayList<String> libs, ArrayList<String> filter, ArrayList<String> framework,
            ArrayList<String> registers, ArrayList<String> vregisters) {
        Log.d("writeFileOnInternalStorage", "type: " + type);
        int triggerFlag;
        if (libs.size() > 0) {
            traceLevel = "4";
        } else {
            boolean branches = false;
            outerloop: for (String class1 : classes) {
                for (String class2 : classes) {
                    if (class1.toLowerCase().contains(class2.toLowerCase()) && !class1.equals(class2)) {
                        branches = true;
                        break outerloop;
                    }
                }
            }
            if (branches) {
                // traceLevel = "2";
            } else {
                // traceLevel = "2";
            }
        }

        if (type.equals("fieldread")) {
            Log.d("[GetClassLib][writeFileOnInternalStorage][fieldread]", "traceLevel: 10");
            traceLevel = "10";
        } else if (type.equals("fieldwrite")) {
            Log.d("[GetClassLib][writeFileOnInternalStorage][fieldwrite]", "traceLevel: 11");
            traceLevel = "11";
        }

        // Check if methodName was selected
        if (!methodName.isEmpty()) {
            triggerFlag = 1;
        } else {
            triggerFlag = 0;
        }

        // Set default value if callDepth is not set by user
        if (callDepth.isEmpty()) {
            callDepth = "0";
        }

        String sBody = "#This is a conf file\n";
        sBody = sBody + "";
        Log.d("writeFileOnInternalStorage", "traceLevel: " + traceLevel);
        sBody = sBody + applicationId + " " + traceLevel + " " + fileSize + " " + triggerFlag + " " + traceCallee + " "
                + callDepth;
        sBody = sBody + "\n";
        sBody = sBody + "*selected"; // + "com.lazada.live.anchor.ProductSelectorActivity";
        for (String class_ : classes) {
            class_ = class_.replace("\n", "");
            sBody = sBody + " " + class_;
        }
        sBody = sBody + "\n";
        sBody = sBody + "*filtered";
        for (String filter_ : filter) {
            filter_ = filter_.replace("\n", "");
            sBody = sBody + " " + filter_;
        }
        sBody = sBody + "\n";
        sBody = sBody + "*framework";
        for (String framework_ : framework) {
            framework_ = framework_.replace("\n", "");
            sBody = sBody + " " + framework_;
        }
        sBody = sBody + "\n";
        sBody = sBody + "*nativeLib";
        for (String lib : libs) {
            lib = lib.replace("\n", "");
            sBody = sBody + " " + lib;
        }
        sBody = sBody + "\n";
        sBody = sBody + "*nativeRegs";
        for (String reg : registers) {
            reg = reg.replace("\n", "");
            sBody = sBody + " " + reg;
        }
        sBody = sBody + "\n";
        sBody = sBody + "*vregs";
        for (String vreg : vregisters) {
            vreg = vreg.replace("\n", "");
            sBody = sBody + " " + vreg;
        }
        sBody = sBody + "\n";
        sBody = sBody + "*trigger" + " " + methodName;
        sBody = sBody + "\n";
        sBody = sBody + "*readMemory";
        if (!offset.isEmpty() && !instrOffset.isEmpty()) {
            sBody = sBody + " " + baseAddr + " " + offset + " " + instrOffset + " " + length;
        } else {
            sBody = sBody + " " + baseAddr + " " + 0 + " " + 0 + " " + 0;
        }
        Log.d("traceOutput", sBody);

        File dir = new File("/data/local/tmp/com.smu.tracing/files/trace");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File gpxfile = new File(dir, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Checks if stop_trace file exists
        try {
            File stopTraceFile = new File(dir, "stop_trace");
            if (stopTraceFile.exists()) {
                if (stopTraceFile.delete()) {
                    System.out.println("[Trace] Successfully removed stop_trace file");
                } else {
                    System.out.println("[Trace] Failed to remove stop_trace file");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopTracing(Context mcoContext, String packageName, String sFileName) {
        String strFileContents;
        // Create empty stop_trace file to be detected in AOSP via OS::FileExists
        File dir = new File(mcoContext.getFilesDir(), "trace");
        final File stopTraceFile = new File(dir, sFileName);
        try {
            if (dir.exists()) {
                Toast toast = Toast.makeText(mcoContext, "Stopping trace...", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                FileWriter writer = new FileWriter(stopTraceFile);
                writer.close();
            } else {
                Toast toast = Toast.makeText(mcoContext, "A trace must be running before it can be stopped",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Read contents from trace.cfg and modify the traceLevel
        try {
            strFileContents = getStringFromFile("/data/local/tmp/com.smu.tracing/files/trace/trace.cfg");
            String[] tokens_line = strFileContents.split("\n"); // store tokens split by line
            String[] tokens_space = tokens_line[1].split(" "); // store tokens split by whitespace

            // set trace level to 0
            tokens_space[0] = "0";
            String strReplace = "";
            for (int i = 0; i < tokens_space.length; i++) {
                strReplace += tokens_space[i] + " ";
            }
            tokens_line[1] = strReplace;

            // Overwrite trace.cfg with the new contents
            File cfgfile = new File(dir, "trace.cfg");
            FileWriter writer_cfg = new FileWriter(cfgfile);
            for (int i = 0; i < tokens_line.length; i++) {
                writer_cfg.append(tokens_line[i]).append("\n");
            }
            writer_cfg.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeFilePermissions(File file) {
        Set<PosixFilePermission> perms = null;
        try {
            perms = Files.readAttributes(Paths.get(file.toString()), PosixFileAttributes.class).permissions();
            System.out.format("Permissions before: %s%n", PosixFilePermissions.toString(perms));
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(Paths.get(file.toString()), perms);
            System.out.format("Permissions after:  %s%n", PosixFilePermissions.toString(perms));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getClasses(String packageName, ArrayList<String> classesList, String apkPath)
            throws IOException {
        DexFile dexFile = null;
        String fileName = packageName + ".txt";
        // File file = new File(getApplicationContext().getFilesDir(), fileName);
        File file = new File("/data/local/tmp/com.smu.tracing/files/trace", fileName);
        Scanner sc = null;
        String type = "package";

        // Executor service
        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(coreCount);

        try {
            if (file.exists()) {
                Log.d("className1", "apkPath: " + apkPath);
                sc = new Scanner(file);
                if (sc != null) {
                    while (sc.hasNextLine()) {
                        String temp = sc.nextLine();
                        // retrieve framework classes
                        if (temp.contains("_framework")) {
                            frameworkList.add(temp.split("_framework")[0]);
                        }
                        // retrieve package classes
                        if (!temp.contains("_framework")) {
                            // Log.d("class: ", temp);//
                            classesList.add(temp);
                        }
                    }
                }
                sc.close();
                Log.d("className", "1");

                return classesList;
            } else {
                Log.d("className2", "apkPath: " + apkPath);
                dexFile = DexFile.loadDex(apkPath, String.valueOf(
                        File.createTempFile("opt", "dex", new File(getApplicationContext().getCacheDir().getPath()))),
                        0);
                // Print all classes in the DexFile
                String sBody = "";
                Log.d("GETCLASSES", String.valueOf(dexFile.entries()));//
                for (Enumeration<String> classNames = dexFile.entries(); classNames.hasMoreElements();) {
                    String className = classNames.nextElement();
                    Log.d("getClasses", "className: " + className);
                    if (className.matches("(?s)\\bandroid\\b.*") ||
                            className.matches("(?s).*\\bandroidx\\b.*") ||
                            className.matches("(?s).*\\bdalvik\\b.*\\bannotation\\b.*") ||
                            className.matches("(?s).*\\bdalvik\\b.*\\bbytecode\\b.*") ||
                            className.matches("(?s).*\\bdalvik\\b.*\\bsystem\\b.*") ||
                            className.matches("(?s).*\\bjava\\b.*") ||
                            className.matches("(?s).*\\borg\\b.*\\bapache\\b.*\\bhttp\\b.*") ||
                            className.matches("(?s).*\\borg\\b.*\\bjson\\b.*") ||
                            className.matches("(?s).*\\borg\\b.*\\bw3c\\b.*") ||
                            className.matches("(?s).*\\borg\\b.*\\bxml\\b.*")) {
                        sBody = sBody + className + "_framework";
                        sBody = sBody + "\n";
                        frameworkList.add(className);
                    } else {
                        sBody = sBody + className;
                        sBody = sBody + "\n";
                        classesList.add(className);
                    }
                }
                Log.d("getClasses", "frameworkList.size(): " + frameworkList.size());
                Log.d("getClasses", "classesList.size(): " + classesList.size());

                try {
                    FileWriter writer = new FileWriter(file);
                    writer.append(sBody);
                    writer.flush();
                    writer.close();
                    changeFilePermissions(file);
                    return classesList;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classesList;
    }

    // Currently we use getClasses() to obtain the framework classes in parallel
    // with the app classes
    // This helps to save time and prevents us to loop through all classes twice
    // thus avoiding redundancy
    public ArrayList<String> getFramework(String packageName, ArrayList<String> frameworkList, String apkPath)
            throws IOException {
        DexFile dexFile = null;

        try {
            dexFile = DexFile.loadDex(apkPath, String.valueOf(
                    File.createTempFile("opt", "dex", new File(getApplicationContext().getCacheDir().getPath()))), 0);
            for (Enumeration<String> frameworkInterfaces = dexFile.entries(); frameworkInterfaces.hasMoreElements();) {
                String framework = frameworkInterfaces.nextElement();
                // Log.d("className", className);//
                if (framework.contains("android")) {
                    // Log.d("framework", framework);
                    frameworkList.add(framework);
                }
                if (framework.matches("(?s)\\bandroid\\b.*") ||
                        framework.matches("(?s).*\\bandroidx\\b.*") ||
                        framework.matches("(?s).*\\bdalvik\\b.*\\bannotation\\b.*") ||
                        framework.matches("(?s).*\\bdalvik\\b.*\\bbytecode\\b.*") ||
                        framework.matches("(?s).*\\bdalvik\\b.*\\bsystem\\b.*") ||
                        framework.matches("(?s).*\\bjava\\b.*") ||
                        framework.matches("(?s).*\\borg\\b.*\\bapache\\b.*\\bhttp\\b.*") ||
                        framework.matches("(?s).*\\borg\\b.*\\bjson\\b.*") ||
                        framework.matches("(?s).*\\borg\\b.*\\bw3c\\b.*") ||
                        framework.matches("(?s).*\\borg\\b.*\\bxml\\b.*")) {
                    // Log.d("framework", framework);
                    frameworkList.add(framework);
                }
            }
            return frameworkList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return frameworkList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_get_class_lib);

        final ListView listView = (ListView) findViewById(R.id.listview);

        // toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        // setSupportActionBar(toolbar);

        // final List<UserModel> libs = new ArrayList<>();//
        final List<UserModel> classes = new ArrayList<>();

        Intent intent = getIntent();
        final String traceLevel = intent.getStringExtra("traceLevel");
        final String fileSize = intent.getStringExtra("fileSize");
        final String methodName = intent.getStringExtra("methodName");
        final String traceCallee = intent.getStringExtra("traceCallee");
        final String callDepth = intent.getStringExtra("callDepth");
        final String baseAddr = intent.getStringExtra("baseAddr");
        final String offset = intent.getStringExtra("offset");
        final String instrOffset = intent.getStringExtra("instrOffset");
        final String length = intent.getStringExtra("length");
        final String packageName = intent.getStringExtra("packageName");
        final String applicationId = intent.getStringExtra("applicationId");
        final String apkPath = intent.getStringExtra("apkPath");
        final String lastTraceApp = intent.getStringExtra("lastTraceApp");
        final String type = intent.getStringExtra("type");

        libsList = intent.getStringArrayListExtra("libsList");
        // ArrayList<String> classesList =
        // intent.getStringArrayListExtra("classesList");

        String libsStatus = intent.getStringExtra("libsStatus");
        String classesStatus = intent.getStringExtra("classesStatus");
        String registersStatus = intent.getStringExtra("registersStatus");
        final ArrayList<String> libsAdapter = intent.getStringArrayListExtra("libsAdapter");
        final ArrayList<String> classesAdapter = intent.getStringArrayListExtra("classesAdapter");
        final ArrayList<String> filterAdapter = intent.getStringArrayListExtra("filterAdapter");
        final ArrayList<String> frameworkAdapter = intent.getStringArrayListExtra("frameworkAdapter");
        final ArrayList<String> registersAdapter = intent.getStringArrayListExtra("registersAdapter");
        final ArrayList<String> vregsAdapter = intent.getStringArrayListExtra("vregsAdapter");
        final ArrayList<String> fieldAdapter = intent.getStringArrayListExtra("fieldAdapter");

        // Sliding navigation bar i.e. libs, classes, filter, regs, vregs etc.
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        nav_list = getResources().getStringArray(R.array.nav_list);
        recyclerAdapter = new RecyclerAdapter(this, nav_list, traceLevel, fileSize, methodName, traceCallee, callDepth,
                baseAddr, offset, instrOffset, length, packageName, applicationId,
                libsList, apkPath, lastTraceApp, type, libsStatus, classesStatus, registersStatus,
                libsAdapter, classesAdapter, filterAdapter, frameworkAdapter, registersAdapter, vregsAdapter);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Obtain libraries if there are no lib files
        if (libsList.isEmpty()) {
            try {
                ArrayList<String> preLibsList = new ArrayList<String>();
                HashSet<String> hashSet = new HashSet<String>();
                preLibsList = retrieveLibs(apkPath);
                for (String lib : preLibsList) {
                    String tokens[] = lib.split("/");
                    libsList.add(tokens[tokens.length - 1]);
                }
                // Remove duplicates
                hashSet.addAll(libsList);
                libsList.clear();
                libsList.addAll(hashSet);

                preLibsList.clear();
                hashSet.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d("class", "99");
        try {
            Log.d("class", "999");
            classesList = getClasses(packageName, classesList, apkPath);
            ArrayList<String> classesList2 = new ArrayList<String>();
            for (String lib : classesList) {
                String[] libArray = lib.split("\\."); // get app package
                String temp1 = "";
                String temp2 = "";
                for (String l : libArray) {
                    temp1 = temp1 + "." + l;
                    if (temp1.length() >= 45) {
                        temp2 = temp2 + "\n";
                        temp1 = "." + l;
                    }
                    temp2 = temp2 + "." + l;
                }
                temp2 = temp2.substring(1);
                classesList2.add(temp2);
            }
            classesList = classesList2;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // populate filter list with app and framework classes
        filterList.addAll(classesList);
        filterList.addAll(frameworkList);

        final ArrayList<String> registersList = new ArrayList<String>();
        registersList.add("x0");
        registersList.add("x1");
        registersList.add("x2");
        registersList.add("x3");
        registersList.add("x4");
        registersList.add("x5");
        registersList.add("x6");
        registersList.add("x7");
        registersList.add("x8");
        registersList.add("x9");
        registersList.add("x10");
        registersList.add("x11");
        registersList.add("x12");
        registersList.add("x13");
        registersList.add("x14");
        registersList.add("x15");
        registersList.add("x16");
        registersList.add("x17");
        registersList.add("x18");
        registersList.add("x19");
        registersList.add("x20");
        registersList.add("x21");
        registersList.add("x22");
        registersList.add("x23");
        registersList.add("x24");
        registersList.add("x25");
        registersList.add("x26");
        registersList.add("x27");
        registersList.add("x28");
        registersList.add("x29");
        registersList.add("x30");

        final ArrayList<String> vregsList = new ArrayList<String>();
        vregsList.add("v0");
        vregsList.add("v1");
        vregsList.add("v2");
        vregsList.add("v3");
        vregsList.add("v4");
        vregsList.add("v5");
        vregsList.add("v6");
        vregsList.add("v7");
        vregsList.add("v8");
        vregsList.add("v9");
        vregsList.add("v10");
        vregsList.add("v11");
        vregsList.add("v12");
        vregsList.add("v13");
        vregsList.add("v14");
        vregsList.add("v15");
        vregsList.add("v16");
        vregsList.add("v17");
        vregsList.add("v18");
        vregsList.add("v19");

        ArrayList<String> fieldsList = new ArrayList<String>();
        // fieldsList.add("ALL_FIELDS");

        Log.d("type", type);
        Log.d("type", classesStatus);
        Log.d("type", libsStatus);
        if (type.equals("classes")) {
            for (String class_ : classesList) {
                classes.add(new UserModel(false, class_));
                libs.add(new UserModel(false, class_));
            }
            adapter = new CustomAdapter(this, libs);
        } else if (type.equals("libs")) {
            for (String lib : libsList) {
                libs.add(new UserModel(false, lib));
            }
            adapter = new CustomAdapter(this, libs);
        } else if (type.equals("filter")) {
            for (String filter : filterList) {
                // classes.add(new UserModel(false, class_));
                libs.add(new UserModel(false, filter));
            }
            adapter = new CustomAdapter(this, libs);
        } else if (type.equals("framework")) {
            for (String framework : frameworkList) {
                // classes.add(new UserModel(false, framework));
                libs.add(new UserModel(false, framework));
            }
            adapter = new CustomAdapter(this, libs);
        } else if (type.equals("registers")) {
            for (String register : registersList) {
                libs.add(new UserModel(false, register));
            }
            adapter = new CustomAdapter(this, libs);
        } else if (type.equals("vregs")) {
            for (String vreg : vregsList) {
                libs.add(new UserModel(false, vreg));
            }
            adapter = new CustomAdapter(this, libs);
        } else {
            // for(String field : fieldsList){
            // libs.add(new UserModel(false, field));
            // }
            adapter = new CustomAdapter(this, libs);
        }

        int libsSize = libsList.size();
        int classesSize = classesList.size();
        int filterSize = filterList.size();
        int frameworkSize = frameworkList.size();

        final ArrayList<String> otherTraceLibs1 = new ArrayList<String>();
        final ArrayList<String> otherTraceLibs2 = new ArrayList<String>();
        final ArrayList<String> otherTraceLibs3 = new ArrayList<String>();
        final ArrayList<String> otherTraceLibs4 = new ArrayList<String>();
        final ArrayList<String> otherTraceLibs5 = new ArrayList<String>();
        final ArrayList<String> otherTraceLibs6 = new ArrayList<String>();
        int restrictedIndex = 0;
        boolean mainActivityFound = false;
        if (type.equals("classes")) {
            for (String i : classesAdapter) {
                UserModel model = libs.get(parseInt(i));
                model.setSelected(true);
                libs.set(parseInt(i), model);
                adapter.updateRecords(libs);
            }

            for (int i = 0; i < libs.size(); i++) {
                UserModel model = libs.get(i);
                if (model.userName.toLowerCase().contains("mainactivity")) {
                    model.setSelected(true);
                    libs.set(i, model);
                    adapter.updateRecords(libs);

                    mainActivityFound = true;
                    restrictedIndex = i;
                    classesAdapter.add(Integer.toString(i));
                    break;
                }
            }
            // if main activity is not found in the list, choose the first class by default
            if (!mainActivityFound) {
                // At least one class must be present in order to access
                if (libs.size() >= 1) {
                    UserModel model = libs.get(0);
                    model.setSelected(true);
                    libs.set(0, model);
                    adapter.updateRecords(libs);

                    restrictedIndex = 0;
                    classesAdapter.add(Integer.toString(0));
                }
            }

            for (String i : libsAdapter) {
                otherTraceLibs2.add(libsList.get(parseInt(i)));
            }
            for (String i : filterAdapter) {
                otherTraceLibs3.add(filterList.get(parseInt(i)));
            }
            for (String i : frameworkAdapter) {
                otherTraceLibs4.add(frameworkList.get(parseInt(i)));
            }
            for (String i : registersAdapter) {
                otherTraceLibs5.add(registersList.get(parseInt(i)));
            }
            for (String i : vregsAdapter) {
                otherTraceLibs6.add(vregsList.get(parseInt(i)));
            }
        } else if (type.equals("libs")) {
            for (String i : libsAdapter) {
                UserModel model = libs.get(parseInt(i));
                model.setSelected(true);
                libs.set(parseInt(i), model);
                adapter.updateRecords(libs);
            }
            for (String i : classesAdapter) {
                otherTraceLibs1.add(classesList.get(parseInt(i)));
            }
            for (String i : filterAdapter) {
                otherTraceLibs3.add(filterList.get(parseInt(i)));
            }
            for (String i : frameworkAdapter) {
                otherTraceLibs4.add(frameworkList.get(parseInt(i)));
            }
            for (String i : registersAdapter) {
                otherTraceLibs5.add(registersList.get(parseInt(i)));
            }
            for (String i : vregsAdapter) {
                otherTraceLibs6.add(vregsList.get(parseInt(i)));
            }
        } else if (type.equals("filter")) {
            for (String i : filterAdapter) {
                UserModel model = libs.get(parseInt(i));
                model.setSelected(true);
                libs.set(parseInt(i), model);
                adapter.updateRecords(libs);
            }
            for (String i : classesAdapter) {
                otherTraceLibs1.add(classesList.get(parseInt(i)));
            }
            for (String i : libsAdapter) {
                otherTraceLibs2.add(libsList.get(parseInt(i)));
            }
            for (String i : frameworkAdapter) {
                otherTraceLibs4.add(frameworkList.get(parseInt(i)));
            }
            for (String i : registersAdapter) {
                otherTraceLibs5.add(registersList.get(parseInt(i)));
            }
            for (String i : vregsAdapter) {
                otherTraceLibs6.add(vregsList.get(parseInt(i)));
            }
        } else if (type.equals("framework")) {
            for (String i : frameworkAdapter) {
                UserModel model = libs.get(parseInt(i));
                model.setSelected(true);
                libs.set(parseInt(i), model);
                adapter.updateRecords(libs);
            }
            for (String i : classesAdapter) {
                otherTraceLibs1.add(classesList.get(parseInt(i)));
            }
            for (String i : libsAdapter) {
                otherTraceLibs2.add(libsList.get(parseInt(i)));
            }
            for (String i : filterAdapter) {
                otherTraceLibs3.add(filterList.get(parseInt(i)));
            }
            for (String i : vregsAdapter) {
                otherTraceLibs5.add(vregsList.get(parseInt(i)));
            }
            for (String i : vregsAdapter) {
                otherTraceLibs6.add(vregsList.get(parseInt(i)));
            }
        } else if (type.equals("registers")) {
            for (String i : registersAdapter) {
                UserModel model = libs.get(parseInt(i));
                model.setSelected(true);
                libs.set(parseInt(i), model);
                adapter.updateRecords(libs);
            }
            for (String i : classesAdapter) {
                otherTraceLibs1.add(classesList.get(parseInt(i)));
            }
            for (String i : libsAdapter) {
                otherTraceLibs2.add(libsList.get(parseInt(i)));
            }
            for (String i : filterAdapter) {
                otherTraceLibs3.add(filterList.get(parseInt(i)));
            }
            for (String i : frameworkAdapter) {
                otherTraceLibs4.add(frameworkList.get(parseInt(i)));
            }
            for (String i : vregsAdapter) {
                otherTraceLibs6.add(vregsList.get(parseInt(i)));
            }
        } else {
            for (String i : vregsAdapter) {
                UserModel model = libs.get(parseInt(i));
                model.setSelected(true);
                libs.set(parseInt(i), model);
                adapter.updateRecords(libs);
            }
            for (String i : classesAdapter) {
                otherTraceLibs1.add(classesList.get(parseInt(i)));
            }
            for (String i : libsAdapter) {
                otherTraceLibs2.add(libsList.get(parseInt(i)));
            }
            for (String i : filterAdapter) {
                otherTraceLibs3.add(filterList.get(parseInt(i)));
            }
            for (String i : frameworkAdapter) {
                otherTraceLibs4.add(frameworkList.get(parseInt(i)));
            }
            for (String i : registersAdapter) {
                otherTraceLibs5.add(registersList.get(parseInt(i)));
            }
        }

        listView.setAdapter(adapter);
        // listView.setSelection(adapter.getCount()-1); //to start at the bottom of the
        // page

        // View header = inflater.inflate(R.layout.layout_lv_header, listView, false);
        View header = inflater.inflate(R.layout.layout_lv_header, listView, false);
        headerText = (TextView) header.findViewById(R.id.text_header);
        if (type.equals("classes")) {
            if (classesSize == 0) {
                headerText.setText("Unable to find any method/class from the app's folder");
            } else {
                headerText.setText("Please select the methods/classes to trace");
            }
        } else if (type.equals("libs")) {
            if (libsSize == 0) {
                headerText.setText("Unable to find any library from the app's folder");
            } else {
                headerText.setText("Please select the libraries to trace");
            }
        } else if (type.equals("filter")) {
            if (filterSize == 0) {
                headerText.setText("Unable to find any method/class from the app's folder");
            } else {
                headerText.setText("Please select the methods/classes to filter");
            }
        } else if (type.equals("framework")) {
            if (frameworkSize == 0) {
                headerText.setText("Unable to find any framework API from the app's folder");
            } else {
                headerText.setText("Please select the methods/classes to filter");
            }
        } else if (type.equals("registers")) {
            headerText.setText("Please select the registers to trace");
        } else if (type.equals("vregs")) {
            headerText.setText("Please select the vregisters to trace");
        } else {
            headerText.setText("Please select start to begin field read/write tracing");
        }

        listView.addHeaderView(header, null, false);

        final View selectAllView = inflater.inflate(R.layout.layout_lv_header, listView, false);
        selectAllViewText = (TextView) selectAllView.findViewById(R.id.text_header);
        selectAllViewText.setText("Select All");
        listView.addFooterView(selectAllView, null, false);

        final View clearAllView = inflater.inflate(R.layout.layout_lv_header, listView, false);
        clearAllViewText = (TextView) clearAllView.findViewById(R.id.text_header);
        clearAllViewText.setText("Clear All");
        listView.addFooterView(clearAllView, null, false);

        // "clear all" button is greyed out by default at the beginning (since nothing
        // is selected yet)
        clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
        clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));

        final View footerView = inflater.inflate(R.layout.layout_lv_header, null, false);
        footerText = (TextView) footerView.findViewById(R.id.text_header);
        footerText.setText("Start Tracing");
        listView.addFooterView(footerView);

        final View footerStopTraceView = inflater.inflate(R.layout.layout_lv_header, null, false);
        footerStopTraceText = (TextView) footerStopTraceView.findViewById(R.id.text_header);
        footerStopTraceText.setText("Stop Tracing");
        listView.addFooterView(footerStopTraceView);

        final int finalRestrictedIndex = restrictedIndex;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int index;
                String selectedFromList = String.valueOf(adapterView.getItemAtPosition(i));
                i = i - 1;
                // UserModel model = libs.get(i);
                UserModel model = adapter.users.get(i);

                if (model.isSelected()) {
                    model.setSelected(false);
                    if (type.equals("classes")) {
                        index = classesList.indexOf(model.getUserName());
                        if (index != -1) {
                            classesAdapter.remove(Integer.toString(index));
                        }
                        // if this is a restricted checkbox, prevent user from unchecking
                        if (finalRestrictedIndex == index) {
                            model.setSelected(true);
                            classesAdapter.add(Integer.toString(i));
                            Toast toast = Toast.makeText(view.getContext(), "Default class cannot be unchecked",
                                    Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    } else if (type.equals("libs")) {
                        index = libsList.indexOf(model.getUserName());
                        if (index != -1) {
                            libsAdapter.remove(Integer.toString(index));
                        }
                    } else if (type.equals("filter")) {
                        index = filterList.indexOf(model.getUserName());
                        if (index != -1) {
                            filterAdapter.remove(Integer.toString(index));
                        }
                    } else if (type.equals("framework")) {
                        index = frameworkList.indexOf(model.getUserName());
                        if (index != -1) {
                            frameworkAdapter.remove(Integer.toString(index));
                        }
                    } else if (type.equals("registers")) {
                        registersAdapter.remove(Integer.toString(i));
                    } else {
                        index = vregsList.indexOf(model.getUserName());
                        if (index != -1) {
                            vregsAdapter.remove(Integer.toString(index));
                        }
                    }
                } else {
                    model.setSelected(true);
                    if (type.equals("classes")) {
                        index = classesList.indexOf(model.getUserName());
                        if (index != -1) {
                            classesAdapter.add(Integer.toString(index));
                        }
                    } else if (type.equals("libs")) {
                        index = libsList.indexOf(model.getUserName());
                        if (index != -1) {
                            libsAdapter.add(Integer.toString(index));
                        }
                    } else if (type.equals("filter")) {
                        index = filterList.indexOf(model.getUserName());
                        if (index != -1) {
                            filterAdapter.add(Integer.toString(index));
                        }
                    } else if (type.equals("framework")) {
                        index = frameworkList.indexOf(model.getUserName());
                        if (index != -1) {
                            frameworkAdapter.add(Integer.toString(index));
                        }
                    } else if (type.equals("registers")) {
                        index = registersList.indexOf(model.getUserName());
                        if (index != -1) {
                            registersAdapter.add(Integer.toString(index));
                        }
                    } else {
                        index = vregsList.indexOf(model.getUserName());
                        if (index != -1) {
                            vregsAdapter.add(Integer.toString(index));
                        }
                    }
                }

                // libs.set(i, model);//
                adapter.users.set(i, model);

                // update the colour of "select all" and "clear all" button
                if (type.equals("classes")) {
                    if (classesAdapter.size() < libs.size()) {
                        clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        selectAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else if (classesAdapter.size() == libs.size()) {
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllViewText.setTextColor(Color.parseColor("#000000"));
                        selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                    } else {
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                    }
                } else if (type.equals("libs")) {
                    if (libsAdapter.size() < libs.size()) {
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        selectAllViewText.setTextColor(Color.parseColor("#000000"));
                        clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                    } else if (libsAdapter.size() == libs.size()) {
                        selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else {
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                    }
                } else if (type.equals("filter")) {
                    if (filterAdapter.size() < libs.size()) {
                        clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        selectAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else if (filterAdapter.size() == libs.size()) {
                        selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else {
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                    }
                } else if (type.equals("framework")) {
                    if (frameworkAdapter.size() < libs.size()) {
                        clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        selectAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else if (frameworkAdapter.size() == libs.size()) {
                        selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else {
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                    }
                } else if (type.equals("registers")) {
                    if (registersAdapter.size() < libs.size()) {
                        clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        selectAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else if (registersAdapter.size() == libs.size()) {
                        selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else {
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                    }
                } else {
                    if (vregsAdapter.size() < libs.size()) {
                        clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        selectAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else if (vregsAdapter.size() == libs.size()) {
                        selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                        selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllViewText.setTextColor(Color.parseColor("#000000"));
                    } else {
                        selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                        clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                    }
                }

                // now update adapter
                adapter.updateRecords(adapter.users);
            }
        });

        // scroll to bottom
        ImageButton scrollToBottom = (ImageButton) findViewById(R.id.buttonScrollBottom);
        scrollToBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setSelection(listView.getCount() - 1);
            }
        });

        selectAllView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < libs.size(); i++) {
                    UserModel model = libs.get(i);
                    if (!model.isSelected()) {
                        model.setSelected(true);
                        if (type.equals("classes")) {
                            classesAdapter.add(Integer.toString(i));
                        } else if (type.equals("libs")) {
                            libsAdapter.add(Integer.toString(i));
                        } else if (type.equals("filter")) {
                            filterAdapter.add(Integer.toString(i));
                        } else if (type.equals("framework")) {
                            frameworkAdapter.add(Integer.toString(i));
                        } else if (type.equals("registers")) {
                            registersAdapter.add(Integer.toString(i));
                        } else {
                            vregsAdapter.add(Integer.toString(i));
                        }
                    }
                    libs.set(i, model);

                    // update the "select all" button colour
                    if (type.equals("classes")) {
                        if (classesAdapter.size() == libs.size()) {
                            selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            clearAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else if (type.equals("libs")) {
                        if (libsAdapter.size() == libs.size()) {
                            selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            clearAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else if (type.equals("filter")) {
                        if (filterAdapter.size() == libs.size()) {
                            selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            clearAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else if (type.equals("framework")) {
                        if (frameworkAdapter.size() == libs.size()) {
                            selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            clearAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else if (type.equals("registers")) {
                        if (registersAdapter.size() == libs.size()) {
                            selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            clearAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else {
                        if (vregsAdapter.size() == libs.size()) {
                            selectAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            selectAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            clearAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            clearAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    }

                    // now update adapter
                    adapter.updateRecords(libs);
                }
            }
        });

        clearAllView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < libs.size(); i++) {
                    UserModel model = libs.get(i);
                    if (model.isSelected()) {
                        model.setSelected(false);
                        if (type.equals("classes")) {
                            classesAdapter.remove(Integer.toString(i));

                            // if this is a restricted checkbox, prevent user from unchecking
                            if (finalRestrictedIndex == i) {
                                model.setSelected(true);
                                classesAdapter.add(Integer.toString(i));
                                Toast toast = Toast.makeText(v.getContext(), "Default class cannot be unchecked",
                                        Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            }
                        } else if (type.equals("libs")) {
                            libsAdapter.remove(Integer.toString(i));
                        } else if (type.equals("filter")) {
                            filterAdapter.remove(Integer.toString(i));
                        } else if (type.equals("framework")) {
                            frameworkAdapter.remove(Integer.toString(i));
                        } else if (type.equals("registers")) {
                            registersAdapter.remove(Integer.toString(i));
                        } else {
                            vregsAdapter.remove(Integer.toString(i));
                        }
                    }
                    libs.set(i, model);

                    // update the "clear all" button colour
                    if (type.equals("classes")) {
                        if (classesAdapter.size() == 1) {
                            clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            selectAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else if (type.equals("libs")) {
                        if (libsAdapter.size() == 0) {
                            clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            selectAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else if (type.equals("filter")) {
                        if (filterAdapter.size() == 0) {
                            clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            selectAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else if (type.equals("framework")) {
                        if (frameworkAdapter.size() == 0) {
                            clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            selectAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else if (type.equals("registers")) {
                        if (registersAdapter.size() == 0) {
                            clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            selectAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    } else {
                        if (vregsAdapter.size() == 0) {
                            clearAllView.setBackgroundColor(Color.parseColor("#f2dededb"));
                            clearAllViewText.setTextColor(Color.parseColor("#FF9E9E9E"));
                            selectAllView.setBackgroundColor(Color.parseColor("#FFFF00"));
                            selectAllViewText.setTextColor(Color.parseColor("#000000"));
                        }
                    }

                    // now update adapter
                    adapter.updateRecords(libs);
                }
            }
        });

        final ArrayList<String> traceLibs = new ArrayList<String>();
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // do something
                traceLibs.clear();
                List<UserModel> selectedLibs = adapter.getRecords();
                for (UserModel selectedLib : selectedLibs) {
                    if (selectedLib.isSelected == true) {
                        traceLibs.add(selectedLib.userName);
                    }
                }

                int level = 0;
                if (type.equals("classes")) {
                    writeFileOnInternalStorage(v.getContext(), "trace.cfg", type, traceLevel, fileSize, methodName,
                            traceCallee, callDepth, baseAddr, offset, instrOffset, length, applicationId, traceLibs,
                            otherTraceLibs2, otherTraceLibs3, otherTraceLibs4, otherTraceLibs5, otherTraceLibs6);
                } else if (type.equals("libs")) {
                    writeFileOnInternalStorage(v.getContext(), "trace.cfg", type, traceLevel, fileSize, methodName,
                            traceCallee, callDepth, baseAddr, offset, instrOffset, length, applicationId,
                            otherTraceLibs1, traceLibs, otherTraceLibs3, otherTraceLibs4, otherTraceLibs5,
                            otherTraceLibs6);
                } else if (type.equals("filter")) {
                    writeFileOnInternalStorage(v.getContext(), "trace.cfg", type, traceLevel, fileSize, methodName,
                            traceCallee, callDepth, baseAddr, offset, instrOffset, length, applicationId,
                            otherTraceLibs1, otherTraceLibs2, traceLibs, otherTraceLibs4, otherTraceLibs5,
                            otherTraceLibs6);
                } else if (type.equals("framework")) {
                    writeFileOnInternalStorage(v.getContext(), "trace.cfg", type, traceLevel, fileSize, methodName,
                            traceCallee, callDepth, baseAddr, offset, instrOffset, length, applicationId,
                            otherTraceLibs1, otherTraceLibs2, otherTraceLibs3, traceLibs, otherTraceLibs5,
                            otherTraceLibs6);
                } else if (type.equals("registers")) {
                    writeFileOnInternalStorage(v.getContext(), "trace.cfg", type, traceLevel, fileSize, methodName,
                            traceCallee, callDepth, baseAddr, offset, instrOffset, length, applicationId,
                            otherTraceLibs1, otherTraceLibs2, otherTraceLibs3, otherTraceLibs4, traceLibs,
                            otherTraceLibs6);
                } else if (type.equals("vregs")) {
                    writeFileOnInternalStorage(v.getContext(), "trace.cfg", type, traceLevel, fileSize, methodName,
                            traceCallee, callDepth, baseAddr, offset, instrOffset, length, applicationId,
                            otherTraceLibs1, otherTraceLibs2, otherTraceLibs3, otherTraceLibs4, otherTraceLibs5,
                            traceLibs);
                } else {
                    writeFileOnInternalStorage(v.getContext(), "trace.cfg", type, traceLevel, fileSize, methodName,
                            traceCallee, callDepth, baseAddr, offset, instrOffset, length, applicationId,
                            otherTraceLibs1, otherTraceLibs2, otherTraceLibs3, otherTraceLibs4, otherTraceLibs5,
                            otherTraceLibs6);
                }

                PackageManager pm = v.getContext().getPackageManager();
                final Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
                final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(v.getContext());
                final AlertDialog.Builder dlgAlert1 = new AlertDialog.Builder(v.getContext());
                dlgAlert.setMessage("Please make sure you have closed any application still currently being traced");
                dlgAlert.setTitle("Before Tracing");
                dlgAlert.setPositiveButton("Start Tracing",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dlgAlert1.setMessage("Tracing has started");
                                v.getContext().startActivity(launchIntent);
                                dlgAlert.setPositiveButton("Okay",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                            }
                        });
                dlgAlert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // dismiss the dialog
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        });

        footerStopTraceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d("[writeFileOnInternalStorage]", "[onClick]packageName: " + packageName);
                stopTracing(v.getContext(), packageName, "stop_trace");
            }
        });
    }

    private Button.OnClickListener onClickListener(final String level, final String size, final String methodName,
            final String callee, final String baseAddr, final String offset, final String instrOffset,
            final String length,
            final String packageName, final String applicationId,
            final List<String> libsList, final List<String> classesList, final String apkPath,
            final String lastTraceApp, final String type,
            final String libsStatus, final String classesStatus, final String registersStatus,
            final ArrayList<String> libsAdapter, final ArrayList<String> classesAdapter,
            final ArrayList<String> filterAdapter, final ArrayList<String> frameworkAdapter,
            final ArrayList<String> registersAdapter, final ArrayList<String> vregsAdapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(v.getContext(), com.smu.tracing.GetClassLib.class);
                    intent.removeExtra("classesAdapter");
                    if (null != intent) {
                        intent.putStringArrayListExtra("libsList", (ArrayList<String>) libsList);
                        intent.putExtra("traceLevel", level);
                        intent.putExtra("fileSize", size);
                        intent.putExtra("methodName", methodName);
                        intent.putExtra("traceCallee", callee);
                        intent.putExtra("baseAddr", baseAddr);
                        intent.putExtra("offset", offset);
                        intent.putExtra("instrOffset", instrOffset);
                        intent.putExtra("length", length);
                        intent.putExtra("packageName", packageName);
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
                        v.getContext().startActivity(intent);
                        finish();
                    }
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
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
        // searchView.setQuery("Select your classes/libraries below", false);
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
                // adapter.notifyDataSetChanged();
                return false;
            }
        };
    }

    private static ArrayList<String> retrieveLibs(String apkFileName) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(new File(apkFileName)))) {
            ZipEntry entry;
            ArrayList<String> libs = new ArrayList<String>();
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    if (entry.getName().startsWith("lib") && entry.getName().endsWith(".so")) {
                        libs.add(entry.getName());
                    }
                }
                zis.closeEntry();
            }
            return libs;
        }
    }
}
