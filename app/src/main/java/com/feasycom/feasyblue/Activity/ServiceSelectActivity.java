package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feasycom.bean.BluetoothDeviceWrapper;
import com.feasycom.controler.FscBleCentralApi;
import com.feasycom.controler.FscBleCentralApiImp;
import com.feasycom.controler.FscBleCentralCallbacksImp;
import com.feasycom.feasyblue.Adapter.ServicesExpandableListAdapter;
import com.feasycom.feasyblue.Controler.ActivityCollector;
import com.feasycom.feasyblue.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import static com.feasycom.feasyblue.Activity.ThroughputActivity.percentageStack;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.receiveBuffer;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.receiveBufferHex;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.receiveByte;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.receiveCRC;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.sendByte;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.sendCRC;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.sendFileButton;

public class ServiceSelectActivity extends BaseActivity {
    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_right_text)
    TextView headerRightText;
    @BindView(R.id.headerLinerLayout)
    LinearLayout headerLinerLayout;
    @BindView(R.id.expandableListView)
    ExpandableListView expandableListView;

    private TextView flag;
    private LinearLayout flagLL;
    private TextView incompleteServiceUUID16Bit;
    private LinearLayout incompleteServiceUUID16BitLL;
    private TextView incompleteServiceUUID128Bit;
    private LinearLayout incompleteServiceUUID128BitLL;
    private TextView completeLocalName;
    private LinearLayout completeLocalNameLL;
    private TextView serviceData;
    private LinearLayout serviceDataLL;
    private TextView txPowerLevel;
    private LinearLayout txPowerLevelLL;
    private TextView manufacturerSpecificData;
    private LinearLayout manufacturerSpecificDataLL;
    private TextView advData;
    private View listViewHeader;
    private ServicesExpandableListAdapter servicesExpandableListAdapter = null;

    private FscBleCentralApi fscBleCentralApi;
    private Activity activity;
    private ArrayList<BluetoothGattService> serviceList = ThroughputActivity.serviceList;
    private BluetoothDeviceWrapper deviceWrapper;
    public static BluetoothGattCharacteristic bluetoothGattCharacteristic=null;
    public static void actionStart(Context context, BluetoothDeviceWrapper bluetoothDeviceWrapper) {
        Intent intent = new Intent(context, ServiceSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("device", bluetoothDeviceWrapper);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_service_select;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(getString(R.string.transmissionConfiguration));
    }

    @Override
    public void initView() {
        listViewHeader = getLayoutInflater().inflate(R.layout.peripheral_list_services_header, null);
        flag = (TextView) listViewHeader.findViewById(R.id.advFlag);
        incompleteServiceUUID16Bit = (TextView) listViewHeader.findViewById(R.id.incompleteServiceUUIDs_16bit);
        incompleteServiceUUID16BitLL = (LinearLayout) listViewHeader.findViewById(R.id.incompleteServiceUUIDs_16bit_LL);
        incompleteServiceUUID128Bit = (TextView) listViewHeader.findViewById(R.id.incompleteServiceUUIDs_128bit);
        incompleteServiceUUID128BitLL = (LinearLayout) listViewHeader.findViewById(R.id.incompleteServiceUUIDs_128bit_LL);
        completeLocalName = (TextView) listViewHeader.findViewById(R.id.completeLocalName);
        completeLocalNameLL = (LinearLayout) listViewHeader.findViewById(R.id.completeLocalNameLL);
        serviceData = (TextView) listViewHeader.findViewById(R.id.serviceData);
        serviceDataLL = (LinearLayout) listViewHeader.findViewById(R.id.serviceDataLL);
        txPowerLevel = (TextView) listViewHeader.findViewById(R.id.txPowerLevel);
        txPowerLevelLL = (LinearLayout) listViewHeader.findViewById(R.id.txPowerLevelLL);
        manufacturerSpecificData = (TextView) listViewHeader.findViewById(R.id.manufacturerSpecificData);
        manufacturerSpecificDataLL = (LinearLayout) listViewHeader.findViewById(R.id.manufacturerSpecificDataLL);

        listViewHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        advData = (TextView) listViewHeader.findViewById(R.id.advData);
        advData.setText(deviceWrapper.getAdvData());
        expandableListView.addHeaderView(listViewHeader);
        expandableListView.setAdapter(servicesExpandableListAdapter);
        for(int i=0;i<servicesExpandableListAdapter.getGroupCount();i++){
            expandableListView.expandGroup(i);
        }
        /**
         * forced open
         */
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                bluetoothGattCharacteristic=serviceList.get(groupPosition).getCharacteristics().get(childPosition);
                CharacteristicDetialActivity.actionStart(activity);
                return false;
            }
        });
        servicesExpandableListAdapter.notifyDataSetChanged();
        if (deviceWrapper.getFlag() == null) {
            flagLL.setVisibility(View.GONE);
        }
        flag.setText(deviceWrapper.getFlag());
        if (deviceWrapper.getIncompleteServiceUUIDs_16bit() == null) {
            incompleteServiceUUID16BitLL.setVisibility(View.GONE);
        }
        incompleteServiceUUID16Bit.setText(deviceWrapper.getIncompleteServiceUUIDs_16bit());
        if (deviceWrapper.getIncompleteServiceUUIDs_128bit() == null) {
            incompleteServiceUUID128BitLL.setVisibility(View.GONE);
        }
        incompleteServiceUUID128Bit.setText(deviceWrapper.getIncompleteServiceUUIDs_128bit());
        if (deviceWrapper.getCompleteLocalName() == null) {
            completeLocalNameLL.setVisibility(View.GONE);
        }
        completeLocalName.setText(deviceWrapper.getCompleteLocalName());
        if (deviceWrapper.getServiceData() == null) {
            serviceDataLL.setVisibility(View.GONE);
        }
        serviceData.setText(deviceWrapper.getServiceData());
        if (deviceWrapper.getTxPowerLevel() == null) {
            txPowerLevelLL.setVisibility(View.GONE);
        }
        txPowerLevel.setText(deviceWrapper.getTxPowerLevel());
        if (deviceWrapper.getManufacturerSpecificData() == null) {
            manufacturerSpecificDataLL.setVisibility(View.GONE);
        }
        manufacturerSpecificData.setText(deviceWrapper.getManufacturerSpecificData());
    }

    @Override
    public void loadData() {
        activity = this;
        bluetoothGattCharacteristic=null;
        //serviceList = getIntent().getParcelableArrayListExtra("serviceList");
        deviceWrapper = (BluetoothDeviceWrapper) getIntent().getSerializableExtra("device");
        fscBleCentralApi = FscBleCentralApiImp.getInstance();

        if(servicesExpandableListAdapter == null)
            servicesExpandableListAdapter = new ServicesExpandableListAdapter(this,serviceList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fscBleCentralApi.setCallbacks(new FscBleCentralCallbacksImp() {
            @Override
            public void sendPacketProgress(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattCharacteristic ch, int percentage, byte[] tempByte) {
                if (!percentageStack.contains(percentage)) {
                    percentageStack.push(percentage);
                }
                if (percentage == FscBleCentralApi.PACKGE_SEND_FINISH) {
                    ThroughputActivity.handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (sendFileButton.getText().equals(activity.getResources().getString(R.string.stop))) {
                                sendFileButton.setText(activity.getResources().getString(R.string.sendFile));
                            }
                            percentageStack.clear();
                        }
                    }, 1500);
                }
                if (null != tempByte) {
                    ThroughputActivity.sendPackge++;
                    sendByte = sendByte + tempByte.length;
                    sendCRC.update(tempByte);
                } else {
                    percentageStack.clear();
                }
            }

            @Override
            public void packetReceived(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, String hexString, byte[] rawValue, String timestamp) {
                ThroughputActivity.receivePackge++;
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
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            activity.finish();
        }
        return true;
    }
    @OnClick(R.id.header_left_image)
    public void goBack() {
        activity.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
