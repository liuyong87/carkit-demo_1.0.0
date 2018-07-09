package com.feasycom.feasyblue.Adapter;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.feasycom.bean.BleNamesResolver;
import com.feasycom.feasyblue.R;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CharacteristicsListAdapter extends BaseAdapter {
    private ArrayList<BluetoothGattCharacteristic> mCharacteristics;
    private LayoutInflater mInflater;

    public CharacteristicsListAdapter(Activity parent) {
        super();
        mCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
        mInflater = parent.getLayoutInflater();
    }

    public void addCharacteristic(BluetoothGattCharacteristic ch) {
        if (mCharacteristics.contains(ch) == false) {
            mCharacteristics.add(ch);
        }
    }

    public BluetoothGattCharacteristic getCharacteristic(int index) {
        return mCharacteristics.get(index);
    }

    public void clearList() {
        mCharacteristics.clear();
    }

    @Override
    public int getCount() {
        return mCharacteristics.size();
    }

    @Override
    public Object getItem(int position) {
        return getCharacteristic(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get already available view or create new if necessary
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.peripheral_list_characteristic_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // set proper values into the view
        BluetoothGattCharacteristic ch = getCharacteristic(position);
        String uuid = ch.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BleNamesResolver.resolveCharacteristicName(uuid);

        viewHolder.peripheralListCharacteristicName.setText(name);
        viewHolder.peripheralListCharacteristicUuid.setText(uuid);

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.peripheral_list_characteristic_name)
        TextView peripheralListCharacteristicName;
        @BindView(R.id.peripheral_list_characteristic_uuid)
        TextView peripheralListCharacteristicUuid;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
