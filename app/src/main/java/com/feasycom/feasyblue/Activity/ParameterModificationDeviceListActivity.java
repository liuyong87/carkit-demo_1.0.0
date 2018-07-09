package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
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
import com.feasycom.feasyblue.Bean.BaseEvent;
import com.feasycom.feasyblue.R;
import com.feasycom.feasyblue.Util.SettingConfigUtil;
import com.feasycom.feasyblue.Widget.RefreshableView;
import com.feasycom.feasyblue.Widget.TitleParamDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemClick;


public class ParameterModificationDeviceListActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_title_msg)
    TextView headerTitleMsg;
    @BindView(R.id.headerLinerLayout)
    LinearLayout headerLinerLayout;
    @BindView(R.id.devicesList)
    ListView devicesList;


    private final int ENABLE_BT_REQUEST_ID = 2;
    @BindView(R.id.ble_check)
    CheckBox bleCheck;
    @BindView(R.id.spp_check)
    CheckBox sppCheck;
    @BindView(R.id.refreshableView)
    RefreshableView refreshableView;

    private final String TAG="DeviceList";
    private Activity activity;
    FscBleCentralApi fscBleCentralApi;
    FscSppApi fscSppApi;
    Queue<BluetoothDeviceWrapper> deviceQueue = new LinkedList<BluetoothDeviceWrapper>();
    private Set<String> commandSet;
    public SearchDeviceListAdapter adapter;
    public static final int REQUEST_MODIFY_PARAMETER = 1;
    private boolean BLE_MODE = false;
    private TitleParamDialog countDialog;
    private int successfulCount = 0;
    // add for test
    private boolean OPEN_TEST_MODE = false;
    private int testCount;

    private Timer timerUI;
    private TimerTask taskUI;
    private Handler handler = new Handler();
    public static void actionStart(Context context, Set<String> commandSet) {
        Intent intent = new Intent(context, ParameterModificationDeviceListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("commandSet", (Serializable) commandSet);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_parameter_modification_device_list;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(getString(R.string.properitiesDefining));
        headerTitleMsg.setText(getString(R.string.compeletCount) + ":" + successfulCount);
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
                        adapter.clearList();
                        adapter.notifyDataSetChanged();
                        startScan();
                        refreshableView.finishRefreshing();
                    }
                });
            }
        }, 0);
        if (BLE_MODE) {
            bleCheck.setChecked(true);
        } else {
            sppCheck.setChecked(true);
        }
    }

    @Override
    public void loadData() {
        activity = this;
        BLE_MODE = (boolean) SettingConfigUtil.getData(activity, "parameterModifyMode", false);
        successfulCount = (int) SettingConfigUtil.getData(activity, "modifySuccessfulCount", 0);
        countDialog = new TitleParamDialog(activity, InputType.TYPE_CLASS_NUMBER, getString(R.string.modifyCompleteCount));
        commandSet = (Set<String>) getIntent().getSerializableExtra("commandSet");
        adapter = new SearchDeviceListAdapter(activity, getLayoutInflater());
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fscBleCentralApi = FscBleCentralApiImp.getInstance(activity);
        fscBleCentralApi.initialize();
        fscSppApi = FscSppApiImp.getInstance(activity);
        fscSppApi.initialize();
        fscBleCentralApi.setCallbacks(new FscBleCentralCallbacksImp() {
            @Override
            public void blePeripheralFound(BluetoothDeviceWrapper device, int rssi, byte[] record) {
                if (OPEN_TEST_MODE) {
                    if ("test".equals(device.getName())) {
                        Log.i(TAG,"test "+Integer.valueOf(testCount++).toString());
                        fscBleCentralApi.stopScan();
                        BluetoothDeviceWrapper bluetoothDeviceWrapper = new BluetoothDeviceWrapper("DD:0D:30:00:15:C7");
                        ParameterModifyInformationActivity.actionStart(activity, bluetoothDeviceWrapper, commandSet, REQUEST_MODIFY_PARAMETER, BLE_MODE);
                    }

                } else {
                    if (BLE_MODE) {
                        if (deviceQueue.size() < 50) {
                            deviceQueue.offer(device);
                        }
                    }
                }
            }
        });
        fscSppApi.setCallbacks(new FscSppCallbacksImp() {
            @Override
            public void sppDeviceFound(BluetoothDeviceWrapper device, int rssi) {
                if (OPEN_TEST_MODE) {
                    if ("test".equals(device.getName())) {
                        Log.i(TAG,"test "+Integer.valueOf(testCount++).toString());
                        fscSppApi.stopScan();
                        BluetoothDeviceWrapper bluetoothDeviceWrapper = new BluetoothDeviceWrapper("DD:0D:30:00:15:C7");
                        ParameterModifyInformationActivity.actionStart(activity, bluetoothDeviceWrapper, commandSet, REQUEST_MODIFY_PARAMETER, BLE_MODE);
                    }
                } else {
                    if (!BLE_MODE) {
                        if (deviceQueue.size() < 100) {
                            deviceQueue.offer(device);
                        }
                    }
                }
            }
        });
        startScan();
        timerUI = new Timer();
        taskUI = new UITimerTask(new WeakReference<ParameterModificationDeviceListActivity>((ParameterModificationDeviceListActivity) activity));
        timerUI.schedule(taskUI, 100, 200);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            finish();
        }
        return true;
    }

    @OnCheckedChanged({R.id.spp_check, R.id.ble_check})
    public void modeCheck(CompoundButton v, boolean flag) {
        switch (v.getId()) {
            case R.id.ble_check:
                if (flag) {
                    BLE_MODE = true;
                    SettingConfigUtil.saveData(activity, "parameterModifyMode", BLE_MODE);
                }
                sppCheck.setChecked(!flag);
                break;
            case R.id.spp_check:
                if (flag) {
                    BLE_MODE = false;
                    SettingConfigUtil.saveData(activity, "parameterModifyMode", BLE_MODE);
                }
                bleCheck.setChecked(!flag);
                break;
        }
    }

    @OnClick(R.id.header_left_image)
    public void goBack() {
        finish();
    }

    @OnClick({R.id.header_title, R.id.header_title_msg})
    public void completeButton() {
        countDialog.show();
    }

    @OnItemClick(R.id.devicesList)
    public void deviceClick(int position) {
        fscBleCentralApi.stopScan();
        fscSppApi.stopScan();
        BluetoothDeviceWrapper bluetoothDeviceWrapper = (BluetoothDeviceWrapper) adapter.getItem(position);
        ParameterModifyInformationActivity.actionStart(activity, bluetoothDeviceWrapper, commandSet, REQUEST_MODIFY_PARAMETER, BLE_MODE);
    }

    @Subscribe
    public void onEventMainThread(BaseEvent event) {
        switch (event.getEventId()) {
            case BaseEvent.COMPLETE_COUNT_CHANGE:
                successfulCount = Integer.valueOf((String) event.getParam()).intValue();
                SettingConfigUtil.saveData(activity, "modifySuccessfulCount", successfulCount);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        headerTitleMsg.setText(getString(R.string.compeletCount) + ":" + successfulCount);
                    }
                });
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!BLE_MODE) {
            fscSppApi.stopScan();
        }
        if (taskUI != null) {
            taskUI.cancel();
            taskUI = null;
        }
        if (timerUI != null) {
            timerUI.cancel();
            timerUI = null;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        successfulCount = successfulCount + resultCode;
        SettingConfigUtil.saveData(activity, "modifySuccessfulCount", successfulCount);
        if (OPEN_TEST_MODE) {
            Log.i(TAG,"success "+Integer.valueOf(successfulCount).toString());
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                headerTitleMsg.setText(getString(R.string.compeletCount) + ":" + successfulCount);
            }
        });
    }

    public void startScan() {
        if (BLE_MODE) {
            if (fscBleCentralApi.isBtEnabled() == false) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            }
            fscSppApi.stopScan();
            fscBleCentralApi.startScan(10000);
        } else {
            fscBleCentralApi.stopScan();
            fscSppApi.startScan(10000);
        }
    }


    class UITimerTask extends TimerTask {
        private WeakReference<ParameterModificationDeviceListActivity> activityWeakReference;

        public UITimerTask(WeakReference<ParameterModificationDeviceListActivity> activityWeakReference) {
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
