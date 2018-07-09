package com.feasycom.feasyblue.Adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.feasycom.bean.BluetoothDeviceWrapper;
import com.feasycom.feasyblue.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.feasycom.feasyblue.Activity.filterDeviceActivity.filterValue;
import static com.feasycom.feasyblue.Activity.filterDeviceActivity.isFilterEnable;

public class SearchDeviceListAdapter extends BaseAdapter {
    private LayoutInflater mInflator;
    private Context mContext;
    private ArrayList<BluetoothDeviceWrapper> mDevices = new ArrayList<BluetoothDeviceWrapper>();

    public SearchDeviceListAdapter(Context context, LayoutInflater Inflator) {
        super();
        mContext = context;
        mInflator = Inflator;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        BluetoothDeviceWrapper bluetoothDeviceWrapper;
        bluetoothDeviceWrapper = mDevices.get(position);
        return bluetoothDeviceWrapper;
    }


    public void addDevice(BluetoothDeviceWrapper deviceDetail) {
        if (null == deviceDetail) return;
        int i = 0;
        for (; i < mDevices.size(); i++) {
            if (deviceDetail.getAddress().equals(mDevices.get(i).getAddress())) {

                mDevices.get(i).setName(deviceDetail.getName());
                mDevices.get(i).setRssi(deviceDetail.getRssi());
                mDevices.get(i).setAdvData(deviceDetail.getAdvData());

                break;
            }
        }
        if (i >= mDevices.size()) {
            mDevices.add(deviceDetail);
        }

        //process the filter event.
        if(isFilterEnable == 1) {
            if (mDevices.get(i).getRssi() < filterValue) {
                mDevices.remove(i);
            }
        }
    }

    public void sort() {
        for (int i=0; i < mDevices.size() - 1; i++) {
            for (int j = 0; j < mDevices.size() - 1 - i; j++) {
                if (mDevices.get(j).getRssi() < mDevices.get(j + 1).getRssi()) {
                    BluetoothDeviceWrapper bd = mDevices.get(j);
                    mDevices.set(j, mDevices.get(j + 1));
                    mDevices.set(j + 1, bd);
                }
            }
        }
    }

    public void clearList() {
        mDevices.clear();
    }

    public ArrayList<BluetoothDeviceWrapper> getmDevices() {
        return mDevices;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.search_device_info, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDeviceWrapper deviceDetail = mDevices.get(position);
        String deviceName = deviceDetail.getName();
        String deviceAdd = deviceDetail.getAddress();
        int deviceRssi = deviceDetail.getRssi().intValue();
        if (deviceName != null && deviceName.length() > 0) {
            //设备名长度限制，最大30
            if (deviceName.length() >= 30) {
                deviceName = deviceName.substring(0, 30);
            }
            if (deviceDetail.getBondState() == BluetoothDevice.BOND_BONDED) {
                viewHolder.tvName.setText(mContext.getResources().getString(R.string.paired) + deviceName);
            } else {
                viewHolder.tvName.setText(deviceName);
            }
        } else {
            viewHolder.tvName.setText("unknow");
        }
        if (deviceAdd != null && deviceAdd.length() > 0) {
            viewHolder.tvAddr.setText(" (" + deviceAdd + ")");
        } else {
            viewHolder.tvAddr.setText(" (unknow)");
        }


        if (deviceRssi <= -100) {
            deviceRssi = -100;
        } else if (deviceRssi > 0) {
            deviceRssi = 0;
        }
        String str_rssi = "(" + deviceRssi + ")";
        if (str_rssi.equals("(-100)")) {
            str_rssi = "null";
        }
        viewHolder.deviceMode.setText(deviceDetail.getModel());
        viewHolder.pbRssi.setProgress(100 + deviceRssi);
        viewHolder.tvRssi.setText(mContext.getResources().getString(R.string.rssi) + "(" + deviceDetail.getRssi().toString() + ")");
        return view;
    }


    static class ViewHolder {

        @BindView(R.id.tv_rssi)
        TextView tvRssi;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_addr)
        TextView tvAddr;
        @BindView(R.id.device_mode)
        TextView deviceMode;
        @BindView(R.id.pb_rssi)
        ProgressBar pbRssi;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
