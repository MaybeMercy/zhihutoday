package com.example.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.example.zhihupocket.MainActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class StoriesHandleSQLite {

	Context context;
	MainDBHelper dbhelper;
	SQLiteDatabase db;
	String date;
	
	public StoriesHandleSQLite(Context context){
		this.context = context;
		Date format = MainActivity.sys_calendar.getTime();
		date = MainActivity.DATEFORMAT.format(format);
	}
	
	// 将普通故事放入数据库中
	public boolean storedStoriesIntoDB(ArrayList<HashMap<String, Object>> stories_group){
		try {
			dbhelper = new MainDBHelper(context, MainDBHelper.DATABASE_NAME, null, 1);
			// 先判断数据库中有木有
			db = dbhelper.getReadableDatabase();
			Cursor cursor = db.query(MainDBHelper.TABLE_STORIES, new String[]{"id"},"date="+date, null, null, null, null, null);
			// 删除数据库中原来有记录
			db = dbhelper.getWritableDatabase();
			if (cursor.getCount()!=0) {
				String del = "delete from '"+MainDBHelper.TABLE_STORIES+"' where date='"+date+"'";
				db.execSQL(del);
			}
			// 重新写入
			ContentValues values = new ContentValues();
			for (int i = 0; i < stories_group.size(); i++) {
				 values.put("date", date);
				 values.put("images", stories_group.get(i).get("images").toString());
				 values.put("id", stories_group.get(i).get("id").toString());
				 values.put("type", stories_group.get(i).get("type").toString());
				 values.put("title", stories_group.get(i).get("title").toString());
				 values.put("share_url", stories_group.get(i).get("share_url").toString());
				 values.put("ga_prefix", stories_group.get(i).get("ga_prefix").toString());
				 db.insert(MainDBHelper.TABLE_STORIES, "id", values);
				 values.clear();
			}
			db.close();
			dbhelper.close();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	
	// 将普通故事从数据库中取出来
	public ArrayList<HashMap<String, Object>> getStoriesFromDB(){
		try {
			dbhelper = new MainDBHelper(context, MainDBHelper.DATABASE_NAME, null, 1);
			db = dbhelper.getReadableDatabase();
			Cursor cursor = db.query(MainDBHelper.TABLE_STORIES, new String[]{"id", "images", "title", "type", "share_url", "ga_prefix"},"date="+date, null, null, null, "ga_prefix DESC", null);
			Log.v("TopStoriesHandleSQLite.getStoriesFromDB", cursor.getColumnCount()+"");
			if (cursor.getCount()==0) {
				Toast.makeText(context, "数据库出现错误！", Toast.LENGTH_SHORT).show();
				return null;
			}
			else {
				ArrayList<HashMap<String, Object>> stories = new ArrayList<HashMap<String,Object>>();
				HashMap<String, Object> item;
				while (cursor.moveToNext()) {
					item = new HashMap<String, Object>();
					item.put("id", cursor.getString(cursor.getColumnIndex("id")));
					item.put("title", cursor.getString(cursor.getColumnIndex("title")));
					item.put("images", cursor.getString(cursor.getColumnIndex("images")));
					item.put("type", cursor.getString(cursor.getColumnIndex("type")));
					item.put("ga_prefix", cursor.getString(cursor.getColumnIndex("ga_prefix")));
					item.put("share_url", cursor.getString(cursor.getColumnIndex("share_url")));
					stories.add(item);
				}
				db.close();
				dbhelper.close();
				return stories;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			db.close();
			dbhelper.close();
			return null;
		}
	}
}
