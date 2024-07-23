package com.smu.tracing;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class PromptCompile extends AppCompatActivity {
    public int compileFlag;
    public int traceLevel;
    public int traceCallee;
    public long fileSize;
    public String methodName;
    public String callDepth;
    public String baseAddr;
    public String offset;
    public String instrOffset;
    public String length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_compile);

        final CheckBox checkbox[] = new CheckBox[2];

        checkbox[0] = (CheckBox) findViewById(R.id.checkBoxYes);
        checkbox[1] = (CheckBox) findViewById(R.id.checkBoxNo);

        checkbox[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkbox[0].isChecked() == true) {
                    checkbox[1].setChecked(false);
                    compileFlag = 1;
                }
            }
        });

        checkbox[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkbox[1].isChecked() == true) {
                    checkbox[0].setChecked(false);
                    compileFlag = 0;
                }
            }
        });

        // Retrieve trace level based on user selected checkbox created in TraceType class
        traceLevel = getIntent().getIntExtra("traceLevel",0);
        fileSize = getIntent().getLongExtra("fileSize", 0);
        methodName = getIntent().getStringExtra("methodName");
        traceCallee = getIntent().getIntExtra("traceCallee", 0);
        callDepth = getIntent().getStringExtra("callDepth");
        baseAddr = getIntent().getStringExtra("baseAddr");
        offset = getIntent().getStringExtra("offset");
        instrOffset = getIntent().getStringExtra("instrOffset");
        length = getIntent().getStringExtra("length");

        Button button = (Button) findViewById(R.id.button_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity(compileFlag);
            }
        });
    }

    public void openMainActivity(int flag) {
        Intent intent;
        if (flag == 1) {
            intent = new Intent(this,MainInfo.class);
        } else {
            intent = new Intent(this,MainActivity.class);
        }
        intent.putExtra("traceLevel", traceLevel);
        intent.putExtra("fileSize", fileSize);
        intent.putExtra("methodName", methodName);
        intent.putExtra("traceCallee", traceCallee);
        intent.putExtra("callDepth", callDepth);
        intent.putExtra("baseAddr", baseAddr);
        intent.putExtra("offset", offset);
        intent.putExtra("instrOffset", instrOffset);
        intent.putExtra("length", length);

        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
    }
}