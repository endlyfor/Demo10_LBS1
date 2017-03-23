package com.exams.demo10_lbs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final int CMD_STOP_SERVICE = 0;

	public static final String TAG = "MainActivity";
	public Button startbtnButton, stopButton,showButton;
	public TextView tView;
	DataReceiver dataReceiver;// BroadcastReceiver对象
	public LocationManager lManager;
	public String latitude,longitude;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startbtnButton = (Button) findViewById(R.id.Startbtn);
		stopButton = (Button) findViewById(R.id.Stopbtn);
		showButton= (Button) findViewById(R.id.showOnMap);

		
		tView = (TextView) findViewById(R.id.tv);
		Log.i(TAG,"oncreate");

		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 判断GPS是否正常启动
		if (!lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.i(TAG,"Gps Provider disabled");
			Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_LONG).show();
			// 返回开启GPS导航设置界面
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			//startActivityForResult(intent, 0);
			startActivity(intent);
			return;
		}

		startbtnButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				startService();
				
			}
		});
		stopButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopService();
				
			}
		});

		showButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,BmapActivity.class);
				intent.putExtra("latitude",latitude);
				intent.putExtra("longitude",longitude);
				startActivity(intent);
			}
		});

	}

	private void startService() {
		startbtnButton.setEnabled(false);
		stopButton.setEnabled(true);
		Intent i = new Intent(this, LBSService.class);
		this.startService(i);
		Log.i(TAG, "in startService method.");
		if (dataReceiver == null) {
			dataReceiver = new DataReceiver();
			IntentFilter filter = new IntentFilter();// 创建IntentFilter对象
			filter.addAction("com.exams.demo10_lbs");
			registerReceiver(dataReceiver, filter);// 注册Broadcast Receiver
		}
	}

	private void stopService() {
		startbtnButton.setEnabled(true);
		stopButton.setEnabled(false);
		Intent i = new Intent(this, LBSService.class);
		this.stopService(i);
		Log.i(TAG, "in stopService method.");
		if (dataReceiver != null) {
			unregisterReceiver(dataReceiver);// 取消注册Broadcast Receiver
			dataReceiver = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class DataReceiver extends BroadcastReceiver {// 继承自BroadcastReceiver的子类

		@Override
		public void onReceive(Context context, Intent intent) {// 重写onReceive方法

			Bundle bundledata = intent.getExtras();
			if (bundledata != null) {
				 latitude = bundledata.getString("latitude");
				 longitude = bundledata.getString("longitude");
				String accuracy = bundledata.getString("accuracy");
				String speed=bundledata.getString("speed");
				String Satenum = bundledata.getString("Satenum");
				String dateString = bundledata.getString("date");
				tView.setText("\t卫星在用数量:" + Satenum + "\n\t纬度:" + latitude
						+ "\t经度:" + longitude + "\n\t精度:" + accuracy
						+"\n\t速度:"+speed+ "\n\t更新时间:" + dateString);
			}

		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(TAG, "onstart");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i(TAG, "onstop");
	}
	@Override
	protected  void onDestroy(){
		super.onDestroy();
		Log.i(TAG, "ondestroy");
//		Intent i = new Intent(this, LBSService.class);
//		this.stopService(i);
//
//		if (dataReceiver != null) {
//			unregisterReceiver(dataReceiver);// 取消注册Broadcast Receiver
//			dataReceiver = null;
//		}
		stopService();

	}

}
