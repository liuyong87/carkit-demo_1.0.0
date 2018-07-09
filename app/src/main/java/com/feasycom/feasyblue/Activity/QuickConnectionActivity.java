package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.feasycom.bean.QuickConnectionParam;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.controler.FscSppCallbacks;
import com.feasycom.controler.FscSppCallbacksImp;
import com.feasycom.feasyblue.R;
import com.feasycom.feasyblue.Util.SettingConfigUtil;
import com.feasycom.util.LogUtil;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;


public class QuickConnectionActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_right_text)
    TextView headerRightText;
    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.mac)
    EditText mac;
    @BindView(R.id.begin)
    Button begin;
    @BindView(R.id.log)
    EditText log;
    @BindView(R.id.tx)
    EditText tx;
    @BindView(R.id.rx)
    EditText rx;
    private Activity activity;
    private FscSppApi fscSppApi;
    private StringBuffer logBuffer;
    private long smartLinkbegin;
    private long smartLinkend;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, QuickConnectionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_quick_connection;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerTitle.setText(getString(R.string.quickConnection));
        headerLeftImage.setImageResource(R.drawable.goback);
    }

    @Override
    public void initView() {
        String nameTemp= (String) SettingConfigUtil.getData(activity,"smartLinkName","Feasycom");
        name.setText(nameTemp);
        String macTemp= (String) SettingConfigUtil.getData(activity,"smartLinkMac","00:00:00:00:00:00");
        mac.setText(macTemp);
        String txTemp=(String)SettingConfigUtil.getData(activity,"smartLinkTx","123");
        tx.setText(txTemp);
    }

    @Override
    public void loadData() {
        activity = this;
        fscSppApi = FscSppApiImp.getInstance(activity);
        fscSppApi.initialize();
        fscSppApi.setCallbacks(new FscSppCallbacksImp() {
            @Override
            public void sppConnected(BluetoothDevice device) {
                addState(getResources().getString(R.string.connected));
            }

            @Override
            public void sppDisconnected(BluetoothDevice device) {
                addState(getResources().getString(R.string.disconnected));
                smartLinkend = System.currentTimeMillis();
                addState("总共 " + (smartLinkend - smartLinkbegin) + " ms");
            }

            @Override
            public void packetReceived(byte[] dataByte, String dataString, String dataHexString) {
                addState(getResources().getString(R.string.receive) + "  " + dataString);
            }

            @Override
            public void sendPacketProgress(BluetoothDevice device, int percentage, byte[] sendByte) {
                addState(getResources().getString(R.string.send) + "  " + new String(sendByte));
                if (percentage == 100) {
                    fscSppApi.disconnect();
                }
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.i("action", intent.getAction().toString());
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        intentFilter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        intentFilter.addAction("android.bluetooth.adapter.action.BLE_ACL_CONNECTED");
        intentFilter.addAction("android.bluetooth.adapter.action.BLE_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.REQUEST_BLE_SCAN_ALWAYS_AVAILABLE");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @OnClick(R.id.header_left_image)
    public void goBack() {
        SettingActivity.actionStart(activity);
        finishActivity();
    }

    @OnClick(R.id.begin)
    public void go() {
        smartLinkbegin = System.currentTimeMillis();
//        LogUtil.i("quick", "go 1");
        if (logBuffer == null) {
            logBuffer = new StringBuffer();
        } else {
            logBuffer.delete(0, logBuffer.length());
            logBuffer.setLength(0);
        }
        addState(getString(R.string.begin));
        String data = this.tx.getText().toString();
        String mac = this.mac.getText().toString();
        String name = this.name.getText().toString();
        QuickConnectionParam quickConnectionParam = new QuickConnectionParam.Builder()
                .setmName(name)
                .setmMac(mac)
                .setmData(data)
                .setmActivity(activity).build();
//        LogUtil.i("quick", "go 2");
        try {
            fscSppApi.smartLink(quickConnectionParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnTextChanged(R.id.mac)
    public void mac(Editable a) {
        if (BluetoothAdapter.checkBluetoothAddress(a.toString())) {
            SettingConfigUtil.saveData(activity, "smartLinkMac", a.toString());
        }
    }

    @OnTextChanged(R.id.name)
    public void name(Editable a) {
        SettingConfigUtil.saveData(activity, "smartLinkName", a.toString());
    }

    @OnTextChanged(R.id.tx)
    public void tx(Editable a) {
        SettingConfigUtil.saveData(activity, "smartLinkTx", a.toString());
    }

    public void addState(String string) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int msecond = c.get(Calendar.MILLISECOND);
        String strTime = String.format("【%02d:%02d:%02d.%03d】", hour, minute, second, msecond);
        logBuffer.append(strTime + string + "\r\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.setText(logBuffer);
            }
        });
    }

}
