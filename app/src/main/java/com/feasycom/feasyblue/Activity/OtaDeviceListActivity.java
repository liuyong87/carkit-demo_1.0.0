package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.feasycom.bean.BluetoothDeviceWrapper;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.controler.FscSppCallbacksImp;
import com.feasycom.feasyblue.Adapter.SearchDeviceListAdapter;
import com.feasycom.feasyblue.Controler.ActivityCollector;
import com.feasycom.feasyblue.R;
import com.feasycom.feasyblue.Widget.RefreshableView;

import java.util.LinkedList;
import java.util.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

import static com.feasycom.feasyblue.Activity.OtaActivity.OPEN_TEST_MODE;

public class OtaDeviceListActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.devicesList)
    ListView devicesList;
    @BindView(R.id.refreshableView)
    RefreshableView refreshableView;
    @BindView(R.id.header_title_msg)
    TextView headerTitleMsg;
    @BindView(R.id.header_right_button)
    Button headerRightButton;

    private Activity activity;
    private FscSppApi fscSppApi;
    public static SearchDeviceListAdapter adapter;
    private BluetoothDeviceWrapper bluetoothDeviceWrapper;
    Queue<BluetoothDeviceWrapper> deviceQueue = new LinkedList<BluetoothDeviceWrapper>();
    private final int SCAN_TIME = 20000;

    public static void actionStart(Context context, int operation) {
        Intent intent = new Intent(context, OtaDeviceListActivity.class);
        ((Activity) context).startActivityForResult(intent, operation);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ota_device_list;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(getResources().getString(R.string.OTAUpgrade));
        headerTitleMsg.setText(getResources().getString(R.string.searching));
        headerRightButton.setVisibility(View.INVISIBLE);
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
                        fscSppApi.startScan(SCAN_TIME);
                        refreshableView.finishRefreshing();
                    }
                });
            }
        }, 0);
        fscSppApi.startScan(SCAN_TIME);
    }

    @Override
    public void loadData() {
        activity = this;
        adapter = new SearchDeviceListAdapter(activity, getLayoutInflater());
        fscSppApi = FscSppApiImp.getInstance(activity);
        fscSppApi.initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fscSppApi != null) {
            setCallBacks();
        }
    }

    @OnItemClick(R.id.devicesList)
    public void deviceClick(int position) {
        headerTitleMsg.setText(getResources().getString(R.string.connecting));
        fscSppApi.stopScan();
        bluetoothDeviceWrapper = (BluetoothDeviceWrapper) adapter.getItem(position);
        fscSppApi.connect(bluetoothDeviceWrapper.getAddress());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            fscSppApi.stopScan();
            setResult(RESULT_CANCELED);
            activity.finish();
        }
        return true;
    }

    @OnClick(R.id.header_left_image)
    public void goBack() {
        fscSppApi.stopScan();
        setResult(RESULT_CANCELED);
        activity.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fscSppApi.stopScan();
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

    private void setCallBacks() {
        fscSppApi.setCallbacks(new FscSppCallbacksImp() {

            @Override
            public void startScan() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        headerTitleMsg.setText(getResources().getString(R.string.searching));
                    }
                });
            }

            @Override
            public void stopScan() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        headerTitleMsg.setText(getResources().getString(R.string.searched));
                    }
                });
            }

            @Override
            public void sppConnected(BluetoothDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        headerTitleMsg.setText(getResources().getString(R.string.connected));
                    }
                });
                setResult(Activity.RESULT_OK);
                activity.finish();
            }

            @Override
            public void sppDisconnected(BluetoothDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        headerTitleMsg.setText(getResources().getString(R.string.disconnected));
                    }
                });
            }

            @Override
            public void sppDeviceFound(BluetoothDeviceWrapper device, int rssi) {
                if (OPEN_TEST_MODE) {
                    if ("test1".equals(device.getName())) {
                        fscSppApi.stopScan();
                        bluetoothDeviceWrapper = device;
                        fscSppApi.connect(bluetoothDeviceWrapper.getAddress());
                    }
                } else {
                    deviceQueue.offer(device);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             * queue take an element return true / false
                             */
                            adapter.addDevice(deviceQueue.poll());
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        });
    }
}
