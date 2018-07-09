package com.feasycom.feasyblue.Activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.feasycom.bean.BluetoothDeviceWrapper;
import com.feasycom.controler.FscBleCentralApi;
import com.feasycom.controler.FscBleCentralApiImp;
import com.feasycom.controler.FscBleCentralCallbacksImp;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.controler.FscSppCallbacksImp;
import com.feasycom.feasyblue.Adapter.SearchDeviceListAdapter;
import com.feasycom.feasyblue.R;
import com.feasycom.feasyblue.Widget.RefreshableView;
import com.feasycom.util.LogUtil;
import com.feasycom.util.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnCheckedChanged;

import butterknife.OnClick;
import butterknife.OnItemClick;

public class SearchDeviceActivity extends BaseActivity {
    private final int ENABLE_BT_REQUEST_ID = 2;
    public static final boolean AUTH_TEST = false;

    FscSppApi fscSppApi;
    FscBleCentralApi fscBleCentralApi;
    Activity activity;
    Queue<BluetoothDeviceWrapper> deviceQueue = new LinkedList<BluetoothDeviceWrapper>();
    @BindView(R.id.header_left)
    TextView headerLeft;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_title_msg)
    TextView headerTitleMsg;
    @BindView(R.id.header_right_TV)
    TextView headerRightTV;
    @BindView(R.id.header_right_LL)
    LinearLayout headerRightLL;
    @BindView(R.id.headerLinerLayout)
    LinearLayout headerLinerLayout;
    @BindView(R.id.devicesList)
    ListView devicesList;
    @BindView(R.id.refreshableView)
    RefreshableView refreshableView;
    @BindView(R.id.communication_button)
    ImageView communicationButton;
    @BindView(R.id.setting_button)
    ImageView settingButton;
    @BindView(R.id.about_button)
    ImageView aboutButton;
    @BindView(R.id.communication_button_text)
    TextView communicationButtonText;
    @BindView(R.id.setting_button_text)
    TextView settingButtonText;
    @BindView(R.id.about_button_text)
    TextView aboutButtonText;
    @BindView(R.id.ble_check)
    CheckBox bleCheck;
    @BindView(R.id.spp_check)
    CheckBox sppCheck;
    @BindView(R.id.header_sort_button)
    Button headerSortButton;
    @BindView(R.id.header_filter_button)
    Button headerFilterButton;

    public static String currentMode;
    public SearchDeviceListAdapter adapter;
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private final int REQUEST_LOCATION = 3;
    private final int REQUEST_FILE = 4;
    private Timer timerUI;
    private TimerTask taskUI;
    private boolean checkChange = false;
    private boolean testDeviceFound = false;
    private Handler handler=new Handler();


    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SearchDeviceActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search_device;
    }


    @Override
    public void refreshFooter() {
        communicationButton.setImageResource(R.drawable.communication_on);
        settingButton.setImageResource(R.drawable.setting_off);
        aboutButton.setImageResource(R.drawable.about_off);

        communicationButtonText.setTextColor(getResources().getColor(R.color.footer_on_text_color));
        settingButtonText.setTextColor(getResources().getColor(R.color.color_tb_text));
        aboutButtonText.setTextColor(getResources().getColor(R.color.color_tb_text));
    }

    @Override
    public void refreshHeader() {
        headerTitle.setText(getResources().getString(R.string.communication));

        adapter.notifyDataSetChanged();
    }

    @Override
    public void initView() {
        devicesList.setAdapter(adapter);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceQueue.clear();
                        /**
                         * optimize memory, device information cache
                         */
//                        if (checkChange) {
//                            checkChange=false;
//                        } else {
//                            deviceQueue.addAll(adapter.getmDevices());
//                        }
                        adapter.clearList();
                        adapter.notifyDataSetChanged();
                        startScan();
                        refreshableView.finishRefreshing();
                    }
                });
            }
        }, 0);
    }

    @Override
    public void loadData() {
        activity = this;
        adapter = new SearchDeviceListAdapter(activity, getLayoutInflater());

    }


    @Override
    protected void onResume() {
        super.onResume();
        testDeviceFound = false;
        /**
         * enable or disable debug information
         */
        LogUtil.setDebug(true);
        /**
         * switch the context and then use the fscBleCentralApi or fscSppApi must be updated to the current context
         */
        fscBleCentralApi = FscBleCentralApiImp.getInstance(activity);
        fscBleCentralApi.initialize();

        fscSppApi = FscSppApiImp.getInstance(activity);
        fscSppApi.initialize();
        if ((fscSppApi != null) && (fscBleCentralApi != null)) {
            setCallBacks();
        }
        /**
         * bluetooth search needs positioning permissions  for android 6.0 or above
         */
        int permissionLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionFile = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    REQUEST_LOCATION
            );
        } else if (permissionFile != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_FILE
            );
        }
        startScan();
        timerUI = new Timer();
        taskUI = new UITimerTask(new WeakReference<SearchDeviceActivity>((SearchDeviceActivity) activity));
        timerUI.schedule(taskUI, 100, 250);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != timerUI) {
            timerUI.cancel();
            timerUI = null;
        }
        if (null != taskUI) {
            taskUI.cancel();
            taskUI = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * fscSppApi.stopScan()  will  release the resources of broadcastReceiver
         */
        fscSppApi.stopScan();
//        fscBleCentralApi.stopScan();
        if (null != timerUI) {
            timerUI.cancel();
            timerUI = null;
        }
        if (null != taskUI) {
            taskUI.cancel();
            taskUI = null;
        }
    }

    @OnClick(R.id.header_sort_button)
    public void deviceSort(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.sort();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.header_filter_button)
    public void deviceFilterClick() {
        filterDeviceActivity.actionStart(activity);
    }

    @OnCheckedChanged({R.id.spp_check, R.id.ble_check})
    public void modeCheck(CompoundButton v, boolean flag) {
        checkChange = true;
        switch (v.getId()) {
            case R.id.ble_check:
                if (flag) {
                    currentMode = BluetoothDeviceWrapper.BLE_MODE;
                }
                sppCheck.setChecked(!flag);
                break;
            case R.id.spp_check:
                if (flag) {
                    currentMode = BluetoothDeviceWrapper.SPP_MODE;
                }
                bleCheck.setChecked(!flag);
                break;
        }
    }


    @OnItemClick(R.id.devicesList)
    public void deviceClick(int position) {
        stopAllScan();
        BluetoothDeviceWrapper bluetoothDeviceWrapper = (BluetoothDeviceWrapper) adapter.getItem(position);
        ThroughputActivity.actionStart(activity, bluetoothDeviceWrapper);
    }

    public void uiHandlerStartScan() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                headerTitleMsg.setText(getResources().getString(R.string.searching));
            }
        });
    }

    public void uiHandlerStopScan() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                adapter.sort();
