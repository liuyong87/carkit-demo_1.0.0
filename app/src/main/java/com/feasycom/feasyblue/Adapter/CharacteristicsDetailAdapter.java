package com.feasycom.feasyblue.Adapter;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feasycom.bean.BleNamesResolver;
import com.feasycom.feasyblue.Activity.ThroughputActivity;
import com.feasycom.feasyblue.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class CharacteristicsDetailAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    List<String> propertieList = new ArrayList<String>();
    List<BluetoothGattDescriptor> descriptors;
    private boolean mNotificationEnabled = false;
    private boolean mIndicateEnabled = false;
    private boolean mWriteNoResponseEnabled = false;
    private boolean mWriteEnabled = false;
    private ViewHolder viewHolder;

    public String getProperties(int position) {
        return propertieList.get(position);
    }

    public CharacteristicsDetailAdapter(Activity parent, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        super();
        mInflater = parent.getLayoutInflater();
        this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
        int properties = bluetoothGattCharacteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            propertieList.add("Write Without Response");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            propertieList.add("Write");
        }

        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            propertieList.add("Notify");
        }

        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            propertieList.add("Indicate");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            propertieList.add("Read");
        }

        descriptors = bluetoothGattCharacteristic.getDescriptors();
        if (descriptors != null && descriptors.size() > 0) {
            for (BluetoothGattDescriptor descriptor : descriptors) {
                if (BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE.equals(descriptor.getValue())) {
                    mNotificationEnabled = true;
                    mIndicateEnabled = false;
                } else if (BluetoothGattDescriptor.ENABLE_INDICATION_VALUE.equals(descriptor.getValue())) {
                    mNotificationEnabled = false;
                    mIndicateEnabled = true;
                }
            }
        }
        if ((bluetoothGattCharacteristic != null) && bluetoothGattCharacteristic.equals(ThroughputActivity.currentWriteCharacteristic)) {
            if (ThroughputActivity.currentWriteCharacteristic.getWriteType() == BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) {
                mWriteEnabled = true;
                mWriteNoResponseEnabled = false;
            } else if (ThroughputActivity.currentWriteCharacteristic.getWriteType() == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) {
                mWriteEnabled = false;
                mWriteNoResponseEnabled = true;
            }
        }
    }

    @Override
    public int getCount() {
        return propertieList.size();
    }

    @Override
    public Object getItem(int position) {
        return propertieList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get already available view or create new if necessary

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.property_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (propertieList.get(position).equals("Read")) {
            viewHolder.check.setVisibility(View.GONE);
        }
        viewHolder.property.setText(propertieList.get(position));


        if (propertieList.get(position).equals("Notify")) {
            if (mNotificationEnabled) {
                viewHolder.check.setChecked(true);
            } else {
                viewHolder.check.setChecked(false);
            }
        }


        if (propertieList.get(position).equals("Indicate")) {
            if (mIndicateEnabled) {
                viewHolder.check.setChecked(true);
            } else {
                viewHolder.check.setChecked(false);
            }
        }

        if (propertieList.get(position).equals("Write")) {
            if (mWriteEnabled) {
                viewHolder.check.setChecked(true);
            }else{
                viewHolder.check.setChecked(false);
            }
        }

        if (propertieList.get(position).equals("Write Without Response")) {
            if (mWriteNoResponseEnabled) {
                viewHolder.check.setChecked(true);
            }else{
                viewHolder.check.setChecked(false);
            }
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.property)
        TextView property;
        @BindView(R.id.check)
        CheckBox check;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public boolean ismNotificationEnabled() {
        return mNotificationEnabled;
    }

    public void setmNotificationEnabled(boolean mNotificationEnabled) {
        this.mNotificationEnabled = mNotificationEnabled;
    }

    public boolean ismIndicateEnabled() {
        return mIndicateEnabled;
    }

    public void setmIndicateEnabled(boolean mIndicateEnabled) {
        this.mIndicateEnabled = mIndicateEnabled;
    }

    public boolean ismWriteNoResponseEnabled() {
        return mWriteNoResponseEnabled;
    }

    public void setmWriteNoResponseEnabled(boolean mWriteNoResponseEnabled) {
        this.mWriteNoResponseEnabled = mWriteNoResponseEnabled;
    }

    public boolean ismWriteEnabled() {
        return mWriteEnabled;
    }

    public void setmWriteEnabled(boolean mWriteEnabled) {
        this.mWriteEnabled = mWriteEnabled;
    }
}
