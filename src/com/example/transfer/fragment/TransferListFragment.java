package com.example.transfer.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.transfer.MainActivity;
import com.tf.transfer.R;
import com.example.transfer.bean.TaskRecord;
import com.example.transfer.util.SocketThreadFactory;
import com.example.transfer.util.SqliteAdapter;

public class TransferListFragment extends Fragment{

	private Button button_back;
	private ViewPager viewPager;
	private MainActivity activity;
	private ArrayList<View> listViewPager = new ArrayList<View>();
	private String[] title = new String[]{"当前传输", "历史记录"};
	private PagerTabStrip tabStrip;
	private ArrayList<TaskRecord> list_current = new ArrayList<TaskRecord>();
	private ArrayList<TaskRecord> list_history = new ArrayList<TaskRecord>();
	private ListView listView_current;
	private ListView listView_history;
	private LruCache<String, Bitmap> mMemoryCache;
	private View view1, view2;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity) activity;
		//获取系统分配给每个应用程序的最大内存，每个应用系统分配32M  
        int maxMemory = (int) Runtime.getRuntime().maxMemory();    
        int mCacheSize = maxMemory / 8;  
        //给LruCache分配1/8 4M  
        mMemoryCache = new LruCache<String, Bitmap>(mCacheSize){  
            //必须重写此方法，来测量Bitmap的大小  
            @Override  
            protected int sizeOf(String key, Bitmap value) {  
                return value.getRowBytes() * value.getHeight();  
            }  
        };
	}
	
	/** 
     * 添加Bitmap到内存缓存 
     * @param key 
     * @param bitmap 
     */  
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {    
        if (mMemoryCache.get(key) == null && bitmap != null) {    
            mMemoryCache.put(key, bitmap);    
        }    
    }
	
    /** 
     * 获取Bitmap, 内存中没有就去手机或者sd卡中获取，这一步在getView中会调用，比较关键的一步 
     * @param url 
     * @return 
     */  
    public Bitmap showCacheBitmap(String url){  
        if(mMemoryCache.get(url) != null){  
            return mMemoryCache.get(url);  
        }else if(new File(url).exists() && new File(url).length() != 0){  
            //从SD卡获取手机里面获取Bitmap  
            Bitmap bitmap = BitmapFactory.decodeFile(url);  
            //将Bitmap 加入内存缓存  
            addBitmapToMemoryCache(url, bitmap);  
            return bitmap;  
        }  
        return null;  
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_transfer_list, null);
		button_back = (Button) view.findViewById(R.id.transfer_list_back);
		button_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				activity.getFragmentManager().popBackStack();
			}
		});
		list_current = getCurrentList();
		list_history = getHistoryList();
		viewPager = (ViewPager) view.findViewById(R.id.transfer_list_viewpager);
		tabStrip = (PagerTabStrip) view.findViewById(R.id.transfer_list_TabStrip);
		tabStrip.setTabIndicatorColorResource(R.color.theme_color);
		if(listViewPager.size() == 0){
			view1 = inflater.inflate(R.layout.fragment_current_list, null);
			view2 = inflater.inflate(R.layout.fragment_current_list, null);
			listViewPager.add(view1);
			listViewPager.add(view2);
		}
		inti1(view1);
		init2(view2);
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				list_current = getCurrentList();
				list_history = getHistoryList();
				list_adapter.notifyDataSetChanged();
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
		return view;
	}
	
	private void init2(View view) {
		listView_history = (ListView) view.findViewById(R.id.transfer_list_view);
		listView_history.setAdapter(list_adapter);
		listView_history.setOnItemClickListener(listener);
	}

	private void inti1(View view) {
		listView_current = (ListView) view.findViewById(R.id.transfer_list_view);
		listView_current.setAdapter(list_adapter);
		listView_current.setOnItemClickListener(listener);
	}

	OnItemClickListener listener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			SendFileFragment fragment = new SendFileFragment();
			if(viewPager.getCurrentItem() == 1){
				if(list_history.get(arg2).getType() == 2){
					activity.showQRCodeDialog(list_history.get(arg2).getRemark(), list_history.get(arg2).getQrCode_path());
				}else{
					Bundle bundle = new Bundle();
					bundle.putLong("id", list_history.get(arg2).getId());
					bundle.putString("qrCode_path", list_history.get(arg2).getQrCode_path());
					bundle.putBoolean("flag", false);
					fragment.setArguments(bundle);
					activity.switchFragment(fragment, true, bundle);
				}
			}else{
				Bundle bundle = new Bundle();
				bundle.putLong("id", list_current.get(arg2).getId());
				bundle.putString("qrCode_path", list_current.get(arg2).getQrCode_path());
				bundle.putBoolean("flag", false);
				fragment.setArguments(bundle);
				activity.switchFragment(fragment, true, bundle);
			}
		}
	};
	
	BaseAdapter list_adapter = new BaseAdapter() {
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ViewHolder holder;
			if(arg1 == null){
				holder = new ViewHolder();
				arg1 = getActivity().getLayoutInflater().inflate(R.layout.item_transfer_list, null);
				holder.image = (ImageView) arg1.findViewById(R.id.item_transfer_qrCode);
				holder.time = (TextView) arg1.findViewById(R.id.item_transfer_time);
				holder.id = (TextView) arg1.findViewById(R.id.item_transfer_id);
				arg1.setTag(holder);
			}else{
				holder = (ViewHolder) arg1.getTag();
			}
			if(viewPager.getCurrentItem() == 1){
				holder.image.setImageBitmap(showCacheBitmap(list_history.get(arg0).getQrCode_path()));
				holder.time.setText("时间:"+list_history.get(arg0).getTime());
				holder.id.setText("ID:"+list_history.get(arg0).getId());
			}else{
				holder.image.setImageBitmap(showCacheBitmap(list_current.get(arg0).getQrCode_path()));
				holder.time.setText("时间:"+list_current.get(arg0).getTime());
				holder.id.setText("ID:"+list_current.get(arg0).getId());
			}
			return arg1;
		}
		
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		
		@Override
		public Object getItem(int arg0) {
			if(viewPager.getCurrentItem() == 1)
				return list_history.get(arg0);
			else
				return list_current.get(arg0);
		}
		
		@Override
		public int getCount() {
			if(viewPager.getCurrentItem() == 1){
				return list_history.size();
			}else{
				return list_current.size();
			}
		}
	};
	
	static class ViewHolder	{		
		public ImageView image;	
		public TextView time;	
		public TextView id;
	}
	
	
	PagerAdapter adapter = new PagerAdapter() {
		
		public Object instantiateItem(ViewGroup container, int position) {
			View view = listViewPager.get(position);
			container.addView(view);
			return view;
		}
		
		public void destroyItem(View container, int position, Object object) {
			((ViewPager)container).removeView(listViewPager.get(position));
		}
		
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (arg0==arg1);
		}
		
		@Override
		public int getCount() {
			return listViewPager.size();
		}
		
		public CharSequence getPageTitle(int position) {
			return title[position];
		};
		
	};
	
	//获取历史记录列表
	public ArrayList<TaskRecord> getHistoryList(){
		ArrayList<TaskRecord> list = new ArrayList<TaskRecord>();
		SqliteAdapter adapter = new SqliteAdapter(getActivity());
		Cursor cursor = adapter.getTask();
		while(cursor.moveToNext()){
			list.add(new TaskRecord(cursor.getLong(cursor.getColumnIndex("_id")), 
					cursor.getString(cursor.getColumnIndex("qr_code")), 
					cursor.getString(cursor.getColumnIndex("create_time")), 
					cursor.getInt(cursor.getColumnIndex("type")),
					cursor.getString(cursor.getColumnIndex("remark"))));
		}
		return list;
	}
	
	//获取当前记录列表
	public ArrayList<TaskRecord> getCurrentList(){
		SqliteAdapter adapter = new SqliteAdapter(getActivity());
		ArrayList<Map<String, Object>> list_path = SocketThreadFactory.getCurrentTaskList();
		ArrayList<TaskRecord> list_task = new ArrayList<TaskRecord>();
		for(int i = 0;i<list_path.size();i++){
			Map<String, Object> map = list_path.get(i);
			long id = (long) map.get("id");
			Cursor cursor = adapter.getTask(id);
			while(cursor.moveToNext()){
				list_task.add(new TaskRecord(cursor.getLong(cursor.getColumnIndex("_id")), 
						cursor.getString(cursor.getColumnIndex("qr_code")), 
						cursor.getString(cursor.getColumnIndex("create_time")),
						cursor.getInt(cursor.getColumnIndex("type")),""));
			}
		}
		System.out.println("当前任务数："+list_task.size());
		return list_task;
	}
}
