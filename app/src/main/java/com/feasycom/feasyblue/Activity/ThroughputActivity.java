package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.method.DigitsKeyListener;
import android.text.method.TextKeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feasycom.bean.BluetoothDeviceWrapper;
import com.feasycom.controler.FscBleCentralApi;
import com.feasycom.controler.FscBleCentralApiImp;
import com.feasycom.controler.FscBleCentralCallbacksImp;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.controler.FscSppCallbacksImp;
import com.feasycom.feasyblue.Controler.ActivityCollector;
import com.feasycom.feasyblue.R;
import com.feasycom.feasyblue.Util.SettingConfigUtil;
import com.feasycom.util.FileUtil;
import com.feasycom.util.LogUtil;
import com.feasycom.util.ToastUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.CRC32;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.feasycom.feasyblue.Activity.SearchDeviceActivity.AUTH_TEST;

public class ThroughputActivity extends BaseActivity {
    private static int AUTH_TEST_COUNT = 0;

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_title_msg)
    TextView headerTitleMsg;
    @BindView(R.id.header_right_button)
    Button headerRightButton;
    @BindView(R.id.headerLinerLayout)
    LinearLayout headerLinerLayout;
    @BindView(R.id.byte_count_receive)
    TextView byteCountReceive;
    @BindView(R.id.packge_count_receive)
    TextView packgeCountReceive;
    @BindView(R.id.crc32_receive)
    TextView crc32Receive;
    @BindView(R.id.byte_count_send)
    TextView byteCountSend;
    @BindView(R.id.packge_count_send)
    TextView packgeCountSend;
    @BindView(R.id.crc32_send)
    TextView crc32Send;
    @BindView(R.id.send_packge_button)
    Button sendPackgeButton;
    //    @BindView(R.id.send_file_button)
    public static Button sendFileButton;
    @BindView(R.id.interval_send_time)
    EditText intervalSendTime;
    @BindView(R.id.interval_send_check)
    CheckBox intervalSendCheck;
    @BindView(R.id.hex_check)
    CheckBox hexCheck;
    @BindView(R.id.send_edit)
    EditText sendEdit;
    @BindView(R.id.receive_edit)
    EditText receiveEdit;

    private final int REQUEST_SEND_FILE = 1;
    @BindView(R.id.data_percentage)
    TextView dataPercentage;
    @BindView(R.id.switchServiceButton)
    Button switchServiceButton;
    @BindView(R.id.editByteCount)
    TextView editByteCount;

    private final String TAG="Throughput";
    private BluetoothDeviceWrapper deviceDetail;
    private Activity activity;
    public static Handler handler;
    private FscBleCentralApi fscBleCentralApi = null;
    private FscSppApi fscSppApi = null;
    String mac;                                                              // 设备地址
    byte[] currentPackge;                                                    // 当前发送的数据包
    byte[] fileByte;                                                         // 文件字节数组
    /**
     *
     */
    public static int sendByte;                                              // 发送字节数
    public static int sendPackge;                                            // 发送包数
    public static int sendPercentage;                                        // 发送百分比 (发送文件用)
    public static Stack<Integer> percentageStack = new Stack<Integer>();     // 用于缓存接收到的发送百分比
    public static CRC32 sendCRC = new CRC32();                               // 发送crc
    public static int receiveByte;                                           // 接收字节数
    public static int receivePackge;                                         // 接收包数
    public static CRC32 receiveCRC = new CRC32();                            // 接收crc
    private TimerTask timerSendTask;                                         // 定时发送任务
    private TimerTask timerUITask;                                           // 更新UI任务
    private Timer timerSend = new Timer();                                   // 发送计时器
    private Timer timerUI = new Timer();                                     // UI更新计时器
    public static StringBuffer receiveBuffer = new StringBuffer();           // 接收数据缓存buffer string类型
    public static StringBuffer receiveBufferHex = new StringBuffer();        // 接收数据缓存buffer hex string 类型
    public String currentMode;                                               // 当前模式 BLE or SPP
    public static ArrayList<BluetoothGattService> serviceList;
    public static BluetoothGattCharacteristic currentWriteCharacteristic;
    private boolean PAUSE_STATUE = false;
    // for fix bug
    private boolean FIRST_ENTER = true;

    public static void actionStart(Context context, BluetoothDeviceWrapper deviceDetail) {
        Intent intent = new Intent(context, ThroughputActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("device", deviceDetail);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCallBacks();

        /**
         * update the connection status of ble after PAUSE_STATUE
         */
        if ((BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) && PAUSE_STATUE) {
            if (!fscBleCentralApi.isConnected()) {
                headerTitleMsg.setText(getResources().getString(R.string.disconnected));
            }
        }
        PAUSE_STATUE = false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_throughput;
    }

    @Override
    public void refreshFooter() {
    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(deviceDetail.getName());
        headerTitleMsg.setText(getResources().getString(R.string.connecting));
        headerRightButton.setText(getResources().getString(R.string.clear));
    }

    @Override
    public void initView() {
        if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
            switchServiceButton.setVisibility(View.VISIBLE);
        } else if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
            switchServiceButton.setVisibility(View.GONE);
        }

        SpannableString spannableString = new SpannableString(getResources().getString(R.string.intervalHint));
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(12, true);
        spannableString.setSpan(absoluteSizeSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        intervalSendTime.setHint(new SpannedString(spannableString));

        uiDataUpdate();
        timerUI.schedule(timerUITask, 1000, 1000);

        boolean isHexCheck = (Boolean) SettingConfigUtil.getData(getApplicationContext(), "hexCheck", false);
        if (!isHexCheck) {
            FIRST_ENTER = false;
            sendEdit.setText((String) SettingConfigUtil.getData(getApplicationContext(), "editInfo", ""));
        } else {
            hexCheck.setChecked(true);
        }
    }

    @Override
    public void loadData() {
        clearData();
        activity = this;
        handler = new Handler();
        deviceDetail = (BluetoothDeviceWrapper) getIntent().getSerializableExtra("device");
        currentMode = deviceDetail.getModel();
        mac = deviceDetail.getAddress();
        setCallBacks();
        timerUITask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uiDataUpdate();
                    }
                });
            }
        };
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectDevice();
            }
        }, 200);
        sendFileButton = (Button) findViewById(R.id.send_file_button);

    }


    @OnClick(R.id.switchServiceButton)
    public void switchService() {
        ServiceSelectActivity.actionStart(activity, deviceDetail);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            disconnectDevice();
            activity.finish();
        }
        return true;
    }

    @OnClick(R.id.header_left_image)
    public void goBack() {
        disconnectDevice();
        activity.finish();
    }

    @OnClick(R.id.send_packge_button)
    public void sendPackge() {
        if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
            fscBleCentralApi.send(currentPackge);
        } else {
            fscSppApi.send(currentPackge);
        }
    }

    @OnClick(R.id.send_file_button)
    public void sendFile() {
        if (((null != fscSppApi && fscSppApi.isConnected())
                || (null != fscBleCentralApi && fscBleCentralApi.isConnected()))
                && sendFileButton.getText().equals(getResources().getString(R.string.sendFile))) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_SEND_FILE);
        } else if (((null != fscSppApi && fscSppApi.isConnected())
                || (null != fscBleCentralApi && fscBleCentralApi.isConnected()))
                && sendFileButton.getText().equals(getResources().getString(R.string.stop))) {

            if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
                fscSppApi.stopSend();
            } else {
                fscBleCentralApi.stopSend();
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendPercentage = 0;
                    sendFileButton.setText(getResources().getString(R.string.sendFile));
                }
            }, 1500);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_SEND_FILE) {
            Uri uri = data.getData();
            String filePath = null;
            try {
                filePath = FileUtil.getFileAbsolutePath(this.getApplicationContext(), uri);
            } catch (Exception e) {
                e.printStackTrace();
                filePath = null;
            }
            if (null == filePath) {
                ToastUtil.show(getApplicationContext(), getResources().getString(R.string.openSendFileError));
                return;
            }
            try {
                fileByte = FileUtil.readFile(filePath);
            } catch (Exception e) {
                ToastUtil.show(getApplicationContext(), getResources().getString(R.string.openSendFileError));
                e.printStackTrace();
                return;
            }
            if (null == fileByte) {
                ToastUtil.show(getApplicationContext(), getResources().getString(R.string.openSendFileError));
                return;
            }

            sendFileButton.setText(getResources().getString(R.string.stop));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
                        fscSppApi.send(fileByte);
                    } else {
                        fscBleCentralApi.send(fileByte);
                    }
                }
            }, 1000);
        }
    }

    @OnCheckedChanged({R.id.interval_send_check, R.id.hex_check})
    public void throughoutCheck(CompoundButton v, boolean flag) {
        switch (v.getId()) {
            case R.id.interval_send_check:
                if (flag) {
                    timerSendTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (currentPackge.length > 0) {
                                sendPackge();
                            }
                        }
                    };
                    if (intervalSendTime.getText().length() < 1) {
                        intervalSendTime.setText("0");
                    }
                    if ((Integer.valueOf(intervalSendTime.getText().toString()).intValue() > 1000)
                            || (Integer.valueOf(intervalSendTime.getText().toString()).intValue() < 0)) {
                        ToastUtil.show(activity, "[0,1000]");
                        intervalSendCheck.setChecked(false);
                    } else {
                        if (fscBleCentralApi != null) {
                            fscBleCentralApi.setSendInterval(Integer.valueOf(intervalSendTime.getText().toString()).intValue());
                        } else if (fscSppApi != null) {
                            fscSppApi.setSendInterval(Integer.valueOf(intervalSendTime.getText().toString()).intValue());
                        }
                        timerSend.schedule(timerSendTask, 50, 10);
                    }
                } else {
                    if (null != timerSendTask) {
                        timerSendTask.cancel();
                        timerSendTask = null;
                    }
                }
                break;
            case R.id.hex_check:
                if (flag) {
                    if (FIRST_ENTER) {
                        FIRST_ENTER = false;
                        sendEdit.setText((String) SettingConfigUtil.getData(getApplicationContext(), "editInfo", ""));
                    } else {
                        byte[] byteTemp;
                        try {
                            byteTemp = ((String) SettingConfigUtil.getData(getApplicationContext(), "editInfo", "")).getBytes("UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                            byteTemp = null;
                        }
                        if (byteTemp != null) {
                            sendEdit.setText(FileUtil.bytesToHex(byteTemp, byteTemp.length));
                        }
                    }
                    sendEdit.setKeyListener(DigitsKeyListener.getInstance(getResources().getString(R.string.digitsHex)));
                } else {
                    byte[] temp = FileUtil.hexToByte(sendEdit.getText().toString());
                    String aa = new String(temp);
                    sendEdit.setText(aa);
                    sendEdit.setKeyListener(TextKeyListener.getInstance());
                }
                SettingConfigUtil.saveData(getApplicationContext(), "editInfo", sendEdit.getText().toString());
                SettingConfigUtil.saveData(getApplicationContext(), "hexCheck", flag);
                break;
        }

    }


    @OnClick(R.id.header_right_button)
    public void clearData() {
        sendByte = 0;
        sendPackge = 0;
        sendCRC.reset();
        receiveByte = 0;
        receivePackge = 0;
        receiveCRC.reset();

        receiveBuffer.delete(0, receiveBuffer.length());
        receiveBuffer.setLength(0);

        receiveBufferHex.delete(0, receiveBufferHex.length());
        receiveBufferHex.setLength(0);

        percentageStack.clear();
    }

    @OnTextChanged(R.id.send_edit)
    public void saveSendEditInfo(Editable e) {
        SettingConfigUtil.saveData(getApplicationContext(), "editInfo", e.toString());
        try {
            if (hexCheck.isChecked()) {
                editByteCount.setText(Integer.valueOf(sendEdit.getText().toString().replace(" ", "").length() / 2).toString() + getResources().getString(R.string.byteString));
            } else {
//                editByteCount.setText(Integer.valueOf(sendEdit.getText().toString().replace(" ", "").length()).toString() + getResources().getString(R.string.byteString));
                editByteCount.setText(sendEdit.getText().toString().getBytes("UTF-8").length + getResources().getString(R.string.byteString));
            }
        } catch (Exception s) {
            editByteCount.setText("0" + getResources().getString(R.string.byteString));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (null != timerSendTask) {
            timerSendTask.cancel();
        }
        if (null != timerUITask) {
            timerUITask.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PAUSE_STATUE = true;
    }

    private void uiDataUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(null == byteCountSend){return;}
                byteCountSend.setText(Integer.valueOf(sendByte).toString());
                packgeCountSend.setText(Integer.valueOf(sendPackge).toString());
                crc32Send.setText(Long.toHexString(sendCRC.getValue()).toUpperCase());
                packgeCountReceive.setText(Integer.valueOf(receivePackge).toString());
                byteCountReceive.setText(Integer.valueOf(receiveByte).toString());
                crc32Receive.setText(Long.toHexString(receiveCRC.getValue()).toUpperCase());

                try {
                    sendPercentage = percentageStack.peek().intValue();
                    dataPercentage.setText(Integer.valueOf(sendPercentage).toString() + "%");
                } catch (Exception e) {
                    dataPercentage.setText("");
                }

                if (hexCheck.isChecked()) {
                    currentPackge = FileUtil.hexToByte(sendEdit.getText().toString());
                    receiveEdit.setText(new String(receiveBufferHex));
                    if (receiveEdit.getText().toString().length() > 100) {
                        receiveEdit.setSelection(receiveEdit.getText().toString().length() - 1);
                    }
                } else {
                    try {
                        currentPackge = sendEdit.getText().toString().getBytes("UTF-8");
                    } catch (Exception e) {
                    }
                    receiveEdit.setText(new String(receiveBuffer));
                    if (receiveEdit.getText().toString().length() > 100) {
                        receiveEdit.setSelection(receiveEdit.getText().toString().length() - 1);
                    }
                }
            }
        });
    }

    private void connectDevice() {
        if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
            fscBleCentralApi.connect(mac);
        } else if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
            fscSppApi.connect(mac);
        }
    }

    private void disconnectDevice() {
        if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
            fscBleCentralApi.disconnect();
        } else if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
            fscSppApi.disconnect();
        }
    }

    private void uiHandlerDeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switchServiceButton.setEnabled(true);
                    }
                }, 1000);
                headerTitleMsg.setText(getResources().getString(R.string.connected));
            }
        });
    }

    private void uiHandlerDeviceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                headerTitleMsg.setText(getResources().getString(R.string.disconnected));
            }
        });
    }

    private void uiHandlerReceiveData(byte[] rawValue, String strValue, String hexString) {
        receivePackge++;
        receiveByte = receiveByte + rawValue.length;
        receiveCRC.update(rawValue);
        receiveBuffer.append(strValue);
        receiveBufferHex.append(hexString.toUpperCase());
        if (receiveBuffer.length() > 1024) {
            receiveBuffer.delete(0, receiveBuffer.length());
            receiveBuffer.setLength(0);
        }
        if (receiveBufferHex.length() > 1024) {
            receiveBufferHex.delete(0, receiveBufferHex.length());
            receiveBufferHex.setLength(0);
        }
    }

    private void uiHandlerSendDataProgress(int percentage, byte[] tempByte) {
        if (!percentageStack.contains(percentage)) {
            percentageStack.push(percentage);
        }
        if (percentage == FscSppApi.PACKGE_SEND_FINISH) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (sendFileButton.getText().equals(getResources().getString(R.string.stop))) {
                        sendFileButton.setText(getResources().getString(R.string.sendFile));
                    }
                    percentageStack.clear();
                }
            }, 1500);
        }
        if (null != tempByte) {
            sendPackge++;
            sendByte = sendByte + tempByte.length;
            sendCRC.update(tempByte);
        } else {
            percentageStack.clear();
        }
    }

    public void setCallBacks() {
        if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
            fscSppApi = FscSppApiImp.getInstance(activity);
            fscSppApi.setCallbacks(new FscSppCallbacksImp() {
                @Override
                public void sppConnected(BluetoothDevice device) {
                    uiHandlerDeviceConnected();
                    if (AUTH_TEST) {
                        Log.i(TAG,"count "+Integer.valueOf(AUTH_TEST_COUNT++).toString());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "test disconnect");
                                disconnectDevice();
                                activity.finish();
                            }
                        }, 8000);
                    }
                }

                @Override
                public void sppDisconnected(BluetoothDevice device) {
                    uiHandlerDeviceDisconnected();
                }

                @Override
                public void packetReceived(byte[] dataByte, String dataString, String dataHexString) {
                    uiHandlerReceiveData(dataByte, dataString, dataHexString);
                }

                @Override
                public void sendPacketProgress(BluetoothDevice device, int percentage, byte[] tempByte) {
                    String sendMessage = null;
                    if (tempByte != null) {
                        try {
                            sendMessage = new String(tempByte, "UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(percentage == FscSppApi.FIFO_SEND_FINISH){
                        /**
                         * the data in the cache is sent
                         */
//                        LogUtil.i(TAG,"fifo null");
                    }
                    /**
                     * after successful connection will send 20 bytes of encrypted information
                     */
                    if (sendMessage != null && tempByte.length == 20 && sendMessage.substring(0, 4).equals("AUTH")) {
                    } else {
                        uiHandlerSendDataProgress(percentage, tempByte);
                    }
                }
            });
        } else if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
            fscBleCentralApi = FscBleCentralApiImp.getInstance(activity);
            fscBleCentralApi.setCallbacks(new FscBleCentralCallbacksImp() {
                @Override
                public void characteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
                    currentWriteCharacteristic = characteristic;
                }

                @Override
                public void servicesFound(BluetoothGatt gatt, BluetoothDevice device, ArrayList<BluetoothGattService> services) {
                    serviceList = services;
                }

                @Override
                public void blePeripheralConnected(BluetoothGatt gatt, BluetoothDevice device) {
                    uiHandlerDeviceConnected();
                    if (AUTH_TEST) {
                        Log.i(TAG,"count "+Integer.valueOf(AUTH_TEST_COUNT++).toString());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "test disconnect");
                                disconnectDevice();
                                activity.finish();
                            }
                        }, 8000);
                    }
                }

                @Override
                public void blePeripheralDisonnected(BluetoothGatt gatt, BluetoothDevice device) {
                    uiHandlerDeviceDisconnected();
                }

                @Override
                public void packetReceived(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, String hexString, byte[] rawValue, String timestamp) {
                    uiHandlerReceiveData(rawValue, strValue, hexString);
                }

                @Override
                public void sendPacketProgress(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattCharacteristic ch, int percentage, byte[] tempByte) {
                    String sendMessage = null;
                    if (tempByte != null) {
                        try {
                            sendMessage = new String(tempByte, "UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(percentage == FscBleCentralApi.FIFO_SEND_FINISH){
                        /**
                         * the data in the cache is sent
                         */
//                        LogUtil.i(TAG,"fifo  null");
                    }
                    /**
                     * after successful connection will send 20 bytes of encrypted information
                     */

                    if (sendMessage != null && tempByte.length == 20 && sendMessage.substring(0, 4).equals("AUTH")) {

                    } else {
                        uiHandlerSendDataProgress(percentage, tempByte);
                    }
                }
            });
        }
    }
}
