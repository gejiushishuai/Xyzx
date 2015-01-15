package com.cn.xyzx.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Video;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.cn.xyzx.R;
import com.cn.xyzx.activity.InfoCenterActivity;
import com.cn.xyzx.activity.VideoPlayerActivity;
import com.cn.xyzx.adapter.VideoAdapter;
import com.cn.xyzx.bean.VideoModel;
import com.cn.xyzx.db.DbDao;
import com.cn.xyzx.req.VideoReq;
import com.cn.xyzx.util.ActionResult;
import com.qianjiang.framework.app.QJActivityBase.ActionListener;
import com.qianjiang.framework.util.NetUtil;
import com.qianjiang.framework.util.StringUtil;
import com.qianjiang.framework.util.UIUtil;

public class VideoFragment extends FragmentBase implements OnItemClickListener, OnClickListener {

	private List<VideoModel> mLeaderList;
	private GridView mGvHonor;
	private VideoAdapter mAdapter;

	public static final VideoFragment newInstance() {
		VideoFragment fragment = new VideoFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mLeaderList = new ArrayList<VideoModel>();
		mAdapter = new VideoAdapter(getActivity(), mLeaderList, mImageLoader, this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.fragment_info, container, false);
		mGvHonor = (GridView) mView.findViewById(R.id.info_gridView);
		mGvHonor.setNumColumns(3);
		mGvHonor.setAdapter(mAdapter);
		mGvHonor.setOnItemClickListener(this);
		getLeaderList();
		return mView;
	}

	private void getLeaderList() {
		if (isAdded()) {
			((InfoCenterActivity) getActivity()).showLoading();
		}
		new AsyncLogin().execute();
	}

	class AsyncLogin extends AsyncTask<Void, Void, ActionResult> {

		@Override
		protected void onPostExecute(ActionResult result) {
			if (result != null && ActionResult.RESULT_CODE_SUCCESS.equals(result.ResultCode)) {

			} else {
				showErrorMsg(result);
			}
			mLeaderList.clear();
			mLeaderList.addAll(DbDao.getModels(VideoModel.class));
			mAdapter.notifyDataSetChanged();
			if (isAdded()) {
				((InfoCenterActivity) getActivity()).dismissLoading();
			}
		}

		@Override
		protected ActionResult doInBackground(Void... params) {
			return VideoReq.getVideoInfo();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		VideoModel model = mAdapter.getItem(position);
		if (null != model && !StringUtil.isNullOrEmpty(model.getId())) {
			Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
			intent.putExtra(Video.class.getName(), model);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(final View v) {
		UIUtil.limitReClick(VideoFragment.class.getName(), new ActionListener() {

			@Override
			public void doAction() {
				switch (v.getId()) {
					case R.id.iv_down_icon:
						if (!isAdded()) {
							return;
						}
						if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
							toast(getActivity().getString(R.string.no_sd_card));
							return;
						}
						if (!NetUtil.isNetworkAvailable()) {
							toast(getActivity().getString(R.string.network_is_not_available));
							return;
						}
						VideoModel model = (VideoModel) v.getTag();
						if (null == model || StringUtil.isNullOrEmpty(model.getFileName())
								|| StringUtil.isNullOrEmpty(model.getVideoUrl())) {
							return;
						}
						((InfoCenterActivity) getActivity()).startDownload(model.getFileName(), model.getTitle(),
								model.getVideoUrl(), model.getPicture());
						break;
					default:
						break;
				}
			}
		});
	}
}
