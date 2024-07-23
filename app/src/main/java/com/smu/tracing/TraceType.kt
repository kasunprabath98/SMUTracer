package com.smu.tracing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class TraceType : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var traceLevel: Int = 0
    var fileSize: Long = 0
    var checked: Int = 0
    private var strUnit: String? = null

    // variable for trigger tracing feature
    private var methodName: String? = null

    // variables for tracing child methods
    var traceCallee: Int = 0
    private var callDepth: String? = null
    private var traceCalleeEnableBtn: ToggleButton? = null

    // variables for memory debug feature
    private var baseAddr: String? = null
    private var offset: String? = null
    private var instrOffset: String? = null
    private var length: String? = null // number of words

    private var autoCompleteTextView: AutoCompleteTextView? = null
    private var autoCompleteTextTraceFileSize: AutoCompleteTextView? = null
    private var autoCompleteTextViewBaseAddress: AutoCompleteTextView?= null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_get_trace_type)

        // init variables
        strUnit = "MB"
        methodName = ""
        baseAddr = "SP"


        autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        autoCompleteTextTraceFileSize = findViewById(R.id.autoCompleteTextTraceFileSize);
        autoCompleteTextViewBaseAddress = findViewById(R.id.base_address)

        val items = arrayOf("Method (native)", "Method (interpreter)", "Branch", "Library", "Instruction")
        val dropDownAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        autoCompleteTextView?.setAdapter(dropDownAdapter)
        autoCompleteTextView?.setOnItemClickListener { adapterView, view, i, l ->
            traceLevel = i+1;
        }

        val fileSizeAdapter = ArrayAdapter.createFromResource(this , R.array.filesize , android.R.layout.simple_spinner_dropdown_item );
        autoCompleteTextTraceFileSize?.setAdapter(fileSizeAdapter);
        autoCompleteTextTraceFileSize?.setOnItemClickListener{
                adapterView, view, i, l ->

        }

        val baseAddressesAdapter =  ArrayAdapter.createFromResource(this , R.array.baseaddr ,  android.R.layout.simple_spinner_dropdown_item ,)
        autoCompleteTextViewBaseAddress?.setAdapter(baseAddressesAdapter)



        traceCalleeEnableBtn = findViewById<View>(R.id.toggleButton) as ToggleButton

        val traceCfg = File("/data/local/tmp/com.smu.tracing");

        val button = findViewById<View>(R.id.button_trace) as Button
        val buttonTraceCfgDel = findViewById<View>(R.id.button_tracecfg_del) as Button
        val textTraceFileSize = findViewById<View>(R.id.trace_file_size) as EditText
        val textMethodName = findViewById<View>(R.id.textMethod) as EditText
        val textCallDepth = findViewById<View>(R.id.textCallDepth) as EditText
        val textOffset = findViewById<View>(R.id.textOffset) as EditText
        val textInstructionOffset = findViewById<View>(R.id.textInstructionOffset) as EditText
        val textLength = findViewById<View>(R.id.textLength) as EditText

        // Drop-down menu for file size unit
        val spinner = findViewById<View>(R.id.spinnerfilesize) as Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.filesize,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0, false)
        spinner.onItemSelectedListener = this

        // Drop-down menu for selecting trigger type
        //Spinner spinner_trigger = (Spinner) findViewById(R.id.spinnerTriggerType);
        //ArrayAdapter<CharSequence> adapter_trigger = ArrayAdapter.createFromResource(this, R.array.triggertype, android.R.layout.simple_spinner_item);
        //adapter_trigger.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner_trigger.setAdapter((adapter_trigger));
        //spinner_trigger.setSelection(0,false);
        //spinner_trigger.setOnItemSelectedListener(this);

        // Drop-down menu for selecting base address
        val spinner_base_addr = findViewById<View>(R.id.spinnerBaseAddr) as Spinner
        val adapter_base_addr = ArrayAdapter.createFromResource(
            this,
            R.array.baseaddr,
            android.R.layout.simple_spinner_item
        )
        adapter_base_addr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_base_addr.adapter = adapter_base_addr
        spinner_base_addr.setSelection(0, false)
        spinner_base_addr.onItemSelectedListener = this

        button.setOnClickListener { view ->
            if (textTraceFileSize.text.toString() == "") {
                fileSize = (1 * 1024 * 1024).toLong() // default 1MB
                strUnit = "MB"
                val toast = Toast.makeText(
                    view.context,
                    "You have not input a size. Default 1MB was set by the application",
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                toast.show()
            } else {
                fileSize = textTraceFileSize.text.toString().toInt().toLong()
                fileSize = sizeToUnit(fileSize, strUnit!!)
            }
            methodName = textMethodName.text.toString()
            callDepth = textCallDepth.text.toString()
            offset = textOffset.text.toString()
            instrOffset = textInstructionOffset.text.toString()
            length = textLength.text.toString()
            openNextActivity()
        }

        buttonTraceCfgDel.setOnClickListener { view ->
            if (traceCfg.exists()) {
                if (deleteDirectory(traceCfg)) {
                    Toast.makeText(
                        view.context,
                        "Trace config file deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        view.context,
                        "Failed to delete trace config file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    view.context,
                    "Trace config already deleted. Proceed to create folder and file via adb",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        // trace callees in unselected classes toggle button
        traceCalleeEnableBtn!!.setOnClickListener {
            traceCallee = if (traceCalleeEnableBtn!!.isChecked) {
                1
            } else {
                0
            }
        }
    }

    fun deleteDirectory(directoryToBeDeleted: File): Boolean {
        val allContents = directoryToBeDeleted.listFiles()
        if (allContents != null) {
            for (file in allContents) {
                deleteDirectory(file)
            }
        }
        return directoryToBeDeleted.delete()
    }

    fun sizeToUnit(size: Long, unit: String): Long {
        var size = size
        if (unit == "MB") {
            size = size * 1024 * 1024
        } else if (unit == ("kB")) {
            size = size * 1024
        } else if (unit == ("B")) {
            size = size
        } else {
            Log.e("ERROR", "sizeToUnits: Invalid unit specified")
        }
        return size
    }

    private fun openNextActivity() {

        val intent = if (traceLevel == 1) {
            Intent(this, PromptCompile::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        intent.putExtra("traceLevel", traceLevel)
        intent.putExtra("fileSize", fileSize)
        intent.putExtra("methodName", methodName)
        intent.putExtra("traceCallee", traceCallee)
        intent.putExtra("callDepth", callDepth)
        intent.putExtra("baseAddr", baseAddr)
        intent.putExtra("offset", offset)
        intent.putExtra("instrOffset", instrOffset)
        intent.putExtra("length", length)
        setResult(RESULT_OK, intent)
        startActivity(intent)
    }


    override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        val spinner = findViewById<View>(R.id.spinnerfilesize) as Spinner
        //Spinner spinner_trigger = (Spinner) findViewById(R.id.spinnerTriggerType);
        val spinner_baseaddr = findViewById<View>(R.id.spinnerBaseAddr) as Spinner

        val id = adapterView.id
        when (id) {
            R.id.spinnerfilesize -> {
                strUnit = spinner.selectedItem.toString()
                Toast.makeText(
                    adapterView.context,
                    "You have selected $strUnit",
                    Toast.LENGTH_SHORT
                ).show()
            }

            R.id.spinnerBaseAddr -> {
                baseAddr = spinner_baseaddr.selectedItem.toString()
                Toast.makeText(
                    adapterView.context,
                    "You have selected $baseAddr",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
    }
}