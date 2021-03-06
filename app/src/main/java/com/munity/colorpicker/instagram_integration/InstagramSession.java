package com.munity.colorpicker.instagram_integration;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by shafeek on 15/06/16.
 */
public class InstagramSession {

	private SharedPreferences sharedPref;
	private Editor editor;
	private static final String SHARED = "Instagram_Preferences";
	private static final String API_USERNAME = "username";
	private static final String API_ID = "id";
	private static final String API_NAME = "name";
	private static final String API_ACCESS_TOKEN = "access_token";

	public InstagramSession(Context context) {
		sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
		editor = sharedPref.edit();
	}

	public void storeAccessToken(String accessToken, String id, String username, String name) {
		editor.putString(API_ID, id);
		editor.putString(API_NAME, name);
		editor.putString(API_ACCESS_TOKEN, accessToken);
		editor.putString(API_USERNAME, username);
		editor.commit();
	}

	public void resetAccessToken() {
		editor.putString(API_ID, null);
		editor.putString(API_NAME, null);
		editor.putString(API_ACCESS_TOKEN, null);
		editor.putString(API_USERNAME, null);
		editor.commit();
	}

	public String getId() {
		return sharedPref.getString(API_ID, null);
	}

	public String getName() {
		return sharedPref.getString(API_NAME, null);
	}

	public String getAccessToken() {
		return sharedPref.getString(API_ACCESS_TOKEN, null);
	}

}