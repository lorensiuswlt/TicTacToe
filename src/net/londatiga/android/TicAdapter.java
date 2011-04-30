package net.londatiga.android;

import net.londatiga.android.Tictactoe.TicData;

import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import android.content.Context;

import java.util.ArrayList;

public class TicAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<TicData> mData;
	private int mLayoutParam;
	
	public TicAdapter(Context context) {
		mContext = context;
	}
	
	public void setData(ArrayList<TicData> data) {
		mData = data;
	}
	
	public void setLayoutParam(int param) {
		mLayoutParam = param;
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView ticImage;
		
		if (convertView == null) {
			ticImage = new ImageView(mContext);
			
			ticImage.setLayoutParams(new GridView.LayoutParams(mLayoutParam, mLayoutParam));
            ticImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ticImage.setPadding(1, 1, 1, 1);
		} else {
			ticImage = (ImageView) convertView;
		}
		
		
		return ticImage;
	}
}