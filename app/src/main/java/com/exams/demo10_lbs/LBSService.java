package com.exams.demo10_lbs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LBSService extends Service {

	public static final String TAG = "LBSService";
	// 30000ms --minimum time interval between location updates, in milliseconds
	private static final long minTime = 3000;
	// 最小变更距离 10m --minimum distance between location updates, in meters
	private static final float minDistance = 10;

	String tag = this.toString();
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location location;
	private GpsStatus gStatus;
	private GpsSatelliteListener gpsSatelliteListener;
	private final IBinder mBinder = new LBSServiceBinder();
	private NotificationManager mNM;
	boolean flag;
	CommandReceiver cmdReceiver;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		flag = true;
		cmdReceiver = new CommandReceiver();

		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		startService();
		Log.i(TAG, "in onCreate method.");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(cmdReceiver);// 取消注册的CommandReceiver
		super.onDestroy();
		stopService();
		mNM.cancel(R.string.lbsservice);
		Log.i(TAG, "in onDestroy method.");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();// 创建IntentFilter对象

		filter.addAction("com.exams.demo10_lbs.LBSService");

		registerReceiver(cmdReceiver, filter);// 注册Broadcast
												// Receiver,后续会接收相关广播intent
		doJob();// 调用方法启动线程
		Log.i(TAG,"doJob");
		return super.onStartCommand(intent, flags, startId);

	}

	public class LBSServiceBinder extends Binder {
		LBSService getService() {
			return LBSService.this;
		}
	}

	public void startService() {

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LBSServiceListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				minTime, minDistance, locationListener);
		gpsSatelliteListener = new GpsSatelliteListener();
		locationManager.addGpsStatusListener(gpsSatelliteListener);
		Log.i(TAG, "in startService method.");
	}

	public void stopService() {
		if (locationManager != null && locationListener != null
				&& gpsSatelliteListener != null) {
			locationManager.removeUpdates(locationListener);
			locationManager.removeGpsStatusListener(gpsSatelliteListener);
		}
		Log.i(TAG, "in stopService method.");
	}

	private class CommandReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int cmd = intent.getIntExtra("cmd", -1);// 获取Extra信息
			if (cmd == MainActivity.CMD_STOP_SERVICE) {// 如果发来的消息是停止服务
				flag = false;// 停止线程
				stopSelf();// 停止服务
			}
		}// 继承自BroadcastReceiver的子类

	}

	/** * Show a notification while this service is running. */
	@SuppressWarnings("deprecation")
	private void showNotification() {
		// In this sample, we'll use the same text
		// for the ticker and the expanded
		// notification
		CharSequence text = getText(R.string.lbsservice);
		// Set the icon, scrolling text and timestamp

		Notification notification = new Notification(R.drawable.ic_launcher,
				text, System.currentTimeMillis());
		// The PendingIntent to launch our activity if the user selects this
		Intent intent = new Intent(this, MainActivity.class);

		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, "LBSService",
				"LBS Service started", contentIntent);
		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(R.string.lbsservice, notification);
	}

	// 方法：
	public void doJob() {
		new Thread() {

			public void run() {

				while (flag) {

					try {// 睡眠一段时间
						Thread.sleep(20000);

					} catch (Exception e) {

						e.printStackTrace();

					}

					Intent intent = new Intent();// 创建Intent对象

					intent.setAction("com.exams.demo10_lbs");

					location = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					gStatus = locationManager.getGpsStatus(null);
					// 获取默认最大卫星数
					int maxSatellites = gStatus.getMaxSatellites();
					Iterable<GpsSatellite> iterable = gStatus.getSatellites();
					Iterator<GpsSatellite> iterator = iterable.iterator();
					int x = 0;

					while (iterator != null && iterator.hasNext()
							&& x <= maxSatellites) {
						GpsSatellite gpsSatellite = (GpsSatellite) iterator
								.next();
						if (gpsSatellite.usedInFix())
							x++;

					}
					String latitude, longitude, accuracy, speed;
					if (location != null) {
						latitude = location.getLatitude() + "";
						longitude = location.getLongitude() + "";
						accuracy = location.getAccuracy() + "";
						speed = location.getSpeed() + "";
					} else {
						latitude = "0.0";
						longitude = "0.0";
						accuracy = "未知 ";
						speed = "0.0";
					}
					Bundle bundle = new Bundle();
					bundle.putString("latitude", latitude);
					bundle.putString("longitude", longitude);
					bundle.putString("accuracy", accuracy + "m");
					bundle.putString("speed", speed + "m/s");
					bundle.putString("Satenum", x + "个");
					SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date nowDate=new Date();
					String dateString=sDateFormat.format(nowDate);
					bundle.putString("date",  dateString+ "");
					intent.putExtras(bundle);

					sendBroadcast(intent);// 发送广播

				}

			}

		}.start();

	}

}
