package edu.ucsd.cse110.zooseeker.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucsd.cse110.zooseeker.event.SettingsUpdateListener;

public class SettingsUtil {
	private static final List<SettingsUpdateListener> listeners = new ArrayList<>();

	public static void registerSettingsUpdateListener(SettingsUpdateListener listener)
	{
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	public static void unregisterSettingsUpdateListener(SettingsUpdateListener listener)
	{
		if(listeners.contains(listener))
			listeners.remove(listener);
	}
	private static void updateListeners(String key, String value)
	{
		for(SettingsUpdateListener listener : listeners)
			listener.onSettingsUpdate(key, value);
	}

	public static void set(Context context, String key, String value)
	{
		//Perform necessary actions to update/save new settings (key,value)
		HashMap<String, String> map = load(context);
		map.put(key, value);
		save(context, map);
		updateListeners(key, value);
	}

	public static String get(Context context, String key, String defaultReturn)
	{
		//Perform necessary actions to return current value associated with key
		HashMap<String, String> map = load(context);
		return map.get(key) == null ? defaultReturn : map.get(key);
	}

	public static int getSize(Context context){
		HashMap<String, String> map = load(context);
		return map.size();
	}

	public static void save(Context context, HashMap<String, String> map){
		Gson gson = new Gson();
		String gsonString = gson.toJson(map);
		SharedPreferences preference = context.getSharedPreferences("setting", MODE_PRIVATE);
		preference.edit().putString("map", gsonString).apply();
	}

	public static HashMap<String, String> load(Context context){
		SharedPreferences preference = context.getSharedPreferences("setting", MODE_PRIVATE);
		Gson gson = new Gson();
		String json = preference.getString("map", "");
		Type type = new TypeToken<HashMap<String, String>>(){}.getType();

		if(gson.fromJson(json, type) == null){
			return new HashMap<String, String>();
		}
		return gson.fromJson(json, type);
	}
}
