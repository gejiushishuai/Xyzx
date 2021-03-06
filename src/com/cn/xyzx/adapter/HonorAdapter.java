package com.cn.xyzx.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cn.xyzx.R;
import com.cn.xyzx.bean.HonorModel;
import com.qianjiang.framework.imageloader.core.DisplayImageOptions;
import com.qianjiang.framework.imageloader.core.DisplayImageOptions.Builder;
import com.qianjiang.framework.imageloader.core.ImageLoader;
import com.qianjiang.framework.imageloader.core.display.SimpleBitmapDisplayer;
import com.qianjiang.framework.util.UIUtil;

public class HonorAdapter extends BaseAdapter {
	private static final int SPACE_VALUE = 50;
	private static final int NUM_COLUMNS = 3;
	private int mWidth;
	private Context mContext;
	private List<HonorModel> mProductList;
	private ImageLoader mImageDownloader;
	private Builder mOptions;

	public HonorAdapter(Activity context, List<HonorModel> list, ImageLoader loader) {
		mContext = context;
		mProductList = list;
		mImageDownloader = loader;
		mOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc()
				.displayer(new SimpleBitmapDisplayer());
		DisplayMetrics metric = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metric);
		mWidth = metric.widthPixels;
	}

	@Override
	public int getCount() {
		if (null == mProductList) {
			return 0;
		}
		return mProductList.size();
	}

	@Override
	public HonorModel getItem(int position) {
		if (null != mProductList) {
			return mProductList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GridViewHolder holder = new GridViewHolder();
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.view_info_center_item, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.info_center_img);
			holder.textView = (TextView) convertView.findViewById(R.id.info_center_tv);
			convertView.setTag(holder);
		} else {
			holder = (GridViewHolder) convertView.getTag();
		}

		HonorModel productModel = mProductList.get(position);
		mImageDownloader.displayImage(productModel.getPicture(), holder.imageView,
				mOptions.showImageForEmptyUri(R.drawable.news_default).build());
		holder.textView.setText(productModel.getTitle());
		LayoutParams layoutParams = holder.imageView.getLayoutParams();
		layoutParams.width = (mWidth - UIUtil.dip2px(mContext, SPACE_VALUE) * (NUM_COLUMNS + 1)) / NUM_COLUMNS;
		layoutParams.height = layoutParams.width * 2 / 3;
		holder.imageView.setLayoutParams(layoutParams);
		return convertView;
	}

	final class GridViewHolder {
		ImageView imageView;
		TextView textView;
	}
}
