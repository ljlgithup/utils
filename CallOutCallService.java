package com.example.mobile.safe.service;

import com.example.mobile.safe.db.dao.AddressDbDao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class ShowAddressService extends Service {

	private TelephonyManager tm;
	private MyPhoneStateListener listener;
	private AddressDbDao dao;
	private String TAG = "ShowAddressService";
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		dao = new AddressDbDao();
		//由于电话的状态只能监听到空闲，响铃，接通等状态。没有去电的状态。所以可以通过广播接收者，来接收去电的广播。
		OutCallReciver outCallReciver = new OutCallReciver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(outCallReciver, filter);
		//注册一个电话状态监听的服务
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyPhoneStateListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		Log.i(TAG, "开启了归属地服务");
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
		Log.i(TAG, "关闭归属地服务");
		super.onDestroy();
	}
	/**
	 * 创建一个电话状态监听的服务
	 */
	class MyPhoneStateListener extends PhoneStateListener{

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if(state==TelephonyManager.CALL_STATE_RINGING){//响铃时，去查询号码的归属地
				String location = dao.findLocation(incomingNumber);
				Toast.makeText(getApplicationContext(), location, 0).show();
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}
	/**
	 * 创建一个监听去电的广播接收者
	 * 去电话的权限
	 *  <uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
	 */
	class OutCallReciver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//获取去电的号码
			String number = getResultData();
			String location = dao.findLocation(number);
			Toast.makeText(getApplicationContext(), location, 0).show();
		}
	}
}
