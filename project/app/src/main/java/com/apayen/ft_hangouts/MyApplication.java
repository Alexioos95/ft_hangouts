package com.apayen.ft_hangouts;

import android.app.Application;

public class MyApplication extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		new Utils(this);
	}
}