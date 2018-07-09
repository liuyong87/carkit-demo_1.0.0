package com.feasycom.feasyblue.Activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.feasycom.bean.DfuFileInfo;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.controler.FscSppCallbacks;
import com.feasycom.controler.FscSppCallbacksImp;
import com.feasycom.feasyblue.Controler.ActivityCollector;
import com.feasycom.feasyblue.R;
import com.feasycom.feasyblue.Util.SettingConfigUtil;
import com.feasycom.util.FileUtil;
import com.feasycom.util.LogUtil;
import com.feasycom.util.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class OtaActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_right_text)
    TextView headerRightText;
    @BindView(R.id.headerLinerLayout)
    LinearLayout headerLinerLayout;
    @BindView(R.id.otaState)
    TextView otaState;
    @BindView(R.id.otaProgress)
    ProgressBar otaProgress;
    @BindView(R.id.progressCount)
    TextView progressCount;
    @BindView(R.id.timeCount)
    TextView timeCount;
    @BindView(R.id.fileName)
    TextView fileName;
    @BindView(R.id.moduleModelName)
    TextView moduleModelName;
    @BindView(R.id.moduleVersion)
    TextView moduleVersion;
    @BindView(R.id.moduleBootloader)
    TextView moduleBootloader;
    @BindView(R.id.dfuModelName)
    TextView dfuModelName;
    @BindView(R.id.dfuVersion)
    TextView dfuVersion;
    @BindView(R.id.dfuBootloader)
    TextView dfuBootloader;
    @BindView(R.id.updateBegin)
    Button updateBegin;
    @BindView(R.id.resetFlag)
    CheckBox resetFlag;

    private final String TAG = "OtaActivity";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_OTA = 2;
    private static final int REQUEST_DFU_FILE = 3;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, OtaActivity.class);
        context.startActivity(intent);
    }

    private Activity activity;
    private byte[] dfuByte;
    private String dfuFileName;
    private String dfuFilePath;
    private String dfuModelNameString;
    private String dfuAppVersionString;
    private String dfuBootLoaderVersionString;
    private FscSppApi fscSppApi;
    private String moduleVersionString;
    private String moduleBootLoaderVersionString;
    private String moduleModleNameString;
    private Handler handler = new Handler();
    /**
     * add for test
     */
    public static boolean OPEN_TEST_MODE = false;
    private int count;
    private int countSuccess;
    private int countFailed;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_ota;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(getString(R.string.OTAUpgrade));
    }

    @Override
    public void initView() {
        if ((dfuFilePath != null) && (dfuFilePath != " ")) {
            handlerFileByte(dfuFilePath);
        }

        resetFlag.setChecked((boolean) SettingConfigUtil.getData(getApplicationContext(), "resetFlag", false));
    }

    @Override
    public void loadData() {
        activity = this;

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        dfuFilePath = (String) SettingConfigUtil.getData(getApplicationContext(), "filePath", " ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        fscSppApi = FscSppApiImp.getInstance(activity);
        if (fscSppApi != null) {
            setCallBacks();
        }
        uiUpdate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_OTA:
                    setCallBacks();
                    /**
                     * it is recommended to put this into the main thread, though it will block the main thread
                     */
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!fscSppApi.startOTA(dfuByte, (boolean) SettingConfigUtil.getData(getApplicationContext(), "resetFlag", false))) {
                                /**
                                 *  if the OTA information does not match, this state does not change
                                 */
                                if ((!otaState.getText().toString().equals(getResources().getString(R.string.versionIsLow)))
                                        && (!otaState.getText().toString().equals(getResources().getString(R.string.nameNotMatch)))) {
                                    otaState.setText(getResources().getString(R.string.updateFailed));
                                }
//                                LogUtil.i(TAG,"disconnect 1");
                                fscSppApi.disconnect();
                                updateBegin.setEnabled(true);
                            }
                        }
                    }, 500);
                    updateBegin.setEnabled(false);
                    otaProgress.setVisibility(View.VISIBLE);
                    progressCount.setVisibility(View.VISIBLE);
                    break;
                case REQUEST_DFU_FILE:
                    Uri uri = data.getData();
                    String filePath = null;
                    try {
                        filePath = FileUtil.getFileAbsolutePath(activity, uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        filePath = null;
                        dfuByte = null;
                        dfuFilePath = null;
                    }
                    if (filePath == null) {
                        ToastUtil.show(getApplicationContext(), getResources().getString(R.string.openSendFileError));
                        dfuByte = null;
                        dfuFilePath = null;
                        return;
                    } else {
                        dfuFilePath = filePath;
                        handlerFileByte(dfuFilePath);
                    }

                    moduleVersionString = null;
                    moduleBootLoaderVersionString = null;
                    moduleModleNameString = null;

                    otaProgress.setProgress(0);
                    otaProgress.setVisibility(View.INVISIBLE);
                    progressCount.setText("0 %");
                    progressCount.setVisibility(View.INVISIBLE);

                    uiUpdate();

                    break;
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if (fscSppApi != null) {
//                LogUtil.i(TAG,"disconnect 2");
                fscSppApi.disconnect();
            }
            activity.finish();
        }
        return true;
    }

    @OnClick(R.id.header_left_image)
    public void goBack() {
        if (fscSppApi != null) {
//            LogUtil.i(TAG,"disconnect 3");
            fscSppApi.disconnect();
        }
        activity.finish();
    }

    @OnClick(R.id.selectFile)
    public void selectDfuFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), REQUEST_DFU_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnCheckedChanged(R.id.resetFlag)
    public void resetSelect(CompoundButton view, boolean flag) {
        SettingConfigUtil.saveData(getApplicationContext(), "resetFlag", flag);
    }

    @OnClick(R.id.updateBegin)
    public void startOta() {
        if ((dfuByte != null) && (dfuFilePath != null) && (!dfuFilePath.equals(" ")) && (dfuFileName != null) && (!dfuFileName.equals(""))) {
            if (OPEN_TEST_MODE) {
                Log.i(TAG, "count " + Integer.valueOf(count++).toString());
            }
            otaState.setText(getResources().getString(R.string.waitingForUpdate));
            otaProgress.setProgress(0);
            otaProgress.setVisibility(View.INVISIBLE);
            progressCount.setText("0 %");
            progressCount.setVisibility(View.INVISIBLE);
            OtaDeviceListActivity.actionStart(activity, REQUEST_OTA);
        } else {
            ToastUtil.show(activity, getResources().getString(R.string.pleaseSelectTheFirmware));
        }
    }

    private void uiUpdate() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /**
                 *  dfu file information update
                 */
                if ((dfuByte != null) && (dfuFileName != null)) {
                    otaState.setText(getResources().getString(R.string.waitingForUpdate));
                    fileName.setText(dfuFileName);
                    DfuFileInfo dfuFileInformation = fscSppApi.checkDfuFile(dfuByte);
                    if (dfuFileInformation != null) {
                        /**
                         * 固件信息改成从.dfu 中解析获取
                         */
                        dfuAppVersionString = Integer.valueOf(dfuFileInformation.versonStart).toString();
                        dfuModelNameString = FileUtil.getModelName(dfuFileInformation.type_model);
                        dfuBootLoaderVersionString = Integer.valueOf(dfuFileInformation.bootloader).toString();
                    } else {
                        dfuAppVersionString = "-";
                        dfuModelNameString = "-";
                        dfuBootLoaderVersionString = "-";
                        ToastUtil.show(getApplicationContext(), getResources().getString(R.string.dfuIllegal));
                    }
                } else {
                    otaState.setText(getResources().getString(R.string.waitingForUpdate));
                    fileName.setText("");
                    dfuAppVersionString = "-";
                    dfuModelNameString = "-";
                    dfuBootLoaderVersionString = "-";
                }
                dfuModelName.setText(dfuModelNameString);
                dfuBootloader.setText(dfuBootLoaderVersionString);
                dfuVersion.setText(dfuAppVersionString);
            }
        });

    }

    private void handlerFileByte(String filePath) {
        int end = 0;
        try {
            dfuFileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
            end = filePath.lastIndexOf(".");
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show(getApplicationContext(), getResources().getString(R.string.openSendFileError));
            end = 0;
            dfuFileName = null;
        }
        if (end > 0) {
            String suffix = filePath.substring(end + 1);
            if (suffix.contains("dfu")) {
                try {
                    dfuByte = FileUtil.readFileToByte(filePath);
                    SettingConfigUtil.saveData(getApplicationContext(), "filePath", filePath);
                } catch (IOException e) {
                    ToastUtil.show(getApplicationContext(), getResources().getString(R.string.openSendFileError));
                    e.printStackTrace();
                }
            } else {
                dfuFileName = null;
                ToastUtil.show(getApplicationContext(), getResources().getString(R.string.selectDfu));
            }
        }
    }

    private boolean checkOtaInformation() {
        /**
         * make sure the module type is the same
         */
        if ((moduleModleNameString != null) && (moduleModleNameString.equals(" ")) && (moduleModleNameString.equals("-")) && (!moduleModleNameString.equals(dfuModelNameString))) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    otaState.setText(getResources().getString(R.string.nameNotMatch));
                }
            });
            return false;
        }

        /**
         * make sure the firmware app version is higher
         */
        if ((moduleVersionString != null) && (!moduleVersionString.equals(" ")) && (!moduleVersionString.equals("-"))) {
            if (Integer.parseInt(moduleVersionString) < Integer.parseInt(dfuAppVersionString)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        otaState.setText(getResources().getString(R.string.versionIsLow));
                    }
                });
                return false;
            }
        }

        /**
         * make sure the firmware bootloader version is higher
         * note that if the module is already in the bootloader state you will get C or S
         */
        if ((moduleBootLoaderVersionString != null) && (!moduleBootLoaderVersionString.equals(" ")) && (!moduleBootLoaderVersionString.equals("-"))
                && (!moduleBootLoaderVersionString.equals("S") && (!moduleBootLoaderVersionString.equals("C")))) {
            if (Integer.parseInt(moduleBootLoaderVersionString) > Integer.parseInt(dfuBootLoaderVersionString)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        otaState.setText(getResources().getString(R.string.blIsHight));
                    }
                });
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fscSppApi != null) {
//            LogUtil.i(TAG,"disconnect 4");
            fscSppApi.disconnect();
        }
    }

    private void setCallBacks() {
        fscSppApi.setCallbacks(new FscSppCallbacksImp() {

            @Override
            public void otaProgressUpdate(final int percentage, final int status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            otaProgress.setProgress(percentage);
                        }catch (NullPointerException e){
                            e.printStackTrace();
                            return;
                        }
                        progressCount.setText(percentage + " %");
                        if (status == FscSppApi.OTA_STATU_FINISH) {
                            otaState.setText(getResources().getString(R.string.updateSuccess));
//                            LogUtil.i(TAG,"disconnect 5");
                            fscSppApi.disconnect();
                            updateBegin.setEnabled(true);

                            if (OPEN_TEST_MODE) {
                                Log.i(TAG, "success count " + Integer.valueOf(countSuccess++).toString());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startOta();
                                    }
                                }, 8000);
                            }
                        } else if (status == FscSppApi.OTA_STATU_FAILED) {
                            if (OPEN_TEST_MODE) {
                                Log.i(TAG, "failed count " + Integer.valueOf(countFailed++).toString());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startOta();
                                    }
                                }, 4000);
                            }
                            /**
                             *  if the OTA information does not match, this state does not change
                             */
                            if ((!otaState.getText().toString().equals(getResources().getString(R.string.versionIsLow)))
                                    && (!otaState.getText().toString().equals(getResources().getString(R.string.nameNotMatch)))) {
                                otaState.setText(getResources().getString(R.string.updateFailed));
                            }
