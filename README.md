# Compilation and Usage of third party app (component 2 - "SMUTracer"):

## 1.

- Launch Android Studio
  - Import the SMUTracer app project into Android studio via File -> New -> Import Project.
    Select the path to the SMUTracer project (fine-grained-tracing/android-<version>/SMUTracer) and press OK. This will begin the import process.
- Compile and install the application to the Android Device using the 'play' button located on the toolbar. The play button is a green triangle icon.
- Note that the device needs to be rooted for the application to function properly.

## 2.

- Run the following commands to gain root access and to navigate to different folders of the device.
  _adb root_
  _adb shell_

  Note: Running "adb shell" goes into command line interface (CLI) of the Android device.

- Disable SELinux:
  _adb shell /system/bin/setenforce 0_

  Change folder permissions [Android 10 only]:

- Navigate to folder "/data/" and use the command "chmod -R 777 /local".
- Navigate to folder "/data/" and use the command "chmod -R 777 app/".
- The "chmod" commands above are to ensure that we have read and write permissions to read the libs, apk, etc... and write new log files to our internal app storage.
- Exit from command line interface:
  _exit_

- Alternatively, you may also change the file permissions without going into CLI mode using the following commands:
  _adb shell chmod -R 777 /data/local/_
  _adb shell chmod -R 777 /data/app_

  Create folder structure:
  _adb shell mkdir -p /data/local/tmp/com.smu.tracing/files/trace_
  _adb shell touch /data/local/tmp/com.smu.tracing/files/trace/trace.cfg_

## 3.

After successful installation, and changing the read/write permissions, we can now start using the application.

- a. From the start menu, the application displays different types of trace modes i.e. - Method (native method tracing) - Method (interpreter method tracing) - Branch (method and branch tracing in interpreter mode) - Library (shared library tracing) - Instruction (DEX instruction trace)

         Select the mode that you wish to use.

- b. If native method tracing (first option) is selected in (a), the user will be prompted if they want to recompile the app natively.
  Upon entering yes, the user will be presented with further instructions to perform the compilation.

- c. For other tracing modes, the user will be presented with a list of apps that is available to trace.
  Select one app to trace from the list.

- d. If method(native/interpreter)/branch/instruction trace mode was selected in (a), click on the 'CLASSES' tab in the toolbar to select the list of classes to trace for the app selected.
  Once selected, scroll to the bottom of the list and click on 'Start Tracing'.

         If library trace mode was selected in (a), select the list of shared libraries (.so) to trace in the 'LIBS' tab in the toolbar.
         Additionally, select the list of classes to trace in the 'CLASSES' tab.
         Once selected, scroll to the bottom of the list and click on 'Start Tracing'.

- e. [OPTIONAL] Depending on the type of trace mode selected in (a), we can optionally perform debugging using the following combinations i.e.

         ==== Method (native) / Method (interpreter) / Branch / Shared library with SP, general purpose registers (X0-X30) and vregisters ====
         After selecting the tracing mode and the list of classes to trace, go to the 'REGS' tab and select the list of registers for debugging.

  Then go to the 'VREGS' tab and select the list of vregisters for debugging
  Once the required registers are selected, begin tracing by selecting 'Start Tracing'.
  Note: SP is not shown in the 'REGS' tab but it will always be traced by default in the system.

         ==== Instruction tracing with vregisters ====
         After selecting instruction tracing and the list of classes to trace, go to the 'VREGS' tab and select the list of vregisters for debugging


         ==== Field read/write event ====
         There is a special mode called field read/write event tracing. This can be done with any trace mode selected because this is a mode by itself.
         All that needs to be done is to select the classes to trace and select either 'FIELD_READ' or 'FIELD_WRITE' from the top toolbar and begin tracing.
         At this point in time, selection of which field to trace is not possible. This can be a future improvement.

- f. After performing the tracing in the app, you may stop the tracing by going by to SMUTracer and click on the 'Stop Tracing' button.
  This will save the remaining logs in the buffer to memory.

  During tracing you might also notice some logs printed in the logcat window in Android studio in the following general format:
  [Trace] File Writing on /data/local/tmp/com.smu.tracing/files/trace/<filename>_<TID>_<file_num>.trace

  The logcat window can be found at the bottom toolbar and is labelled as "Logcat".

- g. The SMUTracer application displays different external applications installed in the device.
- h. Using the search bar at the top of the menu: we can filter for the applications we want to look for.
- i. The status screen at the bottom of the application displays the application stored in the trace.cfg file (to be traced), and the most recently traced application.

## 4.

After clicking on one of the external applications from the listview, the app directs us to a different page.

- a. This page contains the different libraries and classes which the external app uses.
- b. We can choose the libs and classes we want to trace by clicking on them manually or by using the "Select All" and "Clear All" buttons.
- c. After we are done with our selections, we can start tracing by clicking on "Start Tracing".
- d. NOTE: Please remember to close the most recently traced application before tracing another application.
  (the status screen at the bottom of the main menu will be useful for this).

## 5.

- We can manually look at the trace files produced by using the "Device Explorer" feature of android studio
- In "Device Explorer", navigate to folder "/data/local/tmp/com.smu.tracing/files/trace/", all the trace files should be here in the format of ".trace"
