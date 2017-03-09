package com.example.mobile.safe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

/**
 * 检查服务状态的工具类
 */
public class ServiceStatusUtils {

	/**
	 * 判断某个服务是否处于运行状态
	 * @param 上下文
	 * @param  服务的全路径名称
	 */
	public static boolean isServiceRunning(Context context,String serviceFullName){
		//得到系统进程的管理器
		ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		//得到系统里面正在运行的服务
		List<RunningServiceInfo> services = am.getRunningServices(200);
		for(RunningServiceInfo info:services){
			if(serviceFullName.equals(info.service.getClassName())){
				return true;
			}
		}
		return false;
	}
}
