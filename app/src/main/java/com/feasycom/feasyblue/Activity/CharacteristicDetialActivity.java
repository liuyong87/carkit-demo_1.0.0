package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.feasycom.controler.FscBleCentralApi;
import com.feasycom.controler.FscBleCentralApiImp;
import com.feasycom.controler.FscBleCentralCallbacksImp;
import com.feasycom.feasyblue.Adapter.CharacteristicsDetailAdapter;
import com.feasycom.feasyblue.Controler.ActivityCollector;
import com.feasycom.feasyblue.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

import static com.feasycom.feasyblue.Activity.ThroughputActivity.percentageStack;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.receiveBuffer;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.receiveBufferHex;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.receiveByte;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.receiveCRC;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.sendByte;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.sendCRC;
import static com.feasycom.feasyblue.Activity.ThroughputActivity.sendFileButton;

public class CharacteristicDetialActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_right_text)
    TextView headerRightText;
    @BindView(R.id.uuid)
    TextView uuid;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.readResponse)
    EditText readResponse;

    private final String TAG="ChDetialActivity";
    private Activity activity;
    private CharacteristicsDetailAdapter characteristicsDetailAdapter;
    private FscBleCentralApi fscBleCentralApi;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, CharacteristicDetialActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_characteristic_detial;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(getString(R.string.characteristicInfo));
    }

    @Override
    public void initView() {
        uuid.setText("UUID: " + ServiceSelectActivity.bluetoothGattCharacteristic.getUuid().toString());
        listView.setAdapter(characteristicsDetailAdapter);
        if ((ServiceSelectActivity.bluetoothGattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            readResponse.setVisibility(View.VISIBLE);
        } else {
            readResponse.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadData() {
        activity = this;
        fscBleCentralApi = FscBleCentralApiImp.getInstance(activity);
        characteristicsDetailAdapter = new CharacteristicsDetailAdapter(activity, ServiceSelectActivity.bluetoothGattCharacteristic);
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

    @OnItemClick(R.id.listView)
    public void itemClick(AdapterView<?> parent, int position) {
        String propertiesString = characteristicsDetailAdapter.getProperties(position);
        int properties = ServiceSelectActivity.bluetoothGattCharacteristic.getProperties();
        if ("Notify".equals(propertiesString)) {
            if (characteristicsDetailAdapter.ismNotificationEnabled()) {
                Log.i(TAG, "notify dis");
                fscBleCentralApi.setCharacteristic(ServiceSelectActivity.bluetoothGattCharacteristic, FscBleCentralApi.DISABLE_CHARACTERISTIC_NOTIFICATION);
                characteristicsDetailAdapter.setmNotificationEnabled(false);
            } else {
                Log.i(TAG, "notify en");
                fscBleCentralApi.setCharacteristic(ServiceSelectActivity.bluetoothGattCharacteristic, FscBleCentralApi.ENABLE_CHARACTERISTIC_NOTIFICATION);
                characteristicsDetailAdapter.setmNotificationEnabled(true);
                characteristicsDetailAdapter.setmIndicateEnabled(false);
            }
        } else if ("Indicate".equals(propertiesString)) {
            if (characteristicsDetailAdapter.ismIndicateEnabled()) {
                fscBleCentralApi.setCharacteristic(ServiceSelectActivity.bluetoothGattCharacteristic, FscBleCentralApi.DISABLE_CHARACTERISTIC_INDICATE);
                characteristicsDetailAdapter.setmIndicateEnabled(false);
            } else {
                fscBleCentralApi.setCharacteristic(ServiceSelectActivity.bluetoothGattCharacteristic, FscBleCentralApi.ENABLE_CHARACTERISTIC_INDICATE);
                characteristicsDetailAdapter.setmIndicateEnabled(true);
                characteristicsDetailAdapter.setmNotificationEnabled(false);
            }
        } else if ("Write".equals(propertiesString)) {
            if (((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) && (characteristicsDetailAdapter.ismWriteEnabled())) {
                return;
            }
            if (characteristicsDetailAdapter.ismWriteEnabled()) {
                fscBleCentralApi.setCharacteristic(ServiceSelectActivity.bluetoothGattCharacteristic, FscBleCentralApi.CHARACTERISTIC_WRITE_NO_RESPONSE);
                characteristicsDetailAdapter.setmWriteEnabled(false);
                characteristicsDetailAdapter.setmWriteNoResponseEnabled(true);
            } else {
                fscBleCentralApi.setCharacteristic(ServiceSelectActivity.bluetoothGattCharacteristic, FscBleCentralApi.CHARACTERISTIC_WRITE);
                characteristicsDetailAdapter.setmWriteEnabled(true);
                characteristicsDetailAdapter.setmWriteNoResponseEnabled(false);
            }
            ThroughputActivity.currentWriteCharacteristic = ServiceSelectActivity.bluetoothGattCharacteristic;
        } else if ("Write Without Response".equals(propertiesString)) {
            if (((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) && (characteristicsDetailAdapter.ismWriteNoResponseEnabled())) {
                return;
            }
            if (characteristicsDetailAdapter.ismWriteNoResponseEnabled()) {
                fscBleCentralApi.setCharacteristic(ServiceSelectActivity.bluetoothGattCharacteristic, FscBleCentralApi.CHARACTERISTIC_WRITE);
                characteristicsDetailAdapter.setmWriteNoResponseEnabled(false);
                characteristicsDetailAdapter.setmWriteEnabled(true);
            } else {
                fscBleCentralApi.setCharacteristic(ServiceSelectActivity.bluetoothGattCharacteristic, FscBleCentralApi.CHARACTERISTIC_WRITE_NO_RESPONSE);
                characteristicsDetailAdapter.setmWriteNoResponseEnabled(true);
                characteristicsDetailAdapter.setmWriteEnabled(false);
            }
            ThroughputActivity.currentWriteCharacteristic = ServiceSelectActivity.bluetoothGattCharacteristic;
        } else if ("Read".equals(propertiesString)) {
            fscBleCentralApi.read(ServiceSelectActivity.bluetoothGattCharacteristic);
        }
        characteristicsDetailAdapter.notifyDataSetChanged();
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

            @Override
            public void readResponse(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, final String hexString, byte[] rawValue, String timestamp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readResponse.setText(hexString);
                    }
                });
            }
        });
    }

}
