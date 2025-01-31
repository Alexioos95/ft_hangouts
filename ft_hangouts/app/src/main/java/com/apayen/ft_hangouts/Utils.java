package com.apayen.ft_hangouts;

import android.Manifest;
import static androidx.core.app.ActivityCompat.requestPermissions;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class Utils implements DefaultLifecycleObserver
{
	//////////////////////////////
	// Variables
	//////////////////////////////
	Application application;
	//////////////////////////////
	// Functions - Life Cycle
	//////////////////////////////
	public Utils(Application application)
	{
		this.application = application;
		ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
	}
	@Override
	public void onStop(@NonNull LifecycleOwner owner)
	{
		long timestamp = System.currentTimeMillis();

		SharedPreferences prefs = application.getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
		prefs.edit().putLong("inBackgroundFor", timestamp).apply();
	}
	@Override
	public void onStart(@NonNull LifecycleOwner owner)
	{
		SharedPreferences prefs = application.getSharedPreferences("AppPrefs", Application.MODE_PRIVATE);
		long lastBackgroundTime = prefs.getLong("inBackgroundFor", 0);

		if (lastBackgroundTime > 1)
		{
			long elapsedTime = System.currentTimeMillis() - lastBackgroundTime;
			long elapsedSeconds = elapsedTime / 1000;
			String message = this.application.getString(R.string.in_background_for) + " " + elapsedSeconds + " " + this.application.getString(R.string.seconds);

			Toast.makeText(this.application, message, Toast.LENGTH_SHORT).show();
		}
	}
	//////////////////////////////
	// Functions - SMS Permissions
	//////////////////////////////
	public interface PermissionResultCallback
	{
		void onPermissionGranted();
		void onPermissionDenied();
	}
	public static void requestSMSPermissions(Activity activity, PermissionResultCallback callback)
	{
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
			|| ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
		{
			if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS) || !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS))
				Toast.makeText(activity, activity.getString(R.string.sms_permissions_always_denied), Toast.LENGTH_LONG).show();
			requestPermissions(activity, new String[]{Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS}, 1);
		}
		else
			callback.onPermissionGranted();
	}
	//////////////////////////////
	// Functions - DP to PX
	//////////////////////////////
	public static int convertDpToPx(Context context, int dp)
	{ return ((int)(dp * context.getResources().getDisplayMetrics().density)); }
	//////////////////////////////
	// Functions - Colors
	//////////////////////////////
	public static void updateHeaderColor(AppCompatActivity activity, SharedPreferences preferences, int[] rgb)
	{
		int color = Color.rgb(rgb[0], rgb[1], rgb[2]);

		if (activity != null)
		{
			Toolbar toolbar = activity.findViewById(R.id.my_toolbar);
			if (toolbar != null)
				toolbar.setBackgroundColor(color);
			activity.getWindow().setStatusBarColor(color);
		}
		saveColorInStorage(preferences, rgb);
		updateTextColor(activity, preferences, color);
	}
	private static void saveColorInStorage(SharedPreferences preferences, int[] rgb)
	{
		SharedPreferences.Editor editor = preferences.edit();

		editor.putInt("settings_red", rgb[0]);
		editor.putInt("settings_green", rgb[1]);
		editor.putInt("settings_blue", rgb[2]);
		editor.apply();
	}
	public static int[] getColorFromStorage(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
		int red = preferences.getInt("settings_red", 255);
		int green = preferences.getInt("settings_green", 255);
		int blue = preferences.getInt("settings_blue", 255);

		return (new int[]{red, green, blue});
	}
	public static void updateTextColor(AppCompatActivity activity, SharedPreferences preferences, int color)
	{
		int black = Color.parseColor("#000000");
		int white = Color.parseColor("#FFFFFF");
		double contrastWithBlack = calculateContrast(color, black);
		double contrastWithWhite = calculateContrast(color, white);

		if (activity != null)
		{
			Window window = activity.getWindow();
			Toolbar toolbar = activity.findViewById(R.id.my_toolbar);
			MenuItem settingActionItem = toolbar.getMenu().findItem(R.id.action_settings);
			MenuItem saveActionItem = toolbar.getMenu().findItem(R.id.action_save_contact);
			MenuItem phoneActionItem = toolbar.getMenu().findItem(R.id.action_call_contact);
			MenuItem messageActionItem = toolbar.getMenu().findItem(R.id.action_message_contact);
			SharedPreferences.Editor editor = preferences.edit();

			if (contrastWithBlack > contrastWithWhite)
			{
				android.graphics.drawable.Drawable overflow = ContextCompat.getDrawable(activity, R.drawable.action_more_black);
				if (overflow != null)
				{
					overflow.setBounds(0, 0, 24, 48);
					toolbar.setOverflowIcon(overflow);
				}
				toolbar.setTitleTextColor(black);
				if (settingActionItem != null)
					settingActionItem.setIcon(R.drawable.action_settings_black);
				if (saveActionItem != null)
					saveActionItem.setIcon(R.drawable.action_save_black);
				if (phoneActionItem != null)
					phoneActionItem.setIcon(R.drawable.action_phone_black);
				if (messageActionItem != null)
					messageActionItem.setIcon(R.drawable.action_message_black);
				window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			}
			else
			{
				android.graphics.drawable.Drawable overflow = ContextCompat.getDrawable(activity, R.drawable.action_more_white);
				if (overflow != null)
				{
					overflow.setBounds(0, 0, 24, 24);
					toolbar.setOverflowIcon(overflow);
				}
				toolbar.setTitleTextColor(white);
				if (settingActionItem != null)
					settingActionItem.setIcon(R.drawable.action_settings_white);
				if (saveActionItem != null)
					saveActionItem.setIcon(R.drawable.action_save_white);
				if (phoneActionItem != null)
					phoneActionItem.setIcon(R.drawable.action_phone_white);
				if (messageActionItem != null)
					messageActionItem.setIcon(R.drawable.action_message_white);
				window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			}
			editor.apply();
		}
	}
	public static double calculateContrast(int color1, int color2) {
		int[] rgb1 = getRGBComponents(color1);
		int[] rgb2 = getRGBComponents(color2);
		double luminance1 = getLuminance(rgb1[0], rgb1[1], rgb1[2]);
		double luminance2 = getLuminance(rgb2[0], rgb2[1], rgb2[2]);
		double brighter = Math.max(luminance1, luminance2);
		double darker = Math.min(luminance1, luminance2);

		return ((brighter + 0.05) / (darker + 0.05));
	}
	private static int[] getRGBComponents(int color)
	{ return (new int[] {(color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF}); }
	private static double getLuminance(int red, int green, int blue)
	{
		double sr = (red / 255.0);
		double sg = (green / 255.0);
		double sb = (blue / 255.0);

		sr = (sr <= 0.03928) ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
		sg = (sg <= 0.03928) ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
		sb = (sb <= 0.03928) ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);

		return (0.2126 * sr + 0.7152 * sg + 0.0722 * sb);
	}
}