//                            LogUtil.i(TAG,"disconnect 6");
                            fscSppApi.disconnect();
                            updateBegin.setEnabled(true);
                        } else if (status == FscSppApi.OTA_STATU_BEGIN) {
                            if (!checkOtaInformation()) {
//                                LogUtil.i(TAG,"disconnect 7");
                                fscSppApi.disconnect();
                                updateBegin.setEnabled(true);
                            } else {
                                otaState.setText(getResources().getString(R.string.toUpgrade));
                            }
                        } else if (status == FscSppApi.OTA_STATU_PROCESSING) {
                            otaState.setText(getResources().getString(R.string.updating));
                        }
                    }
                });
            }

            @Override
            public void packetReceived(byte[] dataByte, String dataString, String dataHexString) {
                Log.i(TAG, "packetReceive  " + dataString);
                Log.i(TAG, "packetReceive  " + dataString.length());
                Log.i(TAG, "packetReceive  " + dataString.equals(""));
                Log.i(TAG, "packetReceive  " + dataString.equals(" "));
                Log.i(TAG, "packetReceive  " + FileUtil.bytesToHex(dataByte,dataByte.length));
                if (dataString.contains("OK")) {
                    if (dataString.length() >= 15) {
                        moduleVersionString = Integer.valueOf(FileUtil.stringToInt(dataString.substring(3, 7))).toString();
                        moduleBootLoaderVersionString = Integer.valueOf(FileUtil.stringToInt(dataString.substring(7, 11))).toString();
                        moduleModleNameString = FileUtil.getModelName(Integer.valueOf(FileUtil.stringToInt(dataString.substring(11, 15))));
                    } else {
                        moduleVersionString = null;
                        moduleBootLoaderVersionString = null;
                        moduleModleNameString = null;
                    }
                    if (fscSppApi.isConnected()) {
//                        LogUtil.i(TAG,"disconnect 8");
                        fscSppApi.disconnect();
                    }
                } else if (dataString.contains("C")) {
                    moduleVersionString = null;
//                                moduleBootLoaderVersionString = "536";
                    moduleBootLoaderVersionString = "C";
                    moduleModleNameString = null;
                } else if (dataString.contains("S")) {
                    moduleVersionString = null;
//                                moduleBootLoaderVersionString = "565";
                    moduleBootLoaderVersionString = "S";
                    moduleModleNameString = null;
                } else if((dataByte[0] !=0x06) && (dataByte[0] !=0x15) && (dataByte[0] !=0x7F)){
                    moduleVersionString = null;
                    moduleBootLoaderVersionString = null;
                    moduleModleNameString = null;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         *  module information update
                         */
                        if (moduleVersionString != null) {
                            moduleVersion.setText(moduleVersionString);
                        } else {
                            moduleVersion.setText("-");
                        }

                        if (moduleBootLoaderVersionString != null) {
                            moduleBootloader.setText(moduleBootLoaderVersionString);
                        } else {
                            moduleBootloader.setText("-");
                        }

                        if (moduleModleNameString != null) {
                            moduleModelName.setText(moduleModleNameString);
                        } else {
                            moduleModelName.setText("-");
                        }
                    }
                });
            }
        });
    }
}
