package com.example.transfer.fragment;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.transfer.MainActivity;
import com.tf.transfer.R;
import com.example.transfer.ui.swipemenulistview.SwipeMenu;
import com.example.transfer.ui.swipemenulistview.SwipeMenuCreator;
import com.example.transfer.ui.swipemenulistview.SwipeMenuItem;
import com.example.transfer.ui.swipemenulistview.SwipeMenuListView;
import com.example.transfer.ui.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.example.transfer.util.FileUtils;

public class MainFragment extends Fragment implements OnClickListener{

	private Button button_transfer, button_setting, button_transfer_list, button_photo, button_doc, button_vedio, button_music, button_other;
	private AutoCompleteTextView acTextView;
	private MainActivity activity;
	private ArrayList<File> list_file = new ArrayList<File>();
	private SwipeMenuListView mListView;
	private AppAdapter adapter;
	private LruCache<String, Bitmap> mMemoryCache;
	private int type = 1;//照片1  文档2  视频3  音乐4  其他5
	
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
            Bitmap new_bitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 200);
            //将Bitmap 加入内存缓存  
            addBitmapToMemoryCache(url, new_bitmap);  
            return new_bitmap;  
        }  
        return null;  
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFileList();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, null);
		button_photo =  (Button) view.findViewById(R.id.main_photo);
		button_doc =  (Button) view.findViewById(R.id.main_doc);
		button_vedio =  (Button) view.findViewById(R.id.main_vedio);
		button_music =  (Button) view.findViewById(R.id.main_music);
		button_other =  (Button) view.findViewById(R.id.main_other);
		button_photo.setOnClickListener(this);
		button_doc.setOnClickListener(this);
		button_vedio.setOnClickListener(this);
		button_music.setOnClickListener(this);
		button_other.setOnClickListener(this);
		acTextView = (AutoCompleteTextView) view.findViewById(R.id.main_search);
		acTextView.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, getFilesNames()));
		acTextView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FileUtils.openFile(getActivity(), new File(Environment.getExternalStorageDirectory()+"/Transfer/files/"+acTextView.getText().toString()));
				acTextView.setText("");
			}
		});
		button_transfer = (Button) view.findViewById(R.id.main_transfer);
		button_transfer.setOnClickListener(this);
		button_setting = (Button) view.findViewById(R.id.main_setting);
		button_setting.setOnClickListener(this);
		button_transfer_list = (Button) view.findViewById(R.id.main_transfer_list);
		button_transfer_list.setOnClickListener(this);
		mListView = (SwipeMenuListView) view.findViewById(R.id.main_listview);
		mListView.setMenuCreator(creator);
		// step 2. listener item click event
		mListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void onMenuItemClick(int position, SwipeMenu menu, int index) {
				switch (index) {
				case 0:
					// 改名
					modifyName(position);
					break;
				case 1:
					// 删除
					if(delete(position)){
						list_file.remove(position);
						adapter.notifyDataSetChanged();
					}else
						Toast.makeText(activity, "文件删除失败", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FileUtils.openFile(getActivity(), list_file.get(arg2));
			}
		});
		adapter = new AppAdapter();
		mListView.setAdapter(adapter);
		return view;
	}
	
	//修改文件名称
	protected void modifyName(int position) {
		final File file = list_file.get(position);
		final EditText edit = new EditText(getActivity());
		edit.setText(file.getName());
		edit.setFocusable(true);
		edit.setInputType(InputType.TYPE_CLASS_TEXT);
//        FrameLayout fl = new FrameLayout(getActivity()); 
//        fl.addView(edit);
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setView(edit);
        ad.setTitle("请输入新的文件名:");
        ad.setPositiveButton("确定", new DialogInterface.OnClickListener() {               
        	public void onClick(DialogInterface dialog, int whichButton) {
        		File newFile = new File(file.getParent()+"/"+edit.getText().toString());
        		if(!newFile.exists()){
        			boolean renameFlag = file.renameTo(newFile);
        			if(renameFlag){
        				getFileList();
        				adapter.notifyDataSetChanged();
        			}else{
        				Toast.makeText(getActivity(), "修改文件名失败", Toast.LENGTH_SHORT).show();
        			}
        		}else{
        			Toast.makeText(getActivity(), "文件名已存在", Toast.LENGTH_SHORT).show();
        		}
            }                
        });
        ad.create();
        ad.show();
	}

	protected boolean delete(int position) {
		File file = list_file.get(position);
		// 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	        return file.delete();  
	    }  
	    return false;
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.main_transfer:
			activity.openSlidingMenu();
			break;
		case R.id.main_transfer_list:
			activity.switchFragment(new TransferListFragment(), true, null);
			break;
		case R.id.main_setting:
			activity.switchFragment(new SettingFragment(), true, null);
			break;
		case R.id.main_photo:
			type = 1;
			list_file = FileUtils.getFiles(FileUtils.PHOTO);
			adapter.notifyDataSetChanged();
			setButtonColor();
			break;
		case R.id.main_doc:
			type = 2;
			list_file = FileUtils.getFiles(FileUtils.DOCUMENT);
			adapter.notifyDataSetChanged();
			setButtonColor();
			break;
		case R.id.main_vedio:
			type = 3;
			list_file = FileUtils.getFiles(FileUtils.VEDIO);
			adapter.notifyDataSetChanged();
			setButtonColor();
			break;
		case R.id.main_music:
			type = 4;
			list_file = FileUtils.getFiles(FileUtils.MUSIC);
			adapter.notifyDataSetChanged();
			setButtonColor();
			break;
		case R.id.main_other:
			type = 5;
			list_file = FileUtils.getFiles(FileUtils.OTHER);
			adapter.notifyDataSetChanged();
			setButtonColor();
			break;
		}
	}
	
	public void setButtonColor(){
		switch (type) {
		case 1:
			button_photo.setTextColor(getResources().getColor(R.color.theme_color));
			button_doc.setTextColor(getResources().getColor(R.color.title_color));
			button_vedio.setTextColor(getResources().getColor(R.color.title_color));
			button_music.setTextColor(getResources().getColor(R.color.title_color));
			button_other.setTextColor(getResources().getColor(R.color.title_color));
			break;
		case 2:
			button_photo.setTextColor(getResources().getColor(R.color.title_color));
			button_doc.setTextColor(getResources().getColor(R.color.theme_color));
			button_vedio.setTextColor(getResources().getColor(R.color.title_color));
			button_music.setTextColor(getResources().getColor(R.color.title_color));
			button_other.setTextColor(getResources().getColor(R.color.title_color));
			break;
		case 3:
			button_photo.setTextColor(getResources().getColor(R.color.title_color));
			button_doc.setTextColor(getResources().getColor(R.color.title_color));
			button_vedio.setTextColor(getResources().getColor(R.color.theme_color));
			button_music.setTextColor(getResources().getColor(R.color.title_color));
			button_other.setTextColor(getResources().getColor(R.color.title_color));
			break;
		case 4:
			button_photo.setTextColor(getResources().getColor(R.color.title_color));
			button_doc.setTextColor(getResources().getColor(R.color.title_color));
			button_vedio.setTextColor(getResources().getColor(R.color.title_color));
			button_music.setTextColor(getResources().getColor(R.color.theme_color));
			button_other.setTextColor(getResources().getColor(R.color.title_color));
			break;
		case 5:	
			button_photo.setTextColor(getResources().getColor(R.color.title_color));
			button_doc.setTextColor(getResources().getColor(R.color.title_color));
			button_vedio.setTextColor(getResources().getColor(R.color.title_color));
			button_music.setTextColor(getResources().getColor(R.color.title_color));
			button_other.setTextColor(getResources().getColor(R.color.theme_color));
			break;
		}
		
	}
	
	SwipeMenuCreator creator = new SwipeMenuCreator() {

		@Override
		public void create(SwipeMenu menu) {
			// create "open" item
			SwipeMenuItem openItem = new SwipeMenuItem(activity);
			// set item background
			openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
					0xCE)));
			// set item width
			openItem.setWidth(dp2px(50));
			// set item title
			openItem.setTitle("改名");
			// set item title fontsize
			openItem.setTitleSize(18);
			// set item title font color
			openItem.setTitleColor(Color.WHITE);
			// add to menu
			menu.addMenuItem(openItem);

			// create "delete" item
			SwipeMenuItem deleteItem = new SwipeMenuItem(activity);
			// set item background
			deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
					0x3F, 0x25)));
			// set item width
			deleteItem.setWidth(dp2px(50));
			// set a icon
			deleteItem.setTitle("删除");
			deleteItem.setTitleSize(18);
			// set item title font color
			deleteItem.setTitleColor(Color.WHITE);
			// add to menu
			menu.addMenuItem(deleteItem);
		}
	};
	
	class AppAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list_file.size();
		}

		@Override
		public Object getItem(int position) {
			return list_file.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(activity, R.layout.item_main_list, null);
				new ViewHolder(convertView);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			if(type == 1)
				holder.iv_icon.setImageBitmap(showCacheBitmap(list_file.get(position).getAbsolutePath()));
			else
				holder.iv_icon.setImageResource(getImage(list_file.get(position).getName()));
			holder.tv_name.setText(list_file.get(position).getName());
			if(list_file.get(position).length()/1024 < 1024)
				holder.tv_size.setText(list_file.get(position).length()/1024+"KB");
			else{
				float length = list_file.get(position).length()/1024;
				length /= 1024;
				DecimalFormat decimalFormat=new DecimalFormat("0.00");
				holder.tv_size.setText(decimalFormat.format(length)+"MB");
			}
			return convertView;
		}

		class ViewHolder {
			ImageView iv_icon;
			TextView tv_name;
			TextView tv_size;

			public ViewHolder(View view) {
				iv_icon = (ImageView) view.findViewById(R.id.item_main_image);
				tv_name = (TextView) view.findViewById(R.id.item_main_name);
				tv_size = (TextView) view.findViewById(R.id.item_main_size);
				view.setTag(this);
			}
		}
	}
	
	private int getImage(String fileName){
		switch (type) {
		case 2:
			String[] fileNames = fileName.split("\\.");
			if(fileNames[1].equals("doc"))
				return R.drawable.doc_90;
			else if(fileNames[1].equals("xls"))
				return R.drawable.xls_90;
			else if(fileNames[1].equals("ppt"))
				return R.drawable.ppt_90;
		case 3:
			return R.drawable.vedio_90;
		case 4:
			return R.drawable.music_90;
		case 5:
			fileNames = fileName.split("\\.");
			if(fileNames[1].equals("txt"))
				return R.drawable.txt_90;
			else if(fileNames[1].equals("zip") || fileNames[1].equals("rar"))
				return R.drawable.yasuobao_90;
		}
		return R.drawable.weizhi_90;
	}
	
	//根据当前type获取文件列表
	public void getFileList(){
		switch (type) {
		case 1:
			list_file = FileUtils.getFiles(FileUtils.PHOTO);
			break;
		case 2:
			list_file = FileUtils.getFiles(FileUtils.DOCUMENT);
			break;
		case 3:
			list_file = FileUtils.getFiles(FileUtils.VEDIO);
			break;
		case 4:
			list_file = FileUtils.getFiles(FileUtils.MUSIC);
			break;
		case 5:
			list_file = FileUtils.getFiles(FileUtils.OTHER);
			break;
		}
	}
	
	//获得目录下的文件名数组
	public String[] getFilesNames(){
		File dir = new File(Environment.getExternalStorageDirectory()+"/Transfer/files/");
        if(!dir.exists()){
            System.out.println(dir.mkdirs());
        }
        return dir.list();
	}
	
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
	
}
