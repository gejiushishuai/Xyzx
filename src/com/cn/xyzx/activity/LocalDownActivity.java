package com.cn.xyzx.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cn.xyzx.R;
import com.cn.xyzx.adapter.LocalDownLoadAdapter;
import com.cn.xyzx.bean.FileStateModel;
import com.cn.xyzx.download.AppConstant;
import com.cn.xyzx.download.DownLoadDao;
import com.cn.xyzx.download.DownloadService;
import com.cn.xyzx.download.DownloadService.PunchBinder;

public class LocalDownActivity extends Activity {
	private DownLoadDao mDownLoadDao;// 用来与数据库交互
	private ListView mLvDownload;
	private List<FileStateModel> mFileStateModels;// 用于存放要显示的列表
	private LocalDownLoadAdapter mLocalDownLoadAdapter;// 自定义adapter
	private UpdateReceiver mUpdateReceiver;// 广播接收器
	private DownloadService mService;
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PunchBinder binder = (PunchBinder) service;
			mService = binder.getService();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_download);
		bindPunchService();
		initVariable();
		initViews();
	}

	private void initVariable() {
		mDownLoadDao = new DownLoadDao(this);
		mFileStateModels = new ArrayList<FileStateModel>();
		mUpdateReceiver = new UpdateReceiver();
		mUpdateReceiver.registerAction(AppConstant.LocalActivityConstant.update_action);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mFileStateModels.clear();
		mFileStateModels.addAll(mDownLoadDao.getFileState());
		mLocalDownLoadAdapter.notifyDataSetChanged();

	}

	/**
	 * 初始化UI
	 * **/
	private void initViews() {
		mLvDownload = (ListView) this.findViewById(R.id.listview);
		mLocalDownLoadAdapter = new LocalDownLoadAdapter(this, mFileStateModels, mDownLoadDao, new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.btn_local_delete:
						mService.deleteData((String) v.getTag());
						break;
					case R.id.btn_start_pause_download:
						FileStateModel fileState = (FileStateModel) v.getTag();
						if (null != fileState) {
							mService.switchState(fileState.getMusicName());
						}
						break;

					default:
						break;
				}
			}
		}, mLvDownload);
		mLvDownload.setAdapter(mLocalDownLoadAdapter);
		mLvDownload.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FileStateModel fileState = (FileStateModel) parent.getAdapter().getItem(position);
				if (null != fileState) {
					mService.switchState(fileState.getMusicName());
				}
			}

		});
	}

	class UpdateReceiver extends BroadcastReceiver {

		public void registerAction(String action) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(action);
			registerReceiver(this, intentFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收来自DownloadService传送过来的数据,并且更新进度条
			if (intent.getAction().equals(AppConstant.LocalActivityConstant.update_action)) {
				String url = intent.getStringExtra("url");
				int completeSize = intent.getIntExtra("completeSize", 0);
//				System.out.println("completeSize:" + completeSize);
				for (int i = 0; i < mFileStateModels.size(); i++) {
					FileStateModel fileState = mFileStateModels.get(i);
					if (fileState.getUrl().equals(url)) {
						fileState.setCompleteSize(completeSize);
						mLocalDownLoadAdapter.updateProgress(fileState);
						break;
					}
				}
			}
		}
	}

	protected void bindPunchService() {
		Intent mIntent = new Intent(this, DownloadService.class);
		bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

	protected void unbindPunchService() {
		try {
			unbindService(mConnection);
			Log.d("aaa", "unbindPunchService");
		} catch (IllegalArgumentException e) {
			Log.d("aaa", "Service wasn't bound!");
		}
	}

	@Override
	protected void onDestroy() {
		unbindPunchService();
		unregisterReceiver(mUpdateReceiver);
		mDownLoadDao.updateFileDownState(mFileStateModels);// 当activity退出时,更新localdown_info这个表
		super.onDestroy();
	}

}