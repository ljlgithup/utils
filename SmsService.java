package com.example.mobile.service;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.example.mobile.safe.db.dao.BlackNumberDao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallSmsSafeService extends Service {

	private TelephonyManager tm;
	private MyPhoneStateListener listener;
	private BlackNumberDao dao;
	private String TAG = "CallSmsSafeService";
	private InnerSmsReciver receiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	/**
	 * 内部类短信接收者
	 */
	class InnerSmsReciver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "服务内部广播接收者接收到了短信");
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for(Object obj:objs){
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[])obj);
				//获取联系人
				String address = smsMessage.getOriginatingAddress();
				//获取短信内容
				String body = smsMessage.getMessageBody();
				String mode = dao.find(address);
				if("2".equals(mode)||"3".equals(mode)){
					Log.i(TAG, "发现骚扰短信了，拦截");
					abortBroadcast();//终止广播
				}
			}
		}
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "开启服务了");
		dao = new BlackNumberDao(this);
		//当开启骚扰拦截服务后，也注册短信接收者
		receiver = new InnerSmsReciver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		this.registerReceiver(receiver, filter);
		//注册一个电话状态的监听器
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyPhoneStateListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "关闭服务了");
		super.onDestroy();
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
		unregisterReceiver(receiver);
		receiver = null;
	}
	
	/**
	 *电话状态的监听器 
	 */
	class MyPhoneStateListener extends PhoneStateListener{

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE://空闲的状态
				
				break;
			case TelephonyManager.CALL_STATE_RINGING://响铃的状态
				String mode = dao.find(incomingNumber);
				//拦截模式：1.电话拦截 2.短信拦截 3.全部拦截
				if("1".equals(mode) || "3".equals(mode)){
					Log.d(TAG, "挂断电话");
					//从1.5版本后，挂断电话的api被隐藏起来了
					//调用系统底层的服务方法挂断电话
					EndCall();
					//利用内容解析者清除呼叫记录
					DeleteCallLog(incomingNumber);
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK://接通电话的状态
				
				break;

			default:
				break;
			}
		}
	}
	/**
	 * 挂断电话
	 */
	private void EndCall() {
//		ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
		try {
			Class clazz = getClassLoader().loadClass("android.os.ServiceManager");
			 Method method = clazz.getDeclaredMethod("getService", String.class);
			 IBinder ibinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
			 ITelephony telephony = ITelephony.Stub.asInterface(ibinder);
			 telephony.endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 删除黑名单号码历史记录
	 * @param incomingNumber 来自黑名单号码
	 */
	private void DeleteCallLog(final String incomingNumber) {
		final ContentResolver resolver = this.getContentResolver();
		final Uri uri = Uri.parse("content//call_log//calls");
		//利用内容观察者，观察呼叫记录数据库，如果生成了呼叫历史记录，就立刻清除历史记录
		resolver.registerContentObserver(uri, true, new ContentObserver(new Handler()) {

			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				//当内容观察者观察到数据库的内容变化的时候就调用该方法。
				resolver.delete(uri, "phone=?", new String[]{incomingNumber});
			}
		});
	}
}
