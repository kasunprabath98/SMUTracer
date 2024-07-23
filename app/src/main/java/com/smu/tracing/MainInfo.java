package com.smu.tracing;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainInfo extends AppCompatActivity {
    public int traceLevel;
    public long fileSize;
    public String methodName;
    public int traceCallee;
    public String callDepth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info);

        Button button = (Button) findViewById(R.id.button_info);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });

        // Retrieve trace level based on user selected checkbox created in TraceType class
        traceLevel = getIntent().getIntExtra("traceLevel",0);
        fileSize = getIntent().getLongExtra("fileSize", 0);
        methodName = getIntent().getStringExtra("methodName");
        traceCallee = getIntent().getIntExtra("traceCallee",0);
        callDepth = getIntent().getStringExtra("callDepth");
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("traceLevel", traceLevel);
        intent.putExtra("fileSize", fileSize);
        intent.putExtra("methodName", methodName);
        intent.putExtra("traceCallee", traceCallee);
        intent.putExtra("callDepth", callDepth);
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
    }
}