package com.example.zhihupocket;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ScrollView;

import com.example.adapter.HotStoriesPagersAdapter;
import com.example.adapter.StoriesAdapter;
import com.example.listener.StoryItemClickListener;
import com.example.task.GetStoriesAndParseTask;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
/*
 * PagedHeadListView可以用这个开源项目做
 */
public class MainActivity extends FragmentActivity {

	public static final String ZHIHU_API = "http://news-at.zhihu.com/api/3/news/latest";
	public static final String ZHIHU_STORY_API = "http://daily.zhihu.com/story/";
	private ListView lv_showshortcontent;
	public static File pic_cache;
	private ViewPager hotstoriespagers;
	private PullToRefreshScrollView main_swiperefresh;
	@SuppressWarnings("unused")
	private ScrollView main_sv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("MainActivity", "nonono!");
		setContentView(R.layout.activity_main);	
		initView();
		initImageLoaderConfigurations();
	}

	// 初始化视图
	@SuppressLint("InlinedApi")
	public void initView(){
		lv_showshortcontent = (ListView)findViewById(R.id.lv_showshortcontent);
		hotstoriespagers = (ViewPager)findViewById(R.id.hotstoriespagers);
		
		main_swiperefresh = (PullToRefreshScrollView)findViewById(R.id.main_sv);
		main_swiperefresh.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
//				Thread getandparseData = new Thread(new GetStoriesAndParse(MainActivity.this, main_thread_handler, main_swiperefresh));
//				getandparseData.start();
				new GetStoriesAndParseTask(MainActivity.this, main_swiperefresh).execute();
			}
		});
	}
	
	public boolean initImageLoaderConfigurations(){
		try {
			//创建缓存目录，程序一启动就创建
			pic_cache = new File(Environment.getExternalStorageDirectory(), "zhihupocketcache");
			if(!pic_cache.exists()){
					pic_cache.mkdir();
			}
			@SuppressWarnings("deprecation")
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.denyCacheImageMultipleSizesInMemory()
			.discCache(new UnlimitedDiscCache(pic_cache))
			.diskCacheSize(50 * 1024 * 1024) // 50 Mb
			.tasksProcessingOrder(QueueProcessingType.LIFO)
			.writeDebugLogs() // Remove for release app
			.build();
			// Initialize ImageLoader with configuration.
			ImageLoader.getInstance().init(config);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	
	// 加载视图
	public void runView(ArrayList<HashMap<String, Object>> stories_group, final ArrayList<HashMap<String, Object>> topstories_group){
		// TODO Auto-generated method stub
		//在ui线程中设置listview
		lv_showshortcontent = (ListView)findViewById(R.id.lv_showshortcontent);
		StoriesAdapter loadlistadapter = new StoriesAdapter(getApplicationContext(), stories_group);
		lv_showshortcontent.setAdapter(loadlistadapter);
		
		// 设置当互动到当前的listitem时才去加载图片
//		lv_showshortcontent.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true, this));
		
		lv_showshortcontent.setOnItemClickListener(new StoryItemClickListener(getApplicationContext(), stories_group));
		hotstoriespagers.setAdapter(new HotStoriesPagersAdapter(getSupportFragmentManager(), topstories_group));
		hotstoriespagers.setVisibility(View.VISIBLE);
		// 添加点击监听器
		hotstoriespagers.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// getDisplayedChild方法获取的是前一个的
				Log.v("MainActivity", "viewpagerclick!");
				int i = hotstoriespagers.getCurrentItem()+1;
				i = i<5?i:i-5;
				Intent intent = new Intent(MainActivity.this, StoryContent.class);
				intent.putExtra("stories_group", topstories_group);
				// 万万没想到，标记的时候这个是反着来的
				intent.putExtra("story_order", i);
				startActivity(intent);
			}
		});
	    main_swiperefresh.onRefreshComplete();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
            	finish();
                return true;
            default:
            	return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() ==0){
			System.exit(0);
			return true;
		}
		else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