//                adapter.notifyDataSetChanged();
                headerTitleMsg.setText(getResources().getString(R.string.searched));
            }
        });
    }

    public void startScan() {
        if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
            bleCheck.setChecked(true);
            if (fscBleCentralApi.isBtEnabled() == false) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            }
            if (!fscBleCentralApi.checkBleHardwareAvailable()) {
                ToastUtil.show(activity, "is not support ble");
            }
            fscSppApi.stopScan();
            fscBleCentralApi.startScan(8000);
        } else {
            sppCheck.setChecked(true);
            if (fscSppApi.isBtEnabled() == false) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            }
            fscBleCentralApi.stopScan();
            fscSppApi.startScan(10000);
        }
    }

    public void stopAllScan() {
        fscBleCentralApi.stopScan();
        fscSppApi.stopScan();
    }

    private void setCallBacks() {
        fscBleCentralApi.setCallbacks(new FscBleCentralCallbacksImp() {
            @Override
            public void startScan() {
                if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
                    uiHandlerStartScan();
                }
            }

            @Override
            public void stopScan() {
                if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
                    uiHandlerStopScan();
                }
            }

            @Override
            public void blePeripheralFound(BluetoothDeviceWrapper device, int rssi, byte[] record) {
                if (AUTH_TEST) {
                    if ("test".equals(device.getName())) {
                        stopAllScan();
                        if(!testDeviceFound){
                            testDeviceFound=true;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ThroughputActivity.actionStart(activity, device);
                                }
                            },2000);
                        }
                    }
                } else {
                    /**
                     * queue to add a device return true / false
                     * note that the life cycle of the device object, here with a queue cache
                     */
                    if (BluetoothDeviceWrapper.BLE_MODE.equals(currentMode)) {
                        if (deviceQueue.size() < 10) {
                            deviceQueue.offer(device);
                        }
                    }
                }
            }
        });

        fscSppApi.setCallbacks(new FscSppCallbacksImp() {
            @Override
            public void sppDeviceFound(BluetoothDeviceWrapper device, int rssi) {
                if (AUTH_TEST) {
                    if ("test".equals(device.getName())) {
                        stopAllScan();
                        ThroughputActivity.actionStart(activity, device);
                    }
                } else {
                    /**
                     * queue to add a device return true / false
                     * note that the life cycle of the device object, here with a queue cache
                     * fix bug :there will be a delay broadcast, the probability of BLE_MODE list will cause the case of SPP equipment
                     */
                    if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
                        if (deviceQueue.size() < 10) {
                            deviceQueue.offer(device);
                        }
                    }
                }
            }

            public void startScan() {
                if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
                    uiHandlerStartScan();
                }
            }


            public void stopScan() {
                if (BluetoothDeviceWrapper.SPP_MODE.equals(currentMode)) {
                    uiHandlerStopScan();
                }
            }
        });
    }

    class UITimerTask extends TimerTask {
        private WeakReference<SearchDeviceActivity> activityWeakReference;

        public UITimerTask(WeakReference<SearchDeviceActivity> activityWeakReference) {
            this.activityWeakReference = activityWeakReference;
        }

        @Override
        public void run() {
            activityWeakReference.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activityWeakReference.get().getAdapter().addDevice(activityWeakReference.get().getDeviceQueue().poll());
                    activityWeakReference.get().getAdapter().notifyDataSetChanged();
                }
            });
        }
    }

    public SearchDeviceListAdapter getAdapter() {
        return adapter;
    }

    public Queue<BluetoothDeviceWrapper> getDeviceQueue() {
        return deviceQueue;
    }
}
