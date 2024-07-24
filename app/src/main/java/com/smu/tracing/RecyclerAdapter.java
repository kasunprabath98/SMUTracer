package com.smu.tracing;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    String data1[];
    List<String> data;
    List<String> dataAll;
    int images[];
    Context context;

    // Variables for GetClassLib
    private String traceLevel;
    private String filesize;
    private String methodName;
    private String traceCallee;
    private String callDepth;
    private String baseAddr;
    private String offset;
    private String instrOffset;
    private String length;
    private String packageName;
    private String applicationId;
    private String apkPath;
    private String lastTraceApp;
    private String type;
    private String libsStatus;
    private String classesStatus;
    private String registersStatus;
    ArrayList<String> libsList;
    private ArrayList<String> libsAdapter;
    private ArrayList<String> classesAdapter;
    private ArrayList<String> filterAdapter;
    private ArrayList<String> frameworkAdapter;
    private ArrayList<String> registersAdapter;
    private ArrayList<String> vregsAdapter;

    public RecyclerAdapter(Context ct, String s1[], String traceLevel, String filesize, String methodName, String traceCallee, String callDepth, String baseAddr, String offset, String instrOffset, String length,
                           String packageName, String applicationId, ArrayList<String> libsList, String apkPath, String lastTraceApp, String type,
                           String libsStatus, String classesStatus, String registersStatus,
                           ArrayList<String> libsAdapter, ArrayList<String> classesAdapter, ArrayList<String> filterAdapter, ArrayList<String> frameworkAdapter, ArrayList<String> registersAdapter, ArrayList<String> vregsAdapter) {
        this.context = ct;
        this.data1 = s1;
        this.data = new ArrayList<String>(Arrays.asList(data1));
        this.dataAll = new ArrayList<String>(Arrays.asList(data1));

        this.traceLevel = traceLevel;
        this.filesize = filesize;
        this.methodName = methodName;
        this.baseAddr = baseAddr;
        this.traceCallee = traceCallee;
        this.callDepth = callDepth;
        this.offset = offset;
        this.instrOffset = instrOffset;
        this.length = length;
        this.packageName = packageName;
        this.applicationId = applicationId;
        this.libsList = libsList;
        this.apkPath = apkPath;
        this.lastTraceApp = lastTraceApp;
        this.type = type;
        this.libsStatus = libsStatus;
        this.classesStatus = classesStatus;
        this.registersStatus = registersStatus;
        this.libsAdapter = libsAdapter;
        this.classesAdapter = classesAdapter;
        this.filterAdapter = filterAdapter;
        this.frameworkAdapter = frameworkAdapter;
        this.registersAdapter = registersAdapter;
        this.vregsAdapter = vregsAdapter;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.button.setText(data.get(position));
        holder.button.setTextSize(16);
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,com.smu.tracing.GetClassLib.class);
                try {
                    if (null != intent) {
                        intent.putStringArrayListExtra("libsList", (ArrayList<String>) libsList);
                        intent.putExtra("traceLevel", traceLevel);
                        intent.putExtra("fileSize", filesize);
                        intent.putExtra("methodName", methodName);
                        intent.putExtra("traceCallee", traceCallee);
                        intent.putExtra("callDepth", callDepth);
                        intent.putExtra("baseAddr", baseAddr);
                        intent.putExtra("offset", offset);
                        intent.putExtra("instrOffset", instrOffset);
                        intent.putExtra("length", length);
                        intent.putExtra("packageName", packageName);
                        intent.putExtra("applicationId", applicationId);
                        intent.putExtra("apkPath", apkPath);
                        intent.putExtra("lastTraceApp", lastTraceApp);
                        if (position == 0)
                            intent.putExtra("type", "libs");
                        if (position == 1)
                            intent.putExtra("type", "classes");
                        if (position == 2)
                            intent.putExtra("type", "filter");
                        if (position == 3)
                            intent.putExtra("type", "framework");
                        if (position == 4)
                            intent.putExtra("type", "registers");
                        if (position == 5)
                            intent.putExtra("type", "vregs");
                        if (position == 6)
                            intent.putExtra("type", "fieldread");
                        if (position == 7)
                            intent.putExtra("type", "fieldwrite");

                        intent.putExtra("libsStatus", libsStatus);
                        intent.putExtra("classesStatus", classesStatus);
                        intent.putExtra("registersStatus", registersStatus);
                        intent.putExtra("libsAdapter", (ArrayList<String>) libsAdapter);
                        intent.putExtra("classesAdapter", (ArrayList<String>) classesAdapter);
                        intent.putExtra("filterAdapter", (ArrayList<String>) filterAdapter);
                        intent.putExtra("frameworkAdapter", (ArrayList<String>) frameworkAdapter);
                        intent.putExtra("registersAdapter", (ArrayList<String>) registersAdapter);
                        intent.putExtra("vregsAdapter", (ArrayList<String>) vregsAdapter);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // remove the previous activities
                        context.startActivity(intent);

                    }
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView myText1, myText2;
        ImageView myImage;
        Button button;

        public MyViewHolder(View itemView) {
            super(itemView);
            //myText1 = itemView.findViewById(R.id.textView1);
            button = (Button) itemView.findViewById(R.id.button);
        }
    }
}
