package com.feasycom.feasyblue.Adapter;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.feasycom.bean.BleNamesResolver;
import com.feasycom.feasyblue.Bean.CharacteristicList;
import com.feasycom.feasyblue.R;
import com.feasycom.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/* display all services for particular device */
public class ServicesExpandableListAdapter extends BaseExpandableListAdapter {
    List<BluetoothGattService> bluetoothGattServiceList;
    List<CharacteristicList> characteristicListList = new ArrayList<CharacteristicList>();
    private LayoutInflater mInflater;

    public ServicesExpandableListAdapter(Activity activity, List<BluetoothGattService> bluetoothGattServiceList) {
        this.bluetoothGattServiceList = bluetoothGattServiceList;
        this.characteristicListList.clear();
        for (BluetoothGattService service : bluetoothGattServiceList) {
            CharacteristicList characteristicList = new CharacteristicList(service.getCharacteristics());
            characteristicListList.add(characteristicList);
        }
        mInflater = activity.getLayoutInflater();
    }

    @Override
    public int getGroupCount() {
        return bluetoothGattServiceList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return characteristicListList.get(groupPosition).getCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return bluetoothGattServiceList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return characteristicListList.get(groupPosition).getItem(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ServiceViewHolder serviceViewHolder;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.peripheral_list_services_item, null);
            serviceViewHolder = new ServiceViewHolder(convertView);
            convertView.setTag(serviceViewHolder);
        } else {
            serviceViewHolder = (ServiceViewHolder) convertView.getTag();
        }
        BluetoothGattService bluetoothGattService = bluetoothGattServiceList.get(groupPosition);
        String uuid = bluetoothGattService.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BleNamesResolver.resolveServiceName(uuid);
        if ("Unknown".equals(name)) {
            serviceViewHolder.serviceTitle.setText("UUID:" + bluetoothGattService.getUuid());
        } else {
            serviceViewHolder.serviceTitle.setText(name);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChViewHolder chViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.peripheral_details_characteristic_item, null);
            chViewHolder = new ChViewHolder(convertView);
            convertView.setTag(chViewHolder);
        } else {
            chViewHolder = (ChViewHolder) convertView.getTag();
        }
        BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristicListList.get(groupPosition).getItem(childPosition);
        String name = BleNamesResolver.resolveCharacteristicName(bluetoothGattCharacteristic.getUuid().toString());
        if ("Unknown".equals(name)) {
            chViewHolder.characteristicUUID.setText("0x" + bluetoothGattCharacteristic.getUuid().toString());
        } else {
            chViewHolder.characteristicUUID.setText(name);
        }
        int properties = bluetoothGattCharacteristic.getProperties();
        StringBuffer propertiesString = new StringBuffer();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            propertiesString.append(" WriteWithoutResponse");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            propertiesString.append(" Write");
        }

        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            propertiesString.append(" Notify");
        }

        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            propertiesString.append(" Indicate");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            propertiesString.append(" Read");
        }
        if (bluetoothGattCharacteristic.getService().getUuid().toString().substring(4, 8).toLowerCase().equals("180a")) {
            if ((name != null) && (name.length() > 0) && (name.contains("String"))) {
                try {
                    chViewHolder.characteristicProperties.setText(new String(bluetoothGattCharacteristic.getValue()));
                } catch (Exception e) {
                    e.printStackTrace();
                    chViewHolder.characteristicUUID.setVisibility(View.GONE);
                    chViewHolder.characteristicProperties.setVisibility(View.GONE);
                    chViewHolder.characteristicImgLL.setVisibility(View.GONE);
                }
            } else {
                byte[] temp = bluetoothGattCharacteristic.getValue();
                chViewHolder.characteristicProperties.setText("<" + FileUtil.bytesToHex(temp, temp.length) + ">");
            }
        } else {

            chViewHolder.characteristicProperties.setText("Properties: " + new String(propertiesString));
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    static class ServiceViewHolder {
        @BindView(R.id.serviceTitle)
        TextView serviceTitle;

        ServiceViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class ChViewHolder {
        @BindView(R.id.characteristicUUID)
        TextView characteristicUUID;
        @BindView(R.id.characteristicProperties)
        TextView characteristicProperties;
        @BindView(R.id.characteristicImgLL)
        LinearLayout characteristicImgLL;

        ChViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
